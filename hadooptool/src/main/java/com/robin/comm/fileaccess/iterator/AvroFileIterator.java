package com.robin.comm.fileaccess.iterator;

import com.robin.comm.utils.SysUtils;
import com.robin.core.base.exception.MissingConfigException;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.fileaccess.iterator.AbstractFileIterator;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.AvroUtils;
import com.robin.core.fileaccess.util.ResourceUtil;
import com.robin.hadoop.hdfs.HDFSUtil;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.FileReader;
import org.apache.avro.file.SeekableFileInput;
import org.apache.avro.file.SeekableInput;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.flink.core.memory.MemorySegment;
import org.apache.flink.core.memory.MemorySegmentFactory;
import org.apache.hadoop.fs.AvroFSInput;
import org.apache.hadoop.fs.FSDataInputStream;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AvroFileIterator extends AbstractFileIterator {
    private Schema schema;
    private FileReader<GenericRecord> fileReader;
    private Double allowOffHeapDumpLimit = ResourceConst.ALLOWOUFHEAPMEMLIMIT;
    private MemorySegment segment;

    public AvroFileIterator() {
        identifier = Const.FILEFORMATSTR.AVRO.getValue();
    }

    public AvroFileIterator(DataCollectionMeta colmeta) {
        super(colmeta);
        identifier = Const.FILEFORMATSTR.AVRO.getValue();
    }
    public AvroFileIterator(DataCollectionMeta colmeta, AbstractFileSystemAccessor accessor) {
        super(colmeta);
        identifier = Const.FILEFORMATSTR.AVRO.getValue();
        accessUtil=accessor;
    }

    private File tmpFile;
    private SeekableInput input = null;


    public boolean hasNext1() {
        try {
            return fileReader.hasNext();
        } catch (Exception ex) {
            logger.error("", ex);
        }
        return false;
    }

    @Override
    public void beforeProcess() {
        try {
            schema = AvroUtils.getSchemaFromMeta(colmeta);
            doInit(colmeta.getPath());
        } catch (Exception ex) {
            logger.error("Exception {0}", ex);
        }
    }


    private void doInit(String resourcePath) throws Exception {
        if (colmeta.getSourceType().equals(ResourceConst.IngestType.TYPE_HDFS.getValue())) {
            HDFSUtil util = new HDFSUtil(colmeta);
            instream = util.getHDFSDataByRawInputStream(ResourceUtil.getProcessPath(resourcePath));
            input = new AvroFSInput(new FSDataInputStream(instream), util.getHDFSFileSize(ResourceUtil.getProcessPath(resourcePath)));
        } else {
            // no hdfs input source
            if (!ResourceConst.IngestType.TYPE_LOCAL.getValue().equals(colmeta.getSourceType())) {
                instream = accessUtil.getRawInputStream(colmeta, ResourceUtil.getProcessPath(resourcePath));
                long size = accessUtil.getInputStreamSize(colmeta, ResourceUtil.getProcessPath(colmeta.getPath()));
                Double freeMemory = SysUtils.getFreeMemory();
                //file size too large ,can not store in ByteBuffer or freeMemory too low
                if (size >=ResourceConst.MAX_ARRAY_SIZE || freeMemory < allowOffHeapDumpLimit) {
                    String tmpPath = FileUtils.getTempDirectoryPath() + ResourceUtil.getProcessFileName(resourcePath);
                    tmpFile = new File(tmpPath);
                    copyToLocal(tmpFile, instream);
                    input = new SeekableFileInput(tmpFile);
                } else {
                    segment = MemorySegmentFactory.allocateOffHeapUnsafeMemory((int)size, this, new Thread() {});
                    ByteBuffer byteBuffer = segment.getOffHeapBuffer();
                    try (ReadableByteChannel channel = Channels.newChannel(instream)) {
                        IOUtils.readFully(channel, byteBuffer);
                        byteBuffer.position(0);
                    }
                    input = new SeekableByteBufferInputStream(segment.getOffHeapBuffer());
                }
            } else {
                tmpFile = new File(ResourceUtil.getProcessPath(colmeta.getPath()));
                input = new SeekableFileInput(tmpFile);
            }
        }
        Assert.notNull(input, "Seekable input is null");
        GenericDatumReader<GenericRecord> dreader = new GenericDatumReader<>(schema);
        fileReader = new DataFileReader<>(input, dreader);
        schema = fileReader.getSchema();
    }

    @Override
    protected void pullNext() {
        try{
            cachedValue.clear();
            if(fileReader.hasNext()){
                GenericRecord records = fileReader.next();
                List<Field> flist = schema.getFields();
                for (Field f : flist) {
                    if (!ObjectUtils.isEmpty(records.get(f.name()))) {
                        cachedValue.put(f.name(), records.get(f.name()).toString());
                    }
                }
            }
        }catch (Exception ex){
            throw new MissingConfigException(ex);
        }
    }

    public Map<String, Object> next1() {
        Map<String, Object> retmap = new HashMap<>();
        try {
            GenericRecord records = fileReader.next();
            List<Field> flist = schema.getFields();
            for (Field f : flist) {
                if (!ObjectUtils.isEmpty(records.get(f.name()))) {
                    retmap.put(f.name(), records.get(f.name()).toString());
                }
            }
        } catch (Exception ex) {
            logger.error("", ex);
        }
        return retmap;
    }

    public Schema getSchema() {
        return schema;
    }

    @Override
    public void remove() {
        try {
            if(!useFilter) {
                fileReader.next();
            }else{
                hasNext();
            }
        } catch (Exception ex) {
            logger.error("", ex);
        }
    }

    @Override
    public void close() throws IOException {
        if (fileReader != null) {
            fileReader.close();
        }
        if (!ObjectUtils.isEmpty(input)) {
            input.close();
        }
        if (!ObjectUtils.isEmpty(tmpFile)) {
            FileUtils.deleteQuietly(tmpFile);
        }
        super.close();
    }

    static class SeekableByteBufferInputStream extends InputStream implements SeekableInput {
        private final byte[] oneByte = new byte[1];

        private ByteBuffer byteBuffer;

        SeekableByteBufferInputStream(ByteBuffer byteBuffer) throws IOException {
            this.byteBuffer = byteBuffer;
        }

        public void seek(long p) throws IOException {
            if (p < 0L) {
                throw new IOException("Illegal seek: " + p);
            } else {
                byteBuffer.position((int) p);
            }
        }

        public long tell() throws IOException {
            return byteBuffer.position();
        }

        public long length() throws IOException {
            return byteBuffer.capacity();
        }

        public int read(byte[] b) throws IOException {
            return read(b, 0, b.length);
        }

        public int read(byte[] b, int off, int len) throws IOException {
            if (byteBuffer.remaining() == 0) {
                return -1;
            }
            if (len > byteBuffer.remaining()) {
                len = byteBuffer.remaining();
            }
            byteBuffer.get(b, off, len);
            return len;
        }

        public int read() throws IOException {
            int n = this.read(this.oneByte, 0, 1);
            return n == 1 ? this.oneByte[0] & 255 : n;
        }

        public long skip(long skip) throws IOException {
            long newPos = byteBuffer.position() + skip;
            if (newPos > byteBuffer.remaining()) {
                skip = byteBuffer.remaining();
            }
            byteBuffer.position(byteBuffer.position() + (int) skip);
            return skip;
        }

        public void close() throws IOException {
            super.close();
        }

        public int available() throws IOException {
            long remaining = length() - tell();
            return remaining > 2147483647L ? Integer.MAX_VALUE : (int) remaining;
        }
    }
}
