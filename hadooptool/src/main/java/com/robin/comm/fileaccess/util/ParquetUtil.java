package com.robin.comm.fileaccess.util;

import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;
import com.robin.core.fileaccess.util.ResourceUtil;
import org.apache.parquet.io.InputFile;
import org.apache.parquet.io.OutputFile;
import org.apache.parquet.io.PositionOutputStream;
import org.apache.parquet.io.SeekableInputStream;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.OriginalType;
import org.apache.parquet.schema.PrimitiveType;
import org.apache.parquet.schema.Types;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.Nonnull;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.InputMismatchException;


public class ParquetUtil {
    private static final int COPY_BUFFER_SIZE = 8192;
    private static final int IO_BUF_SIZE = 16 * 1024;

    public static InputFile makeInputFile(com.robin.comm.fileaccess.util.SeekableInputStream instream) {
        return new InputFile() {
            @Override
            public long getLength() throws IOException {
                return instream.available();
            }

            @Override
            public org.apache.parquet.io.SeekableInputStream newStream() throws IOException {

                return new SeekableInputStream() {

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
                        return instream.getPos();
                    }

                    @Override
                    public void seek(long l) throws IOException {
                        instream.seek(l);
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

    public  static InputFile makeInputFile(SeekableInputStream inputStream,Long length){
        return new InputFile() {
            @Override
            public long getLength() throws IOException {
                return length;
            }

            @Override
            public SeekableInputStream newStream() throws IOException {
                return inputStream;
            }
        };
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

    public static OutputFile makeOutputFile(@NonNull AbstractFileSystemAccessor accessUtil, @NonNull DataCollectionMeta colmeta, @NonNull String filePath) {
        return new OutputFile() {
            @Override
            public PositionOutputStream create(long l) throws IOException {
                return makePositionOutputStream(accessUtil, colmeta, filePath, IO_BUF_SIZE);
            }

            @Override
            public PositionOutputStream createOrOverwrite(long l) throws IOException {
                return makePositionOutputStream(accessUtil, colmeta, filePath, IO_BUF_SIZE);
            }

            @Override
            public boolean supportsBlockSize() {
                return false;
            }

            @Override
            public long defaultBlockSize() {
                return 0;
            }
        };
    }

    private static PositionOutputStream makePositionOutputStream(@NonNull AbstractFileSystemAccessor accessUtil, DataCollectionMeta colmeta, @Nonnull String filePath, int ioBufSize)
            throws IOException {
        final OutputStream output = accessUtil.getRawOutputStream(colmeta, ResourceUtil.getProcessPath(colmeta.getPath()));
        return new PositionOutputStream() {
            private long position = 0;
            private boolean isClosed=false;

            @Override
            public void write(int b) throws IOException {
                output.write(b);
                position++;
            }

            @Override
            public void write(@Nonnull byte[] b) throws IOException {
                output.write(b);
                position += b.length;
            }

            @Override
            public void write(@Nonnull byte[] b, int off, int len) throws IOException {
                output.write(b, off, len);
                position += len;
            }

            @Override
            public void flush() throws IOException {
                if(!isClosed) {
                    output.flush();
                }
            }

            @Override
            public void close() throws IOException {
                if(!isClosed) {
                    output.close();
                    isClosed = true;
                }
            }

            @Override
            public long getPos() throws IOException {
                return position;
            }
        };
    }

    public static MessageType genSchema(DataCollectionMeta colmeta) {
        Assert.notNull(colmeta, "datacollectionMeta is null");
        Assert.isTrue(!CollectionUtils.isEmpty(colmeta.getColumnList()), "columns is null");
        Types.MessageTypeBuilder builder = Types.buildMessage();
        for (DataSetColumnMeta columnMeta : colmeta.getColumnList()) {
            switch (columnMeta.getColumnType()) {
                case Const.META_TYPE_SHORT:
                    builder.optional(PrimitiveType.PrimitiveTypeName.INT32).as(OriginalType.INT_16).named(columnMeta.getColumnName());
                    break;
                case Const.META_TYPE_INTEGER:
                    builder.optional(PrimitiveType.PrimitiveTypeName.INT32).as(OriginalType.INT_32).named(columnMeta.getColumnName());
                    break;
                case Const.META_TYPE_FLOAT:
                    builder.optional(PrimitiveType.PrimitiveTypeName.FLOAT).as(OriginalType.DECIMAL).named(columnMeta.getColumnName());
                    break;
                case Const.META_TYPE_DOUBLE:
                case Const.META_TYPE_DECIMAL:
                    builder.optional(PrimitiveType.PrimitiveTypeName.DOUBLE).as(OriginalType.DECIMAL).named(columnMeta.getColumnName());
                    break;
                case Const.META_TYPE_BIGINT:
                    builder.optional(PrimitiveType.PrimitiveTypeName.INT64).as(OriginalType.INT_64).named(columnMeta.getColumnName());
                    break;
                case Const.META_TYPE_DATE:
                    break;
                case Const.META_TYPE_TIMESTAMP:
                    builder.optional(PrimitiveType.PrimitiveTypeName.INT64).as(OriginalType.TIMESTAMP_MILLIS).named(columnMeta.getColumnName());
                    break;
                case Const.META_TYPE_STRING:
                    builder.optional(PrimitiveType.PrimitiveTypeName.BINARY).as(OriginalType.UTF8).named(columnMeta.getColumnName());
                    break;
                case Const.META_TYPE_BINARY:
                case Const.META_TYPE_BLOB:
                    builder.optional(PrimitiveType.PrimitiveTypeName.BINARY).named(columnMeta.getColumnName());
                    break;
                default:
                    throw new InputMismatchException("input type not support!");
            }
        }
        MessageType type=builder.named(colmeta.getValueClassName());
        return type;
    }
}
