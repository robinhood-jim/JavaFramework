package com.robin.comm.fileaccess.iterator;

import com.robin.comm.fileaccess.util.MockFileSystem;
import com.robin.comm.fileaccess.util.OrcUtil;
import com.robin.comm.utils.SysUtils;
import com.robin.core.base.exception.OperationNotSupportException;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.fileaccess.iterator.AbstractFileIterator;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;
import com.robin.comm.sql.CompareNode;
import com.robin.core.fileaccess.util.AvroUtils;
import com.robin.core.fileaccess.util.ResourceUtil;
import com.robin.hadoop.hdfs.HDFSUtil;
import org.apache.avro.Schema;
import org.apache.calcite.sql.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.flink.core.memory.MemorySegment;
import org.apache.flink.core.memory.MemorySegmentFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.common.type.HiveDecimal;
import org.apache.hadoop.hive.ql.exec.vector.*;
import org.apache.hadoop.hive.ql.io.sarg.PredicateLeaf;
import org.apache.hadoop.hive.ql.io.sarg.SearchArgument;
import org.apache.hadoop.hive.ql.io.sarg.SearchArgumentFactory;
import org.apache.hadoop.hive.serde2.io.HiveDecimalWritable;
import org.apache.orc.OrcFile;
import org.apache.orc.Reader;
import org.apache.orc.RecordReader;
import org.apache.orc.TypeDescription;
import org.apache.parquet.filter2.predicate.FilterPredicate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.parquet.filter2.predicate.FilterApi.and;
import static org.apache.parquet.filter2.predicate.FilterApi.or;

/**
 * Orc SearchArgument only filter row Groups,must use second filter
 */
public class OrcFileIterator extends AbstractFileIterator {
    private Configuration conf;
    private List<TypeDescription> fields;
    private TypeDescription schema;
    private RecordReader rows ;
    private VectorizedRowBatch batch ;
    private MemorySegment segment;
    private Double allowOffHeapDumpLimit= ResourceConst.ALLOWOUFHEAPMEMLIMIT;
    private Reader.Options options;

    public OrcFileIterator(){
        identifier= Const.FILEFORMATSTR.ORC.getValue();
    }
    public OrcFileIterator(DataCollectionMeta colmeta) {
        super(colmeta);
        identifier= Const.FILEFORMATSTR.ORC.getValue();
        if(!CollectionUtils.isEmpty(colmeta.getResourceCfgMap()) && colmeta.getResourceCfgMap().containsKey(ResourceConst.ALLOWOFFHEAPKEY)){
            allowOffHeapDumpLimit=Double.parseDouble(colmeta.getResourceCfgMap().get(ResourceConst.ALLOWOFFHEAPKEY).toString());
        }
    }
    public OrcFileIterator(DataCollectionMeta colmeta, AbstractFileSystemAccessor accessor) {
        super(colmeta);
        identifier= Const.FILEFORMATSTR.ORC.getValue();
        if(!CollectionUtils.isEmpty(colmeta.getResourceCfgMap()) && colmeta.getResourceCfgMap().containsKey(ResourceConst.ALLOWOFFHEAPKEY)){
            allowOffHeapDumpLimit=Double.parseDouble(colmeta.getResourceCfgMap().get(ResourceConst.ALLOWOFFHEAPKEY).toString());
        }
        accessUtil=accessor;
    }

    int maxRow=0;
    int currentRow=0;
    private FileSystem fs;
    private Reader oreader;
    private File tmpFile;

    @Override
    protected void pullNext() {
        try {
            cachedValue.clear();
            if (maxRow > 0 && currentRow >= maxRow ) {
                currentRow = 0;
                boolean exists=rows.nextBatch(batch);
                if(!exists){
                    return;
                }
                maxRow = batch.size;
            }
            List<String> fieldNames=schema.getFieldNames();
            if(!CollectionUtils.isEmpty(fields)){
                for(int i=0;i<fields.size();i++){
                    OrcUtil.wrapValue(fields.get(i),fieldNames.get(i),batch.cols[i],currentRow,cachedValue);
                }
            }
            currentRow++;
        }catch (Exception ex){
            logger.error("{}",ex.getMessage());
        }
    }





