package com.robin.comm.fileaccess.iterator;

import com.google.common.collect.Sets;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.robin.comm.fileaccess.util.*;
import com.robin.comm.utils.SysUtils;
import com.robin.core.base.exception.OperationNotSupportException;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.fileaccess.iterator.AbstractFileIterator;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;
import com.robin.core.fileaccess.util.AvroUtils;
import com.robin.core.fileaccess.util.ResourceUtil;
import com.robin.hadoop.hdfs.HDFSUtil;
import org.apache.avro.LogicalTypes;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.calcite.sql.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.flink.core.memory.MemorySegment;
import org.apache.flink.core.memory.MemorySegmentFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.ParquetReadOptions;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.filter2.compat.FilterCompat;
import org.apache.parquet.filter2.predicate.FilterApi;
import org.apache.parquet.filter2.predicate.FilterPredicate;
import org.apache.parquet.format.converter.ParquetMetadataConverter;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.metadata.ParquetMetadata;
import org.apache.parquet.hadoop.util.HadoopInputFile;
import org.apache.parquet.io.InputFile;
import org.apache.parquet.io.LocalInputFile;
import org.apache.parquet.io.api.Binary;
import org.apache.parquet.proto.ProtoParquetReader;
import org.apache.parquet.proto.ProtoReadSupport;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.Type;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static org.apache.parquet.filter2.predicate.FilterApi.*;


public class ParquetFileIterator extends AbstractFileIterator {
    private ParquetReader<GenericData.Record> preader;
    private ParquetReader<DynamicMessage.Builder> protoReader;
    private Schema schema;
    private MessageType msgtype;
    private GenericData.Record record;
    private ParquetReader<Map<String, Object>> ireader;
    private boolean useAvroEncode = false;
    private boolean useProtoBuffEncode=false;
    private MemorySegment segment;
    private Double allowOffHeapDumpLimit = ResourceConst.ALLOWOUFHEAPMEMLIMIT;
    private FilterCompat.Filter filter;
    private DynamicMessage message;
    private ProtoBufUtil.ProtoContainer container;


    public ParquetFileIterator() {
        identifier = Const.FILEFORMATSTR.PARQUET.getValue();
    }

    public ParquetFileIterator(DataCollectionMeta colmeta) {
        super(colmeta);
        identifier = Const.FILEFORMATSTR.PARQUET.getValue();
        if (!CollectionUtils.isEmpty(colmeta.getResourceCfgMap()) && colmeta.getResourceCfgMap().containsKey(ResourceConst.ALLOWOFFHEAPKEY)) {
            allowOffHeapDumpLimit = Double.parseDouble(colmeta.getResourceCfgMap().get(ResourceConst.ALLOWOFFHEAPKEY).toString());
        }
    }

    public ParquetFileIterator(DataCollectionMeta colmeta, AbstractFileSystemAccessor accessor) {
        super(colmeta);
        identifier = Const.FILEFORMATSTR.PARQUET.getValue();
        if (!CollectionUtils.isEmpty(colmeta.getResourceCfgMap()) && colmeta.getResourceCfgMap().containsKey(ResourceConst.ALLOWOFFHEAPKEY)) {
            allowOffHeapDumpLimit = Double.parseDouble(colmeta.getResourceCfgMap().get(ResourceConst.ALLOWOFFHEAPKEY).toString());
        }
        accessUtil = accessor;
    }

    private List<Schema.Field> fields;
    private File tmpFile;

