package com.robin.comm.fileaccess.writer;

import com.robin.core.base.util.Const;
import com.robin.core.base.util.FileUtils;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.AvroUtils;
import com.robin.core.fileaccess.writer.AbstractFileWriter;
import com.robin.hadoop.hdfs.HDFSUtil;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;

import javax.naming.OperationNotSupportedException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>Project:  frame</p>
 * <p>
 * <p>Description:com.robin.core.fileaccess.writer</p>
 * <p>
 * <p>Copyright: Copyright (c) 2018 create at 2018年07月25日</p>
 * <p>
 * <p>Company: zhcx_DEV</p>
 *
 * @author robinjim
 * @version 1.0
 */
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
        FileUtils.parseFileFormat(colmeta.getPath(),fileSuffix);
        Const.CompressType type= FileUtils.getFileCompressType(fileSuffix);
        if(type== Const.CompressType.COMPRESS_TYPE_GZ){
            codecName=CompressionCodecName.GZIP;
        }else if(type== Const.CompressType.COMPRESS_TYPE_LZO){
            codecName=CompressionCodecName.LZO;
        }else if(type==Const.CompressType.COMPRESS_TYPE_SNAPPY){
            codecName=CompressionCodecName.SNAPPY;
        }
        pwriter= AvroParquetWriter.builder(new Path(colmeta.getPath())).withSchema(schema).withCompressionCodec(codecName).withConf(new HDFSUtil(colmeta).getConfigration()).build();
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
