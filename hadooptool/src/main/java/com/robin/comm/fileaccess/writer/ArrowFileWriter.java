package com.robin.comm.fileaccess.writer;

import com.robin.comm.fileaccess.util.ArrowSchemaUtils;
import com.robin.core.base.exception.MissingConfigException;
import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;
import com.robin.core.fileaccess.util.ResourceUtil;
import com.robin.core.fileaccess.writer.AbstractFileWriter;
import org.apache.arrow.compression.CommonsCompressionFactory;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.*;
import org.apache.arrow.vector.compression.CompressionCodec;
import org.apache.arrow.vector.compression.CompressionUtil;
import org.apache.arrow.vector.compression.NoCompressionCodec;
import org.apache.arrow.vector.dictionary.DictionaryProvider;
import org.apache.arrow.vector.ipc.ArrowStreamWriter;
import org.apache.arrow.vector.ipc.message.IpcOption;
import org.apache.arrow.vector.types.pojo.Field;
import org.apache.arrow.vector.types.pojo.Schema;
import org.springframework.util.ObjectUtils;

import javax.naming.OperationNotSupportedException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.channels.Channels;
import java.sql.Timestamp;
import java.util.Map;

public class ArrowFileWriter extends AbstractFileWriter {
    private Schema schema;
    private ArrowStreamWriter streamWriter;
    private BufferAllocator allocator;
    private VectorSchemaRoot vectorSchemaRoot;

    private int batchSize = 10000;
    private int currentPos = 0;
    private StringBuilder builder = new StringBuilder();

    public ArrowFileWriter() {
        this.identifier = Const.FILEFORMATSTR.ARROW.getValue();
        useRawOutputStream=true;
    }

    public ArrowFileWriter(DataCollectionMeta colmeta) {
        super(colmeta);
        this.identifier = Const.FILEFORMATSTR.ARROW.getValue();
        useRawOutputStream=true;
    }

    @Override
    public void beginWrite() throws IOException {
        super.beginWrite();
        schema = ArrowSchemaUtils.getSchema(colmeta);
        allocator = new RootAllocator(Integer.MAX_VALUE);
        vectorSchemaRoot = VectorSchemaRoot.create(schema, allocator);
        Const.CompressType type= getCompressType();
        CompressionUtil.CodecType codecType= CompressionUtil.CodecType.NO_COMPRESSION;
        CompressionCodec.Factory factory=new NoCompressionCodec.Factory();
        switch (type){
            case COMPRESS_TYPE_LZ4:
                codecType= CompressionUtil.CodecType.LZ4_FRAME;
                factory=new CommonsCompressionFactory();
                break;
            case COMPRESS_TYPE_ZSTD:
                codecType= CompressionUtil.CodecType.ZSTD;
                factory=new CommonsCompressionFactory();
                break;
            default:
                throw new MissingConfigException("not supported!");

        }
        streamWriter = new ArrowStreamWriter(vectorSchemaRoot, new DictionaryProvider.MapDictionaryProvider(), Channels.newChannel(out), IpcOption.DEFAULT,factory,codecType);
        vectorSchemaRoot.allocateNew();
        streamWriter.start();
    }

    @Override
    public void finishWrite() throws IOException {

        if (streamWriter != null) {
            if (currentPos > 0) {
                vectorSchemaRoot.setRowCount(currentPos);
                streamWriter.writeBatch();
            }
            streamWriter.end();
            streamWriter.close();
            vectorSchemaRoot.close();
            allocator.close();
        }
    }

    @Override
    public void flush() throws IOException {

    }

    @Override
    public void writeRecord(Map<String, Object> map) throws IOException, OperationNotSupportedException {
        if (currentPos == batchSize) {
            System.out.println("new batch");
            vectorSchemaRoot.setRowCount(currentPos);
            streamWriter.writeBatch();
            vectorSchemaRoot.clear();
            currentPos = 0;
        }
        for (int i = 0; i < colmeta.getColumnList().size(); i++) {
            Field field = schema.getFields().get(i);
            writeValue(field, colmeta.getColumnList().get(i), currentPos, map);
        }
        currentPos++;
        vectorSchemaRoot.setRowCount(currentPos);
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public void writeValue(Field field, DataSetColumnMeta meta, int row, Map<String, Object> valueMap) throws UnsupportedEncodingException {
        FieldVector vectors = vectorSchemaRoot.getVector(field);
        if (!ObjectUtils.isEmpty(valueMap.get(meta.getColumnName()))) {
            if (IntVector.class.isAssignableFrom(vectors.getClass())) {
                IntVector intVector = (IntVector) vectors;
                if (Const.META_TYPE_INTEGER.equals(meta.getColumnType())) {
                    intVector.setSafe(row, (Integer) valueMap.get(meta.getColumnName()));
                }
            } else if (DecimalVector.class.isAssignableFrom(vectors.getClass())) {
                ((DecimalVector) vectors).setSafe(row, getValueByScale(valueMap.get(meta.getColumnName()).toString(), 2));
            } else if (BigIntVector.class.isAssignableFrom(vectors.getClass())) {
                BigIntVector bVector = (BigIntVector) vectors;
                if (Const.META_TYPE_BIGINT.equals(meta.getColumnType())) {
                    bVector.setSafe(row, (Long) valueMap.get(meta.getColumnName()));
                }
            } else if (TimeStampVector.class.isAssignableFrom(vectors.getClass())) {
                ((TimeStampVector) vectors).setSafe(row, ((Timestamp) valueMap.get(meta.getColumnName())).getTime());
            } else if (Const.META_TYPE_STRING.equals(meta.getColumnType())) {
                ((VarCharVector) vectors).setSafe(row, valueMap.get(meta.getColumnName()).toString().getBytes("utf8"));
            }
        }
    }

    private BigDecimal getValueByScale(String value, int scale) {
        if (builder.length() > 0) {
            builder.delete(0, builder.length());
        }
        builder.append(value);
        int pos = value.indexOf(".");
        if (value.length() - pos - scale + 1 > 0) {
            for (int i = 0; i < value.length() - pos - scale + 1; i++) {
                builder.append("0");
            }
        }
        return BigDecimal.valueOf(Double.valueOf(builder.toString()));
    }
}
