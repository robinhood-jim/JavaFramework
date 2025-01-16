/*
 * Copyright (c) 2015,robinjim(robinjim@126.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.robin.core.fileaccess.iterator;

import com.robin.comm.dal.pool.ResourceAccessHolder;
import com.robin.comm.sql.CommRecordGenerator;
import com.robin.comm.sql.CommSqlParser;
import com.robin.comm.sql.SqlSegment;
import com.robin.core.base.exception.MissingConfigException;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.IOUtils;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.fileaccess.fs.ApacheVfsFileSystemAccessor;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;
import com.robin.core.fileaccess.util.*;
import org.apache.calcite.config.Lex;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.*;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public abstract class AbstractFileIterator implements IResourceIterator {
    protected BufferedReader reader;
    protected InputStream instream;
    protected AbstractFileSystemAccessor accessUtil;
    protected String identifier;
    protected DataCollectionMeta colmeta;
    protected List<String> columnList = new ArrayList<>();
    protected Map<String, DataSetColumnMeta> columnMap = new HashMap<>();
    protected Logger logger = LoggerFactory.getLogger(getClass());
    protected String filterSql = "";
    protected boolean useFilter = false;
    protected Map<String, Object> cachedValue = new HashMap<>();
    //calculate column run async, so use concurrent
    protected Map<String, Object> newRecord = new ConcurrentHashMap<>();
    // filterSql parse compare tree
    protected String defaultNewColumnPrefix = "N_COLUMN";
    protected DateTimeFormatter formatter=DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    //if using BufferedReader as input.only csv json format must set this to true
    protected boolean useBufferedReader=false;
    protected boolean useOrderBy=false;
    protected boolean useGroupBy=false;
    protected LinkedHashMap<byte[],Map<String,Object>> groupByMap=new LinkedHashMap<>();

    protected SqlSegment segment;
    protected Iterator<Map.Entry<byte[],Map<String,Object>>> groupIter;

    public AbstractFileIterator() {

    }

    public AbstractFileIterator(DataCollectionMeta colmeta) {
        this.colmeta = colmeta;
        for (DataSetColumnMeta meta : colmeta.getColumnList()) {
            columnList.add(meta.getColumnName());
            columnMap.put(meta.getColumnName(), meta);
            if (Const.META_TYPE_FORMULA.equals(meta.getColumnType())) {
                meta.setColumnType(Const.META_TYPE_DOUBLE);
            }
        }
        if (!CollectionUtils.isEmpty(colmeta.getResourceCfgMap()) && !ObjectUtils.isEmpty(colmeta.getResourceCfgMap().get(ResourceConst.STORAGEFILTERSQL))) {
            withFilterSql(colmeta.getResourceCfgMap().get(ResourceConst.STORAGEFILTERSQL).toString());
        }

    }

    public AbstractFileIterator(DataCollectionMeta colmeta, AbstractFileSystemAccessor accessUtil) {
        this.colmeta = colmeta;
        for (DataSetColumnMeta meta : colmeta.getColumnList()) {
            columnList.add(meta.getColumnName());
            columnMap.put(meta.getColumnName(), meta);
        }
        this.accessUtil = accessUtil;
    }

    @Override
    public void beforeProcess() {
        checkAccessUtil(colmeta.getPath());
        Assert.notNull(accessUtil, "ResourceAccessUtil is required!");
        try {
            if(useBufferedReader){
                Pair<BufferedReader, InputStream> pair = accessUtil.getInResourceByReader(colmeta, ResourceUtil.getProcessPath(colmeta.getPath()));
                this.reader = pair.getKey();
                this.instream = pair.getValue();
            }else{
                this.instream=accessUtil.getInResourceByStream(colmeta,ResourceUtil.getProcessPath(colmeta.getPath()));
            }
            if(useOrderBy || useGroupBy){
                //pool all record through OffHeap
                ByteBuffer buffer=ByteBuffer.allocate(512);
                pullNext();
                while (!CollectionUtils.isEmpty(cachedValue)){
                    while (!CollectionUtils.isEmpty(cachedValue) && useFilter && !CommRecordGenerator.doesRecordAcceptable(segment, cachedValue)) {
                        pullNext();
                    }
                    if (segment != null && (!segment.isIncludeAllOriginColumn() && !CollectionUtils.isEmpty(segment.getSelectColumns()))) {
                        newRecord.clear();
                        CommRecordGenerator.doAsyncCalculator(segment, cachedValue, newRecord);
                    }
                    //get group by column
                    buffer.position(0);
                    if(!CollectionUtils.isEmpty(segment.getGroupBy())){
                        for(SqlNode tnode:segment.getGroupBy()) {
                            String columnName=((SqlIdentifier)tnode).getSimple();
                            if (!ObjectUtils.isEmpty(newRecord.get(columnName))) {
                                ByteBufferUtils.fillPrimitive(buffer,newRecord.get(columnName), StandardCharsets.UTF_8);
                            }
                            ByteBufferUtils.fillGap(buffer);
                        }
                        doGroupAgg(ByteBufferUtils.getContent(buffer));
                    }
                    pullNext();
                }
                groupIter=groupByMap.entrySet().iterator();
            }

        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
        }
    }
    private void doGroupAgg(byte[] key){
        try{
            for (int i = 0; i < segment.getSelectColumns().size(); i++) {
                CommSqlParser.ValueParts parts=segment.getSelectColumns().get(i);
                if(SqlKind.LISTAGG.equals(parts.getSqlKind())){
                    Calculator calculator = CommRecordGenerator.getCalculatePool().borrowObject();
                    calculator.clear();
                    calculator.setValueParts(segment.getSelectColumns().get(i));
                    calculator.setInputRecord(cachedValue);
                    calculator.setSegment(segment);
                    SqlContentResolver.doAggregate(calculator,parts,key,groupByMap);
                }
            }
        }catch (Exception ex){

        }
    }

    @Override
    public void afterProcess() {
        try {
            close();
        } catch (IOException ex) {
            logger.error("{}", ex.getMessage());
        }
    }


    protected void checkAccessUtil(String inputPath) {
        try {
            if (accessUtil == null) {
                URI uri = new URI(StringUtils.isEmpty(inputPath) ? colmeta.getPath() : inputPath);
                String schema = !ObjectUtils.isEmpty(colmeta.getFsType()) ? colmeta.getFsType() : uri.getScheme();
                accessUtil = ResourceAccessHolder.getAccessUtilByProtocol(schema.toLowerCase());
            }
        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
        }
    }

    @Override
    public void setReader(BufferedReader reader) {
        this.reader = reader;
    }

    @Override
    public void setInputStream(InputStream stream) {
        this.instream = stream;
    }

    protected void copyToLocal(File tmpFile, InputStream stream) {
        try (FileOutputStream outputStream = new FileOutputStream(tmpFile)) {
            IOUtils.copyBytes(stream, outputStream, 8192);
        } catch (IOException ex) {
            logger.error("{}", ex.getMessage());
        }
    }

    @Override
    public void close() throws IOException {
        if (reader != null) {
            reader.close();
        }
        if (instream != null) {
            instream.close();
        }
        PolandNotationUtil.freeMem();
        if (accessUtil != null) {
            if (ApacheVfsFileSystemAccessor.class.isAssignableFrom(accessUtil.getClass()) && !ObjectUtils.isEmpty(colmeta.getResourceCfgMap().get(Const.ITERATOR_PROCESSID))) {
                ((ApacheVfsFileSystemAccessor) accessUtil).closeWithProcessId(colmeta.getResourceCfgMap().get(Const.ITERATOR_PROCESSID).toString());
            }
        }
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    public AbstractFileSystemAccessor getFileSystemAccessor() {
        return accessUtil;
    }

    public void setAccessUtil(AbstractFileSystemAccessor accessUtil) {
        this.accessUtil = accessUtil;
    }

    @Override
    public boolean hasNext() {
        try {
            // no order by
            if(!useOrderBy) {
                pullNext();
                while (!CollectionUtils.isEmpty(cachedValue) && useFilter && !CommRecordGenerator.doesRecordAcceptable(segment, cachedValue)) {
                    pullNext();
                }
                if (segment != null && (!segment.isIncludeAllOriginColumn() && !CollectionUtils.isEmpty(segment.getSelectColumns()))) {
                    newRecord.clear();
                    CommRecordGenerator.doAsyncCalculator(segment, cachedValue, newRecord);
                }
                return !CollectionUtils.isEmpty(cachedValue);
            }else{
                //capture all record to offHeap
                cachedValue.clear();
                if(groupIter.hasNext()) {
                    cachedValue.putAll(groupIter.next().getValue());
                }
                return !CollectionUtils.isEmpty(cachedValue);
            }
        } catch (Exception ex) {
            throw new MissingConfigException(ex);
        }
    }

    @Override
    public Map<String, Object> next() {
        return useFilter && !CollectionUtils.isEmpty(newRecord) ? newRecord : cachedValue;
    }

    public void withFilterSql(String filterSql) {
        this.filterSql = filterSql;
        segment = CommSqlParser.parseSingleTableQuerySql(filterSql, Lex.MYSQL, colmeta, defaultNewColumnPrefix);
        useFilter = true;
        useOrderBy=!CollectionUtils.isEmpty(segment.getOrderBys());
        useGroupBy=!CollectionUtils.isEmpty(segment.getGroupBy());
    }
    public void setDateFormatter(DateTimeFormatter formatter){
        this.formatter=formatter;
    }

    protected abstract void pullNext();
    public DataCollectionMeta getCollectionMeta(){
        return colmeta;
    }
}
