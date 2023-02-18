package com.robin.comm.fileaccess.iterator;

import com.robin.comm.fileaccess.util.ParquetUtil;
import com.robin.comm.fileaccess.util.SeekableInputStream;
import com.robin.core.base.util.IOUtils;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.iterator.AbstractFileIterator;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.AvroUtils;
import com.robin.core.fileaccess.util.ResourceUtil;
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
import org.apache.parquet.schema.Type;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;


public class ParquetFileIterator extends AbstractFileIterator {
    private ParquetReader<GenericData.Record> preader;
    private Schema schema;
    private MessageType msgtype;
    private Configuration conf;
    private GenericData.Record record;
    private ParquetReader<Map> ireader;
    private boolean useAvroEncode=false;

    public ParquetFileIterator(DataCollectionMeta colmeta) {
        super(colmeta);
    }

    private List<Schema.Field> fields;
    Map<String, Object> rsMap;
    @Override
    public void init() {

        try {
            checkAccessUtil(null);
            if(colmeta.getResourceCfgMap().containsKey("file.useAvroEncode") && "true".equalsIgnoreCase(colmeta.getResourceCfgMap().get("file.useAvroEncode").toString())){
                useAvroEncode=true;
            }
            if(colmeta.getSourceType().equals(ResourceConst.InputSourceType.TYPE_HDFS.getValue())){
                conf = new HDFSUtil(colmeta).getConfigration();
                if (colmeta.getColumnList().isEmpty()) {
                    ParquetMetadata meta = ParquetFileReader.readFooter(conf, new Path(colmeta.getPath()), ParquetMetadataConverter.NO_FILTER);
                    msgtype = meta.getFileMetaData().getSchema();
                    parseSchemaByType();
                } else {
                    schema = AvroUtils.getSchemaFromMeta(colmeta);
                }
                if(!useAvroEncode) {
                    ireader = new ParquetReader<Map>(conf, new Path(ResourceUtil.getProcessPath(colmeta.getPath())), new CustomReadSupport(colmeta));
                }else {
                    preader = AvroParquetReader
                            .<GenericData.Record>builder(HadoopInputFile.fromPath(new Path(ResourceUtil.getProcessPath(colmeta.getPath())), conf)).withConf(conf).build();
                }
            }else{
                instream = accessUtil.getRawInputStream(colmeta, ResourceUtil.getProcessPath(colmeta.getPath()));
                ByteArrayOutputStream byteout=new ByteArrayOutputStream(instream.available());
                IOUtils.copyBytes(instream,byteout,8000);
                SeekableInputStream seekableInputStream=new SeekableInputStream(byteout.toByteArray());
                if (colmeta.getColumnList().isEmpty()) {
                    ParquetMetadata meta = ParquetFileReader.readFooter(ParquetUtil.makeInputFile(seekableInputStream), ParquetMetadataConverter.NO_FILTER);
                    msgtype = meta.getFileMetaData().getSchema();
                    //read footer and schema,must return header
                    seekableInputStream.seek(0L);
                } else {
                    schema = AvroUtils.getSchemaFromMeta(colmeta);
                }
                if(!useAvroEncode) {
                    parseSchemaByType();
                    fields = schema.getFields();
                    ireader = CustomParquetReader.builder(ParquetUtil.makeInputFile(seekableInputStream), colmeta).build();
                }else {
                    preader = AvroParquetReader.<GenericData.Record>builder(ParquetUtil.makeInputFile(seekableInputStream)).build();
                }
            }

        } catch (Exception ex) {
            logger.error("{0}", ex);
        }
    }

    @Override
    public boolean hasNext() {
        try {
            if(useAvroEncode) {
                record = null;
                record = preader.read();
                return record != null;
            }else{
                rsMap=ireader.read();
                return rsMap!=null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public Map<String, Object> next() {
        if(useAvroEncode) {
            Map<String, Object> retMap = new HashMap<>();
            if (record == null) {
                throw new NoSuchElementException("");
            }
            for (Schema.Field field : fields) {
                retMap.put(field.name(), record.get(field.name()));
            }
            return retMap;
        }else{
            return rsMap;
        }
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
            reader.read();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public MessageType getMessageType() {
        return msgtype;
    }
}
