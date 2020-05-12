package com.robin.comm.fileaccess.iterator;

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

/**
 * <p>Project:  frame</p>
 * <p>
 * <p>Description:com.robin.comm.fileaccess.iterator</p>
 * <p>
 * <p>Copyright: Copyright (c) 2018 create at 2018年12月18日</p>
 * <p>
 * <p>Company: zhcx_DEV</p>
 *
 * @author robinjim
 * @version 1.0
 */
public class ParquetStreamIterator extends AbstractFileIterator {
    private ParquetReader<GenericData.Record> reader;
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

            reader = AvroParquetReader
                    .<GenericData.Record>builder(makeInputFile()).withConf(conf).build();
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
    public InputFile makeInputFile(){
        return new InputFile() {
            @Override
            public long getLength() throws IOException {
                return instream.available();
            }

            @Override
            public SeekableInputStream newStream() throws IOException {

                return new SeekableInputStream(){

                    private final byte[] tmpBuf = new byte[COPY_BUFFER_SIZE];

                    @Override
                    public int read() throws IOException {
                        return instream.read();
                    }

                    @SuppressWarnings("NullableProblems")
                    @Override
                    public int read(byte[] b) throws IOException {
                        return instream.read(b);
                    }

                    @SuppressWarnings("NullableProblems")
                    @Override
                    public int read(byte[] b, int off, int len) throws IOException {
                        return instream.read(b, off, len);
                    }

                    @Override
                    public long skip(long n) throws IOException {
                        return instream.skip(n);
                    }

                    @Override
                    public int available() throws IOException {
                        return instream.available();
                    }

                    @Override
                    public void close() throws IOException {
                        instream.close();
                    }



                    @Override
                    public synchronized void mark(int readlimit) {
                        instream.mark(readlimit);

                    }

                    @Override
                    public synchronized void reset() throws IOException {
                       instream.reset();
                    }

                    @Override
                    public boolean markSupported() {
                        return true;
                    }

                    @Override
                    public long getPos() throws IOException {
                        return instream.available();
                    }

                    @Override
                    public void seek(long l) throws IOException {

                    }

                    @Override
                    public void readFully(byte[] bytes) throws IOException {
                        instream.read(bytes);
                    }

                    @Override
                    public void readFully(byte[] bytes, int i, int i1) throws IOException {
                        instream.read(bytes, i, i1);
                    }

                    @Override
                    public int read(ByteBuffer byteBuffer) throws IOException {
                        return readDirectBuffer(byteBuffer, tmpBuf, instream);
                    }

                    @Override
                    public void readFully(ByteBuffer byteBuffer) throws IOException {
                        readFullyDirectBuffer(byteBuffer, tmpBuf, instream);
                    }

                };
            }
        };
    }


    public MessageType getMessageType(){
        return msgtype;
    }




    private static int readDirectBuffer(ByteBuffer byteBufr, byte[] tmpBuf, InputStream rdr) throws IOException {
        int nextReadLength = Math.min(byteBufr.remaining(), tmpBuf.length);
        int totalBytesRead = 0;
        int bytesRead;

        while ((bytesRead = rdr.read(tmpBuf, 0, nextReadLength)) == tmpBuf.length) {
            byteBufr.put(tmpBuf);
            totalBytesRead += bytesRead;
            nextReadLength = Math.min(byteBufr.remaining(), tmpBuf.length);
        }

        if (bytesRead < 0) {
            // return -1 if nothing was read
            return totalBytesRead == 0 ? -1 : totalBytesRead;
        } else {
            // copy the last partial buffer
            byteBufr.put(tmpBuf, 0, bytesRead);
            totalBytesRead += bytesRead;
            return totalBytesRead;
        }
    }

    private static void readFullyDirectBuffer(ByteBuffer byteBufr, byte[] tmpBuf, InputStream rdr) throws IOException {
        int nextReadLength = Math.min(byteBufr.remaining(), tmpBuf.length);
        int bytesRead = 0;

        while (nextReadLength > 0 && (bytesRead = rdr.read(tmpBuf, 0, nextReadLength)) >= 0) {
            byteBufr.put(tmpBuf, 0, bytesRead);
            nextReadLength = Math.min(byteBufr.remaining(), tmpBuf.length);
        }

        if (bytesRead < 0 && byteBufr.remaining() > 0) {
            throw new EOFException("Reached the end of stream with " + byteBufr.remaining() + " bytes left to read");
        }
    }
}