    @Override
    public void beforeProcess() {
        Configuration conf;
        InputFile file;
        try {
            checkAccessUtil(null);
            if (colmeta.getResourceCfgMap().containsKey(ResourceConst.PARQUETFILEFORMAT)) {
                if(ResourceConst.PARQUETSUPPORTFORMAT.AVRO.getValue().equalsIgnoreCase(colmeta.getResourceCfgMap().get(ResourceConst.PARQUETFILEFORMAT).toString())) {
                    useAvroEncode = true;
                }else if(ResourceConst.PARQUETSUPPORTFORMAT.PROTOBUF.getValue().equalsIgnoreCase(colmeta.getResourceCfgMap().get(ResourceConst.PARQUETFILEFORMAT).toString())){
                    useProtoBuffEncode=true;
                    container=ProtoBufUtil.initSchema(colmeta);
                }
            }
            if (!ObjectUtils.isEmpty(super.segment) && !super.segment.isSelectHasFourOperations() && !super.segment.isHasRightColumnCmp()) {
                //FilterPredicate predicate=walkCondition(rootNode);
                FilterPredicate predicate=walkCondition(super.segment.getWhereCause());
                filter= FilterCompat.get(predicate);
            }

            if (Const.FILESYSTEM.HDFS.getValue().equals(colmeta.getFsType())) {
                conf = new HDFSUtil(colmeta).getConfig();
                file = HadoopInputFile.fromPath(new Path(colmeta.getPath()), conf);
                getSchema(file, false);
                if (useAvroEncode) {
                    ParquetReader.Builder<GenericData.Record>  avroBuilder= AvroParquetReader
                            .<GenericData.Record>builder(HadoopInputFile.fromPath(new Path(ResourceUtil.getProcessPath(colmeta.getPath())), conf)).withConf(conf);
                    if(filter==null) {
                       avroBuilder.withFilter(filter);
                    }
                    preader=avroBuilder.build();
                }else if(useProtoBuffEncode){
                    ParquetReader.Builder<DynamicMessage.Builder> protoBuilder=ProtoParquetReader.<DynamicMessage.Builder>builder(HadoopInputFile.fromPath(new Path(ResourceUtil.getProcessPath(colmeta.getPath())), conf))
                            .set(ProtoReadSupport.PB_CLASS,DynamicMessage.class.getName()).withConf(conf);
                    if(filter==null) {
                        protoBuilder.withFilter(filter);
                    }
                    protoReader=protoBuilder.build();
                }else {
                    ParquetReader.Builder<Map<String, Object>> builder = ParquetReader.builder(new CustomRowReadSupport(), new Path(ResourceUtil.getProcessPath(colmeta.getPath()))).withConf(conf);
                    if(filter==null) {
                        builder.withFilter(filter);
                    }
                    ireader = builder.build();
                }
            } else {
                // no hdfs input source
                if (Const.FILESYSTEM.LOCAL.getValue().equals(colmeta.getFsType())) {
                    file = new LocalInputFile(Paths.get(colmeta.getPath()));
                } else {
                    instream = accessUtil.getRawInputStream(colmeta, ResourceUtil.getProcessPath(colmeta.getPath()));
                    long size = accessUtil.getInputStreamSize(colmeta, ResourceUtil.getProcessPath(colmeta.getPath()));
                    Double freeMemory = SysUtils.getFreeMemory();
                    //file size too large ,can not store in ByteBuffer or freeMemory too low
                    if (size >= ResourceConst.MAX_ARRAY_SIZE  || freeMemory < allowOffHeapDumpLimit) {
                        String tmpPath = com.robin.core.base.util.FileUtils.getWorkingPath(colmeta);
                        String tmpFilePath = "file:///" + tmpPath + ResourceUtil.getProcessFileName(colmeta.getPath());
                        tmpFile = new File(new URL(tmpFilePath).toURI());
                        copyToLocal(tmpFile, instream);
                        file = new LocalInputFile(Paths.get(new URI(tmpFilePath)));
                    } else {
                        //use flink memory utils to use offHeapMemory to dump file content
                        segment = MemorySegmentFactory.allocateOffHeapUnsafeMemory((int) size, this, new Thread() {
                        });
                        ByteBuffer byteBuffer = segment.getOffHeapBuffer();
                        try (ReadableByteChannel channel = Channels.newChannel(instream)) {
                            IOUtils.readFully(channel, byteBuffer);
                            byteBuffer.position(0);
                            ByteBufferSeekableInputStream seekableInputStream = new ByteBufferSeekableInputStream(byteBuffer);
                            file = ParquetUtil.makeInputFile(seekableInputStream);
                        }
                    }
                }
                getSchema(file, true);

                if (useAvroEncode) {
                    ParquetReader.Builder<GenericData.Record> builder = AvroParquetReader.<GenericData.Record>builder(file);
                    if (filter != null) {
                        builder.withFilter(filter);
                    }
                    preader = builder.build();
                }else if(useProtoBuffEncode){
                    ParquetReader.Builder<DynamicMessage.Builder> builder=ProtoParquetReader.builder(file);
                    builder.set(ProtoReadSupport.PB_CLASS,DynamicMessage.class.getName()).set(ProtoReadSupport.PB_DESCRIPTOR,container.getSchema().toString());
                    if (filter != null) {
                        builder.withFilter(filter);
                    }
                    protoReader=builder.build();
                }
                else {
                    ParquetReader.Builder builder = CustomParquetReader.builder(file, colmeta);
                    if (filter != null) {
                        builder.withFilter(filter);
                    }
                    ireader = builder.build();
                }
            }
            fields = schema.getFields();
        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
        }
    }

