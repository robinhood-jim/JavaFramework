package com.robin.comm.fileaccess.fs.outputstream;

import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import org.apache.flink.core.memory.MemorySegment;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public abstract class AbstractSegmentOutputStream extends OutputStream {
    protected ByteBuffer buffer;
    protected MemorySegment segment;
    protected String bucketName;
    protected String path;
    protected int position;
    protected String uploadId;
    protected abstract void flushIfNecessary(boolean force);
    protected abstract void uploadPart();
    protected DataCollectionMeta meta;
    @Override
    public void write(int b) throws IOException {
        if (position >= buffer.capacity()) {
            flushIfNecessary(false);
        } else {
            buffer.put((byte) b);
            position += 1;
        }
    }
    @Override
    public void write(@NotNull byte[] b, int off, int len) throws IOException {
        int offset = off;
        int length = len;
        int size;
        while (length > (size = buffer.capacity() - position)) {
            buffer.put(b, offset, size);
            position += size;
            flushIfNecessary(false);
            offset += size;
            length -= size;
        }
        if (length > 0) {
            buffer.put(b, offset, length);
            position += length;
        }
    }
    protected void closeHeap(){
        if(!ObjectUtils.isEmpty(segment)){
            segment.free();
        }
    }
    protected String getContentType(DataCollectionMeta meta) {
        return !ObjectUtils.isEmpty(meta.getContent()) && !ObjectUtils.isEmpty(meta.getContent().getContentType()) ? meta.getContent().getContentType() : ResourceConst.DEFAULTCONTENTTYPE;
    }
}
