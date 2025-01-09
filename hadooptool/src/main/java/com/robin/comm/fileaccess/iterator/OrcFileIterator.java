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
import com.robin.core.fileaccess.util.ResourceUtil;
import com.robin.hadoop.hdfs.HDFSUtil;
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
            currentRow++;
            if(!CollectionUtils.isEmpty(fields)){
                for(int i=0;i<fields.size();i++){
                    wrapValue(fields.get(i),fieldNames.get(i),batch.cols[i],currentRow,cachedValue);
                }
            }
        }catch (Exception ex){
            logger.error("{}",ex.getMessage());
        }
    }



    public void wrapValue(TypeDescription schema,String columnName, ColumnVector vector,int row,Map<String,Object> valueMap){
        if(vector.noNulls || !vector.isNull[row]){
            switch (schema.getCategory()){
                case BOOLEAN:
                    valueMap.put(columnName,((LongColumnVector)vector).vector[row]!=0);
                    break;
                case SHORT:
                    valueMap.put(columnName,Long.valueOf(((LongColumnVector)vector).vector[row]).shortValue());
                    break;
                case INT:
                    valueMap.put(columnName,Long.valueOf(((LongColumnVector)vector).vector[row]).intValue());
                    break;
                case LONG:
                    valueMap.put(columnName,((LongColumnVector)vector).vector[row]);
                    break;
                case FLOAT:
                case DOUBLE:
                    valueMap.put(columnName,((DoubleColumnVector)vector).vector[row]);
                    break;
                case DECIMAL:
                    valueMap.put(columnName,((DecimalColumnVector)vector).vector[row].getHiveDecimal().bigDecimalValue());
                    break;
                case STRING:
                case CHAR:
                case VARCHAR:
                    valueMap.put(columnName,((BytesColumnVector)vector).toString(row));
                    break;
                case DATE:
                    valueMap.put(columnName,new Timestamp(((LongColumnVector)vector).vector[row]));
                    break;
                case TIMESTAMP:
                case TIMESTAMP_INSTANT:
                    valueMap.put(columnName,((TimestampColumnVector)vector).asScratchTimestamp(row));
                    break;
                case LIST:
                case MAP:
                case STRUCT:
                    break;
                default:
                    throw new IllegalArgumentException("Unknown type " + schema.toString());
            }
        }
    }

    private void walkCondition(SqlNode node, SearchArgument.Builder argumentBuilder){
        if(!super.segment.isConditionHasFunction() && !super.segment.isConditionHasFourOperations()) {
            if (SqlBasicCall.class.isAssignableFrom(node.getClass())) {
                List<SqlNode> nodes = ((SqlBasicCall) node).getOperandList();
                if (SqlIdentifier.class.isAssignableFrom(nodes.get(0).getClass()) && SqlLiteral.class.isAssignableFrom(nodes.get(1).getClass())) {
                    parseOperator(node,argumentBuilder);
                } else {
                    boolean canUse=false;
                    List<SqlNode> nodes1=((SqlBasicCall) node).getOperandList();
                    if(SqlKind.OR.equals(node.getKind())){
                        argumentBuilder.startOr();
                        canUse=true;
                    }else if(SqlKind.AND.equals(node.getKind())){
                        argumentBuilder.startAnd();
                        canUse=true;
                    }
                    if(canUse) {
                        walkCondition(nodes1.get(0), argumentBuilder);
                        walkCondition(nodes1.get(0), argumentBuilder);
                        argumentBuilder.end();
                    }else{
                        argumentBuilder.literal(SearchArgument.TruthValue.YES);
                    }

                }
            }
        }
    }
    private void parseOperator(SqlNode node,SearchArgument.Builder argumentBuilder){
        List<SqlNode> nodes=((SqlBasicCall)node).getOperandList();
        String column=((SqlIdentifier)nodes.get(0)).getSimple();
        Pair<PredicateLeaf.Type,Object> pair=returnType(column,((SqlLiteral)nodes.get(1)).getValue());
        switch (node.getKind()) {
            case GREATER_THAN:
                argumentBuilder.startNot();
                argumentBuilder.lessThanEquals(column,pair.getKey(),pair.getValue());
                argumentBuilder.end();
                break;
            case GREATER_THAN_OR_EQUAL:
                argumentBuilder.startNot();
                argumentBuilder.lessThan(column,pair.getKey(),pair.getValue());
                argumentBuilder.end();
                break;
            case EQUALS:
                argumentBuilder.nullSafeEquals(column, pair.getKey(), pair.getValue());
                break;
            case LESS_THAN:
                argumentBuilder.lessThan(column, pair.getKey(), pair.getValue());
                break;
            case LESS_THAN_OR_EQUAL:
                argumentBuilder.lessThanEquals(column, pair.getKey(), pair.getValue());
                break;
            case BETWEEN:
                argumentBuilder.between(column, pair.getKey(), pair.getValue(),pair.getValue());
                break;
            case IN:
                argumentBuilder.in(column,pair.getKey(),super.segment.getInPartMap().get(column).stream().map(f-> returnWithType(columnMap.get(column),f)).collect(Collectors.toList()).toArray());
                break;
            case NOT_EQUALS:
                argumentBuilder.startNot();
                argumentBuilder.equals(column, pair.getKey(), pair.getValue());
                argumentBuilder.end();
                break;
            default:
                throw new OperationNotSupportException(" not supported!");

        }
    }
    private Pair<PredicateLeaf.Type,Object> returnType(String columnName,Object value){
        if(columnMap.containsKey(columnName)){
            return returnType(columnMap.get(columnName),value);
        }else {
            return returnType(columnMap.get(columnName.toUpperCase()),value);
        }
    }
    private Pair<PredicateLeaf.Type,Object> returnType(DataSetColumnMeta columnMeta,Object value){
        PredicateLeaf.Type type=null;
        Object targetVal=null;
        try {
            switch (columnMeta.getColumnType()) {
                case Const.META_TYPE_INTEGER:
                case Const.META_TYPE_BIGINT:
                    type = PredicateLeaf.Type.LONG;
                    targetVal = Long.parseLong(value.toString());
                    break;
                case Const.META_TYPE_DOUBLE:
                    type = PredicateLeaf.Type.FLOAT;
                    targetVal =Double.parseDouble(value.toString());
                    break;
                case Const.META_TYPE_DECIMAL:
                    type = PredicateLeaf.Type.DECIMAL;
                    targetVal =new HiveDecimalWritable(HiveDecimal.create(Double.parseDouble(value.toString())));
                    break;
                case Const.META_TYPE_DATE:
                    type = PredicateLeaf.Type.DATE;
                    targetVal = Long.parseLong(value.toString());
                    break;
                case Const.META_TYPE_TIMESTAMP:
                    type = PredicateLeaf.Type.TIMESTAMP;
                    if(Timestamp.class.isAssignableFrom(value.getClass())){
                        targetVal= value;
                    }else {
                        targetVal = new Timestamp(Long.parseLong(value.toString()));
                    }
                    break;
                default:
                    type = PredicateLeaf.Type.STRING;
                    targetVal = value.toString();
            }
        }catch (Exception ex){

        }
        return Pair.of(type,targetVal);
    }
    private Object returnWithType(DataSetColumnMeta columnMeta,Object value){
        Object targetVal=null;
        switch (columnMeta.getColumnType()) {
            case Const.META_TYPE_INTEGER:
            case Const.META_TYPE_BIGINT:
                targetVal = Long.parseLong(value.toString());
                break;
            case Const.META_TYPE_DOUBLE:
            case Const.META_TYPE_DECIMAL:
                targetVal = Double.parseDouble(value.toString());
                break;
            case Const.META_TYPE_DATE:
            case Const.META_TYPE_TIMESTAMP:
                targetVal = new Timestamp(Long.parseLong(value.toString()));
                break;
            default:
                targetVal = value.toString();
        }
        return targetVal;
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
                    readPath=new File(readPath).toURI().toString();
                }else {
                    instream = accessUtil.getRawInputStream(colmeta, ResourceUtil.getProcessPath(colmeta.getPath()));
                    long size = accessUtil.getInputStreamSize(colmeta, ResourceUtil.getProcessPath(colmeta.getPath()));
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
            if(!ObjectUtils.isEmpty(super.segment.getWhereCause()) &&!super.segment.isConditionHasFunction() && !super.segment.isConditionHasFunction() && !super.segment.isHasRightColumnCmp()){
                SearchArgument.Builder argumentBuilder=SearchArgumentFactory.newBuilder();
                walkCondition(super.segment.getWhereCause(),argumentBuilder);
                options= new Reader.Options().schema(schema).allowSARGToFilter(true)
                        .searchArgument(argumentBuilder.build(),columnList.toArray(new String[0]));
            }

            oreader =OrcFile.createReader(new Path(readPath),OrcFile.readerOptions(conf).filesystem(fs));
            //schema= oreader.getSchema();
            if(options!=null){
                rows=oreader.rows(options);
            }else {
                rows = oreader.rows();
            }
            fields=schema.getChildren();
            batch= schema.createRowBatch();

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