    private void getSchema(InputFile file, boolean seekFrist) throws IOException {
        if (colmeta.getColumnList().isEmpty()) {
            ParquetReadOptions options = ParquetReadOptions.builder().withMetadataFilter(ParquetMetadataConverter.NO_FILTER).build();
            try (ParquetFileReader ireader = ParquetFileReader.open(file, options)) {
                ParquetMetadata meta = ireader.getFooter();
                msgtype = meta.getFileMetaData().getSchema();
                parseSchemaByType();
                if (seekFrist) {
                    file.newStream().seek(0L);
                }
            }
        } else {
            schema = AvroUtils.getSchemaFromMeta(colmeta);
        }
    }

    @Override
    public boolean hasNext() {
        if(!ObjectUtils.isEmpty(super.segment) && !super.segment.isSelectHasFourOperations() && !super.segment.isHasRightColumnCmp()){
            pullNext();
            return !CollectionUtils.isEmpty(cachedValue);
        }else{
            return super.hasNext();
        }
    }

    @Override
    protected void pullNext() {
        try {
            cachedValue.clear();
            if (useAvroEncode) {
                record = null;
                record = preader.read();
                if(record!=null){
                    for (Schema.Field field : fields) {
                        Object value = record.get(field.name());
                        if (LogicalTypes.timestampMillis().equals(field.schema().getLogicalType())) {
                            value = new Timestamp((Long) value);
                        }
                        cachedValue.put(field.name(), value);
                    }
                }
            }else if(useProtoBuffEncode){
                message=protoReader.read().build();
                if(message!=null){
                    for (Descriptors.FieldDescriptor descriptor : container.getSchema().getMessageDescriptor(colmeta.getValueClassName()).getFields()) {
                        cachedValue.put(descriptor.getName(), message.getField(descriptor));
                    }
                }
            }
            else {
                cachedValue = ireader.read();
            }
        } catch (Exception ex) {
            throw new OperationNotSupportException(ex);
        }
    }


    public Map<String, Object> next1() {
        if (useAvroEncode) {
            Map<String, Object> retMap = new HashMap<>();
            if (record == null) {
                throw new NoSuchElementException("");
            }
            for (Schema.Field field : fields) {
                Object value = record.get(field.name());
                if (LogicalTypes.timestampMillis().equals(field.schema().getLogicalType())) {
                    value = new Timestamp((Long) value);
                }
                retMap.put(field.name(), value);
            }
            return retMap;
        }else if(useProtoBuffEncode){
            return cachedValue;
        }else {
            return cachedValue;
        }
    }



