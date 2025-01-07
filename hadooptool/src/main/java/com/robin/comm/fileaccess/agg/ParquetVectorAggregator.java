package com.robin.comm.fileaccess.agg;

import com.robin.comm.dal.pool.ResourceAccessHolder;
import com.robin.comm.fileaccess.iterator.ParquetReaderUtil;
import com.robin.comm.fileaccess.util.ByteBufferSeekableInputStream;
import com.robin.comm.fileaccess.util.ParquetUtil;
import com.robin.comm.fileaccess.util.ProtoBufUtil;
import com.robin.comm.sql.CommSqlParser;
import com.robin.comm.sql.SqlSegment;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.AvroUtils;
import com.robin.core.fileaccess.util.ResourceUtil;
import com.robin.hadoop.hdfs.HDFSUtil;
import lombok.Data;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.calcite.config.Lex;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlNode;
import org.apache.commons.io.IOUtils;
import org.apache.flink.core.memory.MemorySegment;
import org.apache.flink.core.memory.MemorySegmentFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.ParquetReadOptions;
import org.apache.parquet.avro.CustomAvroRecordMaterializer;
import org.apache.parquet.column.ColumnReadStore;
import org.apache.parquet.column.ColumnReader;
import org.apache.parquet.column.impl.ColumnReadStoreImpl;
import org.apache.parquet.column.page.PageReadStore;
import org.apache.parquet.filter2.compat.FilterCompat;
import org.apache.parquet.format.converter.ParquetMetadataConverter;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.metadata.BlockMetaData;
import org.apache.parquet.hadoop.metadata.ParquetMetadata;
import org.apache.parquet.hadoop.util.HadoopInputFile;
import org.apache.parquet.io.*;
import org.apache.parquet.io.api.RecordMaterializer;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.Type;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParquetVectorAggregator implements Closeable {
    private DataCollectionMeta colmeta;
    private AbstractFileSystemAccessor accessUtil;
    private InputFile file;
    private Configuration conf;
    private InputStream instream;
    private MemorySegment segment;
    private ParquetFileReader reader;
    private MessageColumnIO columnIO;
    private MessageType msgtype;
    private Schema schema;
    private boolean useAvroEncode=false;
    private boolean useProtoBuffEncode=false;
    private FilterCompat.Filter filter;
    private ProtoBufUtil.ProtoContainer container;
    private Aggregator aggregator;
    private String filterSql;
    private SqlSegment sqlSegment;
    private List<String> groupColumns;
    private Map<String,Integer> colPosMap=new HashMap<>();

    public ParquetVectorAggregator(DataCollectionMeta colmeta) {
        this.colmeta=colmeta;
        for(int i=0;i<colmeta.getColumnList().size();i++){
            colPosMap.put(colmeta.getColumnList().get(i).getColumnName(),i);
        }
        checkAccessUtil(null);
    }
    public ParquetVectorAggregator(DataCollectionMeta colmeta, AbstractFileSystemAccessor accessor) {
        this.colmeta=colmeta;
        this.accessUtil=accessor;
    }
    protected void checkAccessUtil(String inputPath) {
        try {
            if (colmeta.getResourceCfgMap().containsKey(ResourceConst.PARQUETFILEFORMAT)) {
                if(ResourceConst.PARQUETSUPPORTFORMAT.AVRO.getValue().equalsIgnoreCase(colmeta.getResourceCfgMap().get(ResourceConst.PARQUETFILEFORMAT).toString())) {
                    useAvroEncode = true;
                }else if(ResourceConst.PARQUETSUPPORTFORMAT.PROTOBUF.getValue().equalsIgnoreCase(colmeta.getResourceCfgMap().get(ResourceConst.PARQUETFILEFORMAT).toString())){
                    useProtoBuffEncode=true;
                    container= ProtoBufUtil.initSchema(colmeta);
                }
            }
            if (accessUtil == null) {
                URI uri = new URI(StringUtils.isEmpty(inputPath) ? colmeta.getPath() : inputPath);
                String schema = !ObjectUtils.isEmpty(colmeta.getFsType())?colmeta.getFsType():uri.getScheme();
                accessUtil = ResourceAccessHolder.getAccessUtilByProtocol(schema.toLowerCase());
            }
        } catch (Exception ex) {

        }
    }
    public void beforeProcess() {
        try {
            if (Const.FILESYSTEM.HDFS.getValue().equals(colmeta.getFsType())) {
                conf = new HDFSUtil(colmeta).getConfig();
                file = HadoopInputFile.fromPath(new Path(colmeta.getPath()), conf);
            }else{
                if (Const.FILESYSTEM.LOCAL.getValue().equals(colmeta.getFsType())) {
                    file = new LocalInputFile(Paths.get(colmeta.getPath()));
                } else {
                    instream = accessUtil.getRawInputStream(colmeta, ResourceUtil.getProcessPath(colmeta.getPath()));
                    long size = accessUtil.getInputStreamSize(colmeta, ResourceUtil.getProcessPath(colmeta.getPath()));
                    segment= MemorySegmentFactory.allocateOffHeapUnsafeMemory((int)size,this,new Thread(){});
                    ByteBuffer byteBuffer=segment.getOffHeapBuffer();
                    try(ReadableByteChannel channel= Channels.newChannel(instream)) {
                        IOUtils.readFully(channel, byteBuffer);
                        byteBuffer.position(0);
                        ByteBufferSeekableInputStream seekableInputStream = new ByteBufferSeekableInputStream(byteBuffer);
                        file = ParquetUtil.makeInputFile(seekableInputStream);

                    }
                }
            }
            if(aggregator!=null && !ObjectUtils.isEmpty(aggregator.getAggExpression())){

            }
            ParquetReadOptions options = ParquetReadOptions.builder().withMetadataFilter(ParquetMetadataConverter.NO_FILTER).build();
            reader=ParquetFileReader.open(file,options);
            ParquetMetadata meta = reader.getFooter();
            List<BlockMetaData> list1=meta.getBlocks();
            msgtype = meta.getFileMetaData().getSchema();
            parseSchemaByType();
            columnIO=new ColumnIOFactory().getColumnIO(msgtype);

        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
    public void withFilterSql(String filterSql){
        this.filterSql=filterSql;
        sqlSegment=CommSqlParser.parseGroupByAgg(filterSql, Lex.MYSQL,colmeta,"N_COLUMN");
        if(!ObjectUtils.isEmpty(sqlSegment.getGroupBy())){
            sqlSegment.getGroupBy().stream().map(node->groupColumns.add(node.toString()));
        }
    }

    public void setAggregator(Aggregator aggregator) {
        this.aggregator = aggregator;
    }

    public void read(){
        try {
            if (!ObjectUtils.isEmpty(reader)) {
                PageReadStore store;
                RecordMaterializer materializer=null;
                materializer=new CustomAvroRecordMaterializer<>(msgtype, schema, GenericData.get());
                while((store = reader.readNextRowGroup())!=null){
                    long rowCount = store.getRowCount();
                    System.out.println(store.getRowIndexOffset().get());

                    ColumnReadStore store1=new ColumnReadStoreImpl(store,materializer.getRootConverter(),msgtype,"");
                    ColumnReader reader1=store1.getColumnReader(msgtype.getColumns().get(0));

                    for(int i=0;i<rowCount;i++){
                        System.out.println(reader1.getInteger());
                        reader1.consume();
                    }

                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
    private void readColumnGroup(CommSqlParser.ValueParts parts,ColumnReadStore store){
        if(SqlKind.IDENTIFIER.equals(parts.getSqlKind())){

        }
    }
    private void parseSchemaByType() {

        List<Type> colList = msgtype.getFields();

        for (Type type : colList) {
            colmeta.addColumnMeta(type.getName(), ParquetReaderUtil.parseColumnType(type.asPrimitiveType()), null);
        }
        schema = AvroUtils.getSchemaFromMeta(colmeta);

    }

    @Override
    public void close() throws IOException {
        if(segment!=null){
            segment.free();
        }
        if(reader!=null){
            reader.close();
        }
        if(instream!=null){
            instream.close();
        }

    }
    @Data
    public class Aggregator{
        private List<String> groupBy;
        private List<String> aggExpression;
        private String querySql;
    }
}
