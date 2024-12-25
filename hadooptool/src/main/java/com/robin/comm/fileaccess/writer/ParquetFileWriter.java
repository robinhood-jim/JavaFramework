package com.robin.comm.fileaccess.writer;

import com.robin.comm.fileaccess.util.ParquetUtil;
import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.AvroUtils;
import com.robin.core.fileaccess.util.ResourceUtil;
import com.robin.core.fileaccess.writer.AbstractFileWriter;
import com.robin.hadoop.hdfs.HDFSUtil;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.column.ParquetProperties;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.apache.parquet.hadoop.util.HadoopOutputFile;
import org.apache.parquet.io.OutputFile;
import org.apache.parquet.schema.MessageType;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.naming.OperationNotSupportedException;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;


public class ParquetFileWriter extends AbstractFileWriter {
    private Schema avroSchema;
    //private ParquetWriter pwriter;
    private ParquetWriter<GenericRecord> avroWriter;
    private ParquetWriter<Map<String,Object>> mapWriter;
    private MessageType schema;
    private boolean useAvroEncode=false;
    private ParquetProperties.WriterVersion writerVersion=ParquetProperties.WriterVersion.PARQUET_1_0;
    private int pageSize=ParquetWriter.DEFAULT_PAGE_SIZE;
    public static final String PAGESIZECOLUMN="parquet.Pagesize";
    public static final String WRITEVERSION="parquet.writeVersion";

    private CompressionCodecName codecName;
    public ParquetFileWriter(){
        this.identifier= Const.FILEFORMATSTR.PARQUET.getValue();
    }
    public ParquetFileWriter(DataCollectionMeta colmeta) {
        super(colmeta);
        avroSchema= AvroUtils.getSchemaFromMeta(colmeta);
        schema=ParquetUtil.genSchema(colmeta);
        this.identifier= Const.FILEFORMATSTR.PARQUET.getValue();
        try {
            if (!CollectionUtils.isEmpty(colmeta.getResourceCfgMap())) {
                if (!ObjectUtils.isEmpty(colmeta.getResourceCfgMap().get(PAGESIZECOLUMN))) {
                    pageSize = Integer.parseInt(colmeta.getResourceCfgMap().get(PAGESIZECOLUMN).toString());
                }
                if (!ObjectUtils.isEmpty(colmeta.getResourceCfgMap().get(WRITEVERSION)) && "2".equals(colmeta.getResourceCfgMap().get(WRITEVERSION).toString())) {
                    writerVersion= ParquetProperties.WriterVersion.PARQUET_2_0;
                }
            }
        }catch (Exception ex){

        }

    }

    @Override
    public void beginWrite() throws IOException {
        Const.CompressType type= getCompressType();
        switch (type){
            case COMPRESS_TYPE_GZ:
                codecName=CompressionCodecName.GZIP;
                break;
            case COMPRESS_TYPE_BZ2:
                throw new IOException("parquet does not support bzip2 compression");
            case COMPRESS_TYPE_LZO:
                codecName=CompressionCodecName.LZO;
                break;
            case COMPRESS_TYPE_SNAPPY:
                codecName=CompressionCodecName.SNAPPY;
                break;
            case COMPRESS_TYPE_ZIP:
                throw new IOException("parquet does not support gzip compression");
            case COMPRESS_TYPE_LZ4:
                codecName=CompressionCodecName.LZ4;
                break;
            case COMPRESS_TYPE_LZMA:
                throw new IOException("parquet does not support lzma compression");
            case COMPRESS_TYPE_ZSTD:
                codecName=CompressionCodecName.ZSTD;
                break;
            case COMPRESS_TYPE_BROTLI:
                codecName=CompressionCodecName.BROTLI;
                break;
            case COMPRESS_TYPE_XZ:
                throw new IOException("parquet does not support xz compression");
            default:
                codecName=CompressionCodecName.UNCOMPRESSED;
        }

        if(out==null) {
            checkAccessUtil(null);
        }
        if(colmeta.getResourceCfgMap().containsKey("file.useAvroEncode") && "true".equalsIgnoreCase(colmeta.getResourceCfgMap().get("file.useAvroEncode").toString())){
            useAvroEncode=true;
        }
        if(Const.FILESYSTEM.HDFS.getValue().equals(colmeta.getFsType())){
            if(useAvroEncode) {
                Configuration conf=new HDFSUtil(colmeta).getConfig();
                OutputFile outputFile= HadoopOutputFile.fromPath(new Path(colmeta.getPath()),conf);
                avroWriter = AvroParquetWriter.<GenericRecord>builder(outputFile).withSchema(avroSchema).withCompressionCodec(codecName).withConf(conf).build();
            }else {
                mapWriter =new CustomParquetWriter.Builder(new Path(colmeta.getPath()), schema).withPageSize(pageSize).withCompressionCodec(codecName).withDictionaryEncoding(false).withWriterVersion(writerVersion).build();
            }
        }else{
            out=accessUtil.getRawOutputStream(colmeta, ResourceUtil.getProcessPath(colmeta.getPath()));
            if(useAvroEncode) {
                avroWriter = AvroParquetWriter.<GenericRecord>builder(ParquetUtil.makeOutputFile(out, colmeta, ResourceUtil.getProcessPath(colmeta.getPath()))).withCompressionCodec(codecName).withSchema(avroSchema).build();
            }else {
                mapWriter = new CustomParquetWriter.Builder<Map<String, Object>>(ParquetUtil.makeOutputFile(out, colmeta, ResourceUtil.getProcessPath(colmeta.getPath())), schema).withPageSize(pageSize).withCompressionCodec(codecName).withDictionaryEncoding(false).withWriterVersion(writerVersion).build();
            }
        }
    }


    @Override
    public void writeRecord(Map<String, Object> map) throws IOException, OperationNotSupportedException {

        if(useAvroEncode) {
            GenericRecord record = new GenericData.Record(avroSchema);
            for (int i = 0; i < colmeta.getColumnList().size(); i++) {
                String name = colmeta.getColumnList().get(i).getColumnName();
                Object value = getMapValueByMeta(map, name);
                if (value != null) {
                    if (Timestamp.class.isAssignableFrom(value.getClass())) {
                        record.put(name, ((Timestamp) value).getTime());
                    } else {
                        record.put(name, value);
                    }
                }
            }
            try {
                avroWriter.write(record);
            } catch (IOException ex) {
                logger.error("", ex);
            }
        }else {
            mapWriter.write(map);
        }
    }

    @Override
    public void writeRecord(List<Object> map) throws IOException,OperationNotSupportedException {

    }


    @Override
    public void finishWrite() throws IOException {
        if(avroWriter!=null){
            avroWriter.close();
        }
        if(mapWriter!=null){
            mapWriter.close();
        }
    }

    @Override
    public void flush() throws IOException {

    }


}