    private FilterPredicate walkCondition(SqlNode node) {
        //condition has four operation or function call column,FilterPredicate can not perform,otherwise can use
        if(!super.segment.isConditionHasFunction() && !super.segment.isConditionHasFourOperations()) {
            if (SqlBasicCall.class.isAssignableFrom(node.getClass())) {
                List<SqlNode> nodes = ((SqlBasicCall) node).getOperandList();
                if (SqlIdentifier.class.isAssignableFrom(nodes.get(0).getClass()) && SqlLiteral.class.isAssignableFrom(nodes.get(1).getClass())) {
                    return parseOperator(node);
                } else {
                    FilterPredicate left = walkCondition(nodes.get(0));
                    FilterPredicate right = walkCondition(nodes.get(1));
                    if (SqlKind.OR.equals(node.getKind())) {
                        return or(left, right);
                    } else {
                        return and(left, right);
                    }
                }
            } else {
                return null;
            }
        }else {
            return null;
        }
    }
    private FilterPredicate parseOperator(SqlNode node){
        List<SqlNode> nodes=((SqlBasicCall)node).getOperandList();
        Object cmpValue=((SqlLiteral)nodes.get(1)).getValue();
        return parseOperator(nodes.get(0).toString(),node.getKind(),cmpValue);
    }
    private FilterPredicate parseOperator(String columnName, SqlKind operator, Object value) {
        FilterPredicate predicate;
        DataSetColumnMeta meta = columnMap.get(columnName);

        if (meta == null) {
            meta = columnMap.get(columnName.toUpperCase());
        }
        switch (operator) {
            case GREATER_THAN:
                if (Const.META_TYPE_INTEGER.equals(meta.getColumnType())) {
                    predicate = gt(intColumn(columnName), Integer.parseInt(value.toString()));
                } else if (Const.META_TYPE_DOUBLE.equals(meta.getColumnType()) || Const.META_TYPE_DECIMAL.equals(meta.getColumnType())) {
                    predicate = gt(doubleColumn(columnName), Double.parseDouble(value.toString()));
                } else if (Const.META_TYPE_BIGINT.equals(meta.getColumnType()) || Const.META_TYPE_TIMESTAMP.equals(meta.getColumnType())) {
                    predicate = gt(longColumn(columnName), Long.parseLong(value.toString()));
                } else {
                    throw new OperationNotSupportException("type not support");
                }
                break;
            case GREATER_THAN_OR_EQUAL:
                if (Const.META_TYPE_INTEGER.equals(meta.getColumnType())) {
                    predicate = gtEq(intColumn(columnName), Integer.parseInt(value.toString()));
                } else if (Const.META_TYPE_DOUBLE.equals(meta.getColumnType()) || Const.META_TYPE_DECIMAL.equals(meta.getColumnType())) {
                    predicate = gtEq(doubleColumn(columnName), Double.parseDouble(value.toString()));
                } else if (Const.META_TYPE_BIGINT.equals(meta.getColumnType()) || Const.META_TYPE_TIMESTAMP.equals(meta.getColumnType())) {
                    predicate = gtEq(longColumn(columnName), Long.parseLong(value.toString()));
                } else {
                    throw new OperationNotSupportException("type not support");
                }
                break;
            case EQUALS:
                if (Const.META_TYPE_INTEGER.equals(meta.getColumnType())) {
                    predicate = eq(intColumn(columnName), Integer.parseInt(value.toString()));
                } else if (Const.META_TYPE_DOUBLE.equals(meta.getColumnType()) || Const.META_TYPE_DECIMAL.equals(meta.getColumnType())) {
                    predicate = eq(doubleColumn(columnName), Double.parseDouble(value.toString()));
                } else if (Const.META_TYPE_BIGINT.equals(meta.getColumnType()) || Const.META_TYPE_TIMESTAMP.equals(meta.getColumnType())) {
                    predicate = eq(longColumn(columnName), Long.parseLong(value.toString()));
                } else if (Const.META_TYPE_STRING.equals(meta.getColumnType())) {
                    predicate = eq(binaryColumn(columnName), Binary.fromString(value.toString()));
                } else {
                    throw new OperationNotSupportException("type not support");
                }
                break;
            case LESS_THAN:
                if (Const.META_TYPE_INTEGER.equals(meta.getColumnType())) {
                    predicate = lt(intColumn(columnName), Integer.parseInt(value.toString()));
                } else if (Const.META_TYPE_DOUBLE.equals(meta.getColumnType()) || Const.META_TYPE_DECIMAL.equals(meta.getColumnType())) {
                    predicate = lt(doubleColumn(columnName), Double.parseDouble(value.toString()));
                } else if (Const.META_TYPE_BIGINT.equals(meta.getColumnType()) || Const.META_TYPE_TIMESTAMP.equals(meta.getColumnType())) {
                    predicate = lt(longColumn(columnName), Long.parseLong(value.toString()));
                } else {
                    throw new OperationNotSupportException("type not support");
                }
                break;
            case LESS_THAN_OR_EQUAL:
                if (Const.META_TYPE_INTEGER.equals(meta.getColumnType())) {
                    predicate = ltEq(intColumn(columnName), Integer.parseInt(value.toString()));
                } else if (Const.META_TYPE_DOUBLE.equals(meta.getColumnType()) || Const.META_TYPE_DECIMAL.equals(meta.getColumnType())) {
                    predicate = ltEq(doubleColumn(columnName), Double.parseDouble(value.toString()));
                } else if (Const.META_TYPE_BIGINT.equals(meta.getColumnType()) || Const.META_TYPE_TIMESTAMP.equals(meta.getColumnType())) {
                    predicate = ltEq(longColumn(columnName), Long.parseLong(value.toString()));
                } else {
                    throw new OperationNotSupportException("type not support");
                }
                break;
            case NOT_EQUALS:
                if (Const.META_TYPE_INTEGER.equals(meta.getColumnType())) {
                    predicate = notEq(intColumn(columnName), Integer.parseInt(value.toString()));
                } else if (Const.META_TYPE_DOUBLE.equals(meta.getColumnType()) || Const.META_TYPE_DECIMAL.equals(meta.getColumnType())) {
                    predicate = notEq(doubleColumn(columnName), Double.parseDouble(value.toString()));
                } else if (Const.META_TYPE_BIGINT.equals(meta.getColumnType()) || Const.META_TYPE_TIMESTAMP.equals(meta.getColumnType())) {
                    predicate = notEq(longColumn(columnName), Long.parseLong(value.toString()));
                } else {
                    throw new OperationNotSupportException("type not support");
                }
                break;
            case IN:
                if (Const.META_TYPE_INTEGER.equals(meta.getColumnType())) {
                    predicate = in(intColumn(columnName), Sets.newHashSet(super.segment.getInPartMap().get(columnName).stream().map(Integer::valueOf).collect(Collectors.toList())));
                } else if (Const.META_TYPE_DOUBLE.equals(meta.getColumnType()) || Const.META_TYPE_DECIMAL.equals(meta.getColumnType())) {
                    predicate = in(doubleColumn(columnName), Sets.newHashSet(super.segment.getInPartMap().get(columnName).stream().map(Double::valueOf).collect(Collectors.toList())));
                } else if (Const.META_TYPE_BIGINT.equals(meta.getColumnType()) || Const.META_TYPE_TIMESTAMP.equals(meta.getColumnType())) {
                    predicate = in(longColumn(columnName), Sets.newHashSet(super.segment.getInPartMap().get(columnName).stream().map(Long::valueOf).collect(Collectors.toList())));
                } else if (Const.META_TYPE_STRING.equals(meta.getColumnType())) {
                    predicate = in(binaryColumn(columnName), Sets.newHashSet(super.segment.getInPartMap().get(columnName).stream().map(Binary::fromString).collect(Collectors.toList())));
                } else {
                    throw new OperationNotSupportException("type not support");
                }
                break;
            case NOT_IN:
                if (Const.META_TYPE_INTEGER.equals(meta.getColumnType())) {
                    predicate = notIn(intColumn(columnName), Sets.newHashSet(super.segment.getInPartMap().get(columnName).stream().map(Integer::valueOf).collect(Collectors.toList())));
                } else if (Const.META_TYPE_DOUBLE.equals(meta.getColumnType()) || Const.META_TYPE_DECIMAL.equals(meta.getColumnType())) {
                    predicate = notIn(doubleColumn(columnName), Sets.newHashSet(super.segment.getInPartMap().get(columnName).stream().map(Double::valueOf).collect(Collectors.toList())));
                } else if (Const.META_TYPE_BIGINT.equals(meta.getColumnType()) || Const.META_TYPE_TIMESTAMP.equals(meta.getColumnType())) {
                    predicate = notIn(longColumn(columnName), Sets.newHashSet(super.segment.getInPartMap().get(columnName).stream().map(Long::valueOf).collect(Collectors.toList())));
                } else if (Const.META_TYPE_STRING.equals(meta.getColumnType())) {
                    predicate = notIn(binaryColumn(columnName), Sets.newHashSet(super.segment.getInPartMap().get(columnName).stream().map(Binary::fromString).collect(Collectors.toList())));
                } else {
                    throw new OperationNotSupportException("type not support");
                }
                break;
            case LIKE:
                if (Const.META_TYPE_STRING.equals(meta.getColumnType())){
                    predicate=FilterApi.userDefined(FilterApi.binaryColumn(columnName),new CharLikePredicate(value.toString()));
                }else {
                    throw new OperationNotSupportException("type not support");
                }
                break;

            default:
                throw new OperationNotSupportException(" not supported!");

        }
        return predicate;
    }




