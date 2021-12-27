package com.robin.comm.fileaccess.iterator;

import com.robin.comm.fileaccess.util.ParquetUtil;
import com.robin.core.fileaccess.iterator.AbstractFileIterator;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.AvroUtils;
import com.robin.hadoop.hdfs.HDFSUtil;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.format.converter.ParquetMetadataConverter;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.metadata.ParquetMetadata;
import org.apache.parquet.io.InputFile;
import org.apache.parquet.io.SeekableInputStream;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.Type;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;


public class ParquetStreamIterator extends AbstractFileIterator {
    private ParquetReader<GenericData.Record> preader;
    private Schema schema;
    private MessageType msgtype;
    private Configuration conf;
    private GenericData.Record record;
    public ParquetStreamIterator(DataCollectionMeta colmeta) {
        super(colmeta);
    }
    private List<Schema.Field> fields;
    private static final int COPY_BUFFER_SIZE = 8192;

    @Override
    public void init()  {
        conf=new HDFSUtil(colmeta).getConfigration();
        try {
            if (colmeta.getColumnList().isEmpty()) {
                ParquetMetadata meta = ParquetFileReader.readFooter(conf, new Path(colmeta.getPath()), ParquetMetadataConverter.NO_FILTER);
                msgtype = meta.getFileMetaData().getSchema();
                parseSchemaByType();
            } else {
                schema = AvroUtils.getSchemaFromMeta(colmeta);
            }
            //seek remote file to local tmp

            preader = AvroParquetReader
                    .<GenericData.Record>builder(ParquetUtil.makeInputFile(instream)).withConf(conf).build();
            fields = schema.getFields();
        }catch (Exception ex){
            logger.error("{0}",ex);
        }
    }

    @Override
    public boolean hasNext() {
        try {
            record = null;
            record =preader.read();
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return record !=null;
    }

    @Override
    public Map<String, Object> next() {
        Map<String,Object> retMap=new HashMap<String, Object>();
        if(record==null){
            throw new NoSuchElementException("");
        }
        for(Schema.Field field:fields){
            retMap.put(field.name(), record.get(field.name()));
        }
        return retMap;
    }
    private void parseSchemaByType(){
        List<Type> colList= msgtype.getFields();

        for(Type type:colList){
            colmeta.addColumnMeta(type.getName(),ParquetReaderUtil.parseColumnType(type.asPrimitiveType()),null);
        }
        schema= AvroUtils.getSchemaFromMeta(colmeta);
    }


    public Schema getSchema() {
        return schema;
    }

    @Override
    public void remove() {
        try {
            reader.read();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }



    public MessageType getMessageType(){
        return msgtype;
    }





}


