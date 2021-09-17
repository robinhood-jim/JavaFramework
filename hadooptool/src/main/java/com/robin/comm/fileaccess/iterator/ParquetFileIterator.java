package com.robin.comm.fileaccess.iterator;

import com.robin.core.base.util.Const;
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
import org.apache.parquet.hadoop.util.HadoopInputFile;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.PrimitiveType;
import org.apache.parquet.schema.Type;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ParquetFileIterator extends AbstractFileIterator {
    private ParquetReader<GenericData.Record> reader;
    private Schema schema;
    private MessageType msgtype;
    private Configuration conf;
    private GenericData.Record record;
    public ParquetFileIterator(DataCollectionMeta colmeta) {
        super(colmeta);
    }
    private List<Schema.Field> fields;

    @Override
    public void init(){
        conf=new HDFSUtil(colmeta).getConfigration();
        try {
            if (colmeta.getColumnList().isEmpty()) {
                ParquetMetadata meta = ParquetFileReader.readFooter(conf, new Path(colmeta.getPath()), ParquetMetadataConverter.NO_FILTER);
                msgtype = meta.getFileMetaData().getSchema();
                parseSchemaByType();
            } else {
                schema = AvroUtils.getSchemaFromMeta(colmeta);
            }
            reader = AvroParquetReader
                    .<GenericData.Record>builder(HadoopInputFile.fromPath(new Path(colmeta.getPath()),conf)).withConf(conf).build();
            fields = schema.getFields();
        }catch (Exception ex){
            logger.error("{}",ex);
        }
    }

    @Override
    public boolean hasNext() {
        try {
            record =reader.read();
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return record !=null;
    }

    @Override
    public Map<String, Object> next() {
        Map<String,Object> retMap=new HashMap<String, Object>();
        try{
            for(Schema.Field field:fields){
                retMap.put(field.name(), record.get(field.name()));
            }

        }catch (Exception ex){
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