    private void parseSchemaByType() {

        List<Type> colList = msgtype.getFields();

        for (Type type : colList) {
            colmeta.addColumnMeta(type.getName(), ParquetReaderUtil.parseColumnType(type.asPrimitiveType()), null);
        }
        schema = AvroUtils.getSchemaFromMeta(colmeta);

    }


    public Schema getSchema() {
        return schema;
    }

    @Override
    public void remove() {
        try {
            if(!useFilter && !super.segment.isSelectHasFourOperations() && !super.segment.isHasRightColumnCmp()) {
                if (ireader != null) {
                    ireader.read();
                }
                if (preader != null) {
                    preader.read();
                }
                if (protoReader != null) {
                    protoReader.read();
                }
            }else{
                super.hasNext();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public MessageType getMessageType() {
        return msgtype;
    }

    @Override
    public void close() throws IOException {
        super.close();
        if (!ObjectUtils.isEmpty(ireader)) {
            ireader.close();
        }
        if (!ObjectUtils.isEmpty(preader)) {
            preader.close();
        }
        //free offHeap memory
        if (!ObjectUtils.isEmpty(segment)) {
            segment.free();
        }

        if (!ObjectUtils.isEmpty(tmpFile)) {
            FileUtils.deleteQuietly(tmpFile);
        }
    }
}
