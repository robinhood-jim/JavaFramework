package com.robin.comm.fileaccess.writer;

import com.robin.comm.fileaccess.util.ParquetUtil;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.FileUtils;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.AvroUtils;
import com.robin.core.fileaccess.util.ResourceUtil;
import com.robin.core.fileaccess.writer.AbstractFileWriter;
import com.robin.hadoop.hdfs.HDFSUtil;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.apache.parquet.io.OutputFile;
import org.apache.parquet.io.PositionOutputStream;
import org.apache.parquet.schema.MessageType;

import javax.naming.OperationNotSupportedException;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ParquetFileWriter extends AbstractFileWriter {
    private Schema avroSchema;
    private ParquetWriter pwriter;
    private MessageType schema;
    private boolean useAvroEncode=false;
    public ParquetFileWriter(DataCollectionMeta colmeta) {
        super(colmeta);
        avroSchema= AvroUtils.getSchemaFromMeta(colmeta);
        schema=ParquetUtil.genSchema(colmeta);
    }

    @Override
    public void beginWrite() throws IOException {

        CompressionCodecName codecName;
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
        if(colmeta.getSourceType().equals(ResourceConst.InputSourceType.TYPE_HDFS.getValue())){
            if(useAvroEncode) {
                pwriter = AvroParquetWriter.builder(new Path(colmeta.getPath())).withSchema(avroSchema).withCompressionCodec(codecName).withConf(new HDFSUtil(colmeta).getConfigration()).build();
            }else {
                pwriter = new CustomParquetWriter(new Path(colmeta.getPath()), schema, true, codecName);
            }
        }else{
            if(useAvroEncode) {
                pwriter = AvroParquetWriter.builder(ParquetUtil.makeOutputFile(accessUtil, colmeta, ResourceUtil.getProcessPath(colmeta.getPath()))).withCompressionCodec(codecName).withSchema(avroSchema).build();
            }else {
                pwriter = new CustomParquetWriter.Builder<Map<String, Object>>(ParquetUtil.makeOutputFile(accessUtil, colmeta, ResourceUtil.getProcessPath(colmeta.getPath())), schema).withCompressionCodec(codecName).build();
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
                pwriter.write(record);
            } catch (IOException ex) {
                logger.error("", ex);
            }
        }else {
            pwriter.write(map);
        }
    }

    @Override
    public void writeRecord(List<Object> map) throws IOException,OperationNotSupportedException {

    }

    @Override
    public void finishWrite() throws IOException {
        if(pwriter!=null){
            pwriter.close();
        }
    }

    @Override
    public void flush() throws IOException {
        if(pwriter!=null){

        }
    }


}
