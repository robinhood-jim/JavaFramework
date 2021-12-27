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

import javax.naming.OperationNotSupportedException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ParquetFileWriter extends AbstractFileWriter {
    private Schema schema;
    private ParquetWriter pwriter;
    public ParquetFileWriter(DataCollectionMeta colmeta) {
        super(colmeta);
        schema= AvroUtils.getSchemaFromMeta(colmeta);
    }

    @Override
    public void beginWrite() throws IOException {

        CompressionCodecName codecName= CompressionCodecName.UNCOMPRESSED;
        List<String> fileSuffix=new ArrayList<>();
        FileUtils.parseFileFormat(getOutputPath(colmeta.getPath()),fileSuffix);
        Const.CompressType type= FileUtils.getFileCompressType(fileSuffix);
        if(type== Const.CompressType.COMPRESS_TYPE_GZ){
            codecName=CompressionCodecName.GZIP;
        }else if(type== Const.CompressType.COMPRESS_TYPE_LZO){
            codecName=CompressionCodecName.LZO;
        }else if(type==Const.CompressType.COMPRESS_TYPE_SNAPPY){
            codecName=CompressionCodecName.SNAPPY;
        }
        if(colmeta.getSourceType().equals(ResourceConst.InputSourceType.TYPE_HDFS.getValue())){
            pwriter= AvroParquetWriter.builder(new Path(colmeta.getPath())).withSchema(schema).withCompressionCodec(codecName).withConf(new HDFSUtil(colmeta).getConfigration()).build();
        }else{
            pwriter=AvroParquetWriter.builder(ParquetUtil.makeOutputFile(accessUtil,colmeta, ResourceUtil.getProcessPath(colmeta.getPath()))).withSchema(schema).build();
        }
    }

    @Override
    public void writeRecord(Map<String, ?> map) throws IOException, OperationNotSupportedException {
        GenericRecord record=new GenericData.Record(schema);

        for (int i = 0; i < colmeta.getColumnList().size(); i++) {
            String name = colmeta.getColumnList().get(i).getColumnName();
            Object value=getMapValueByMeta(map,name);
            if(value!=null){
                record.put(name, value);
            }
        }

        try {
            pwriter.write(record);
        }catch (IOException ex){
            logger.error("",ex);
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
            //pwriter.notify();
        }
    }


}