    @Override
    public void beforeProcess() {
        try {
            String readPath=colmeta.getPath();
            if(Const.FILESYSTEM.HDFS.getValue().equals(colmeta.getFsType())){
                HDFSUtil util=new HDFSUtil(colmeta);
                conf=util.getConfig();
                fs=FileSystem.get(conf);
            }else {
                conf=new Configuration(false);
                checkAccessUtil(null);
                if(Const.FILESYSTEM.LOCAL.getValue().equals(colmeta.getFsType())){
                    fs=FileSystem.get(new Configuration());
                    if(!readPath.startsWith("file:/")) {
                        readPath = new File(readPath).toURI().toString();
                    }
                }else {
                    instream = accessUtil.getRawInputStream(ResourceUtil.getProcessPath(colmeta.getPath()));
                    long size = accessUtil.getInputStreamSize(ResourceUtil.getProcessPath(colmeta.getPath()));
                    Double freeMemory= SysUtils.getFreeMemory();
                    if (size < ResourceConst.MAX_ARRAY_SIZE && freeMemory>allowOffHeapDumpLimit) {
                        //use flink memory utils to use offHeapMemory to dump file content
                        segment= MemorySegmentFactory.allocateOffHeapUnsafeMemory((int)size,this,new Thread(){});
                        ByteBuffer byteBuffer=segment.getOffHeapBuffer();
                        try(ReadableByteChannel channel= Channels.newChannel(instream)) {
                            org.apache.commons.io.IOUtils.readFully(channel, byteBuffer);
                            byteBuffer.position(0);
                        }
                        fs=new MockFileSystem(conf,byteBuffer);
                    } else {
                        String tmpPath = com.robin.core.base.util.FileUtils.getWorkingPath(colmeta);
                        String tmpFilePath = "file:///" + tmpPath + ResourceUtil.getProcessFileName(colmeta.getPath());
                        tmpFile = new File(new URL(tmpFilePath).toURI());
                        copyToLocal(tmpFile, instream);
                        fs = FileSystem.get(new Configuration());
                        readPath = tmpFilePath;
                    }
                }
            }
            schema=OrcUtil.getSchema(colmeta);
            avroSchema= AvroUtils.getSchemaFromMeta(colmeta);
            if(!ObjectUtils.isEmpty(super.segment) && !ObjectUtils.isEmpty(super.segment.getWhereCause()) &&!super.segment.isConditionHasFunction() && !super.segment.isConditionHasFunction() && !super.segment.isHasRightColumnCmp()){
                SearchArgument.Builder argumentBuilder=SearchArgumentFactory.newBuilder();
                OrcUtil.walkCondition(this,super.segment.getWhereCause(),argumentBuilder);
                options= new Reader.Options().schema(schema).allowSARGToFilter(true)
                        .searchArgument(argumentBuilder.build(),columnList.toArray(new String[0]));
            }
            oreader =OrcFile.createReader(new Path(readPath),OrcFile.readerOptions(conf).filesystem(fs));
            if(schema==null) {
                schema = oreader.getSchema();
                if(schema!=null){
                    avroSchema=OrcUtil.parseSchemaByType(schema,colmeta);
                }
            }

            if(options!=null){
                rows=oreader.rows(options);
            }else {
                rows = oreader.rows();
            }
            fields=schema.getChildren();
            batch= schema.createRowBatch();
            rows.nextBatch(batch);
            maxRow=batch.size;

        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        if(rows!=null){
            rows.close();
        }
        if(!ObjectUtils.isEmpty(fs)){
            fs.close();
        }
        if(!ObjectUtils.isEmpty(oreader)){
            oreader.close();
        }
        if(!ObjectUtils.isEmpty(segment)){
            segment.free();
        }
        if(!ObjectUtils.isEmpty(tmpFile)){
            FileUtils.deleteQuietly(tmpFile);
        }
    }
}
