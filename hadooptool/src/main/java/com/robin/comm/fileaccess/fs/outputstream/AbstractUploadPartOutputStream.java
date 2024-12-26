package com.robin.comm.fileaccess.fs.outputstream;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.core.memory.MemorySegment;
import org.apache.flink.core.memory.MemorySegmentFactory;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Abstract partUpload OutputStream,Support sync or async uploadParts
 */
@Slf4j
public abstract class AbstractUploadPartOutputStream extends OutputStream {
    protected ByteBuffer buffer;
    protected MemorySegment segment;
    protected String bucketName;
    protected String path;
    protected int position;
    protected String uploadId;

    protected DataCollectionMeta meta;
    protected boolean doFlush = false;
    protected int partNum = 1;
    protected List<String> etags = new ArrayList<>();
    protected Map<Integer, String> etagsMap = new HashMap<>();
    protected String region;
    protected boolean asyncTag = false;
    protected ExecutorService executorService;
    protected ListeningExecutorService guavaExecutor;
    protected int uploadThread = ResourceConst.DEFAULTSTORAGEUPLOADTHREAD;
    protected List<ListenableFuture<Boolean>> futures = new ArrayList<>();

    protected void init() {
        if (!asyncTag && !ObjectUtils.isEmpty(meta) && !CollectionUtils.isEmpty(meta.getResourceCfgMap()) && !ObjectUtils.isEmpty(meta.getResourceCfgMap().get(ResourceConst.USEASYNCUPLOAD)) && ("true".equalsIgnoreCase(meta.getResourceCfgMap().get(ResourceConst.USEASYNCUPLOAD).toString()))) {
            asyncTag = true;
        }
        if (asyncTag) {
            if (!ObjectUtils.isEmpty(meta.getResourceCfgMap().get(ResourceConst.DEFAULTSTORAGEUPLOADTHREADKEY))) {
                uploadThread = Integer.parseInt(meta.getResourceCfgMap().get(ResourceConst.DEFAULTSTORAGEUPLOADTHREADKEY).toString());
            }
            executorService = Executors.newFixedThreadPool(uploadThread);
            guavaExecutor = MoreExecutors.listeningDecorator(executorService);
        }
        initHeap();
    }

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

    protected void closeHeap() {
        if (!ObjectUtils.isEmpty(segment)) {
            segment.free();
        }
    }

    protected void flushIfNecessary(boolean force) throws IOException {
        try {
            if (ObjectUtils.isEmpty(uploadId)) {
                initiateUpload();
            }
            if (position >= buffer.capacity() || force) {
                doUploadPart();
                buffer.clear();
                buffer.position(0);
                position = 0;
                partNum += 1;
            }
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }


    @Override
    public void close() throws IOException {
        String etag = null;
        try {
            if (!doFlush) {
                if (uploadId != null) {
                    if (position > 0) {
                        doUploadPart();
                        position = 0;
                    }
                    if (!asyncTag) {
                        etag = completeMultiUpload();
                        doFlush = true;
                    } else {
                        ListenableFuture<List<Boolean>> tags = Futures.allAsList(futures);
                        List<Boolean> status = tags.get();
                        if (status.stream().allMatch(f -> f)) {
                            etag = completeMultiUpload();
                            doFlush = true;
                        }
                    }

                } else {
                    etag = uploadSingle();
                    doFlush = true;
                }
                if (ObjectUtils.isEmpty(etag)) {
                    log.error("upload Failed");
                } else {
                    log.info("upload success with etag {}", etag);
                }
            }
        } catch (Exception ex) {
            throw new IOException(ex);
        } finally {
            closeHeap();
        }
    }

    private void doUploadPart() throws IOException {
        if (!asyncTag) {
            uploadPart();
        } else {
            if (!ObjectUtils.isEmpty(guavaExecutor)) {
                WeakReference<byte[]> writeBytesRef = new WeakReference<>(new byte[(int) position]);
                buffer.position(0);
                buffer.get(writeBytesRef.get(), 0, position);
                uploadAsync(writeBytesRef, partNum, position);
            }
        }
    }

    protected String getContentType(DataCollectionMeta meta) {
        return !ObjectUtils.isEmpty(meta.getContent()) && !ObjectUtils.isEmpty(meta.getContent().getContentType()) ? meta.getContent().getContentType() : ResourceConst.DEFAULTCONTENTTYPE;
    }

    protected void initHeap() {
        int initLength = !ObjectUtils.isEmpty(meta.getResourceCfgMap().get(ResourceConst.DEFAULTCACHEOFFHEAPSIZEKEY))
                ? Integer.parseInt(meta.getResourceCfgMap().get(ResourceConst.DEFAULTCACHEOFFHEAPSIZEKEY).toString()) : ResourceConst.DEFAULTCACHEOFFHEAPSIZE;
        segment = MemorySegmentFactory.allocateOffHeapUnsafeMemory(initLength, this, new Thread() {
        });
        buffer = segment.getOffHeapBuffer();
    }

    protected abstract void initiateUpload() throws IOException;

    protected abstract void uploadPart() throws IOException;

    protected abstract void uploadAsync(WeakReference<byte[]> writeBytesRef, int partNumber, int byteSize) throws IOException;

    protected abstract String completeMultiUpload() throws IOException;

    protected abstract String uploadSingle() throws IOException;

    abstract class AbstractUploadPartCallable implements Callable<Boolean> {
        protected int retryNum = 4;
        protected int partNumber;
        protected WeakReference<byte[]> content;
        protected int byteSize;

        AbstractUploadPartCallable(WeakReference<byte[]> content, int partNumber, int byteSize) {
            this.content = content;
            this.partNumber = partNumber;
            this.byteSize = byteSize;
        }

        @Override
        public Boolean call() throws Exception {
            boolean successTag = false;
            int tryNum = 0;

            while (!successTag && tryNum <= retryNum) {
                try {
                    successTag = uploadPartAsync();
                } catch (Exception ex) {
                    tryNum += 1;
                }
            }
            free();
            return successTag;
        }

        public void setPartNumber(int partNumber) {
            this.partNumber = partNumber;
        }

        protected void free() {

        }

        protected abstract boolean uploadPartAsync() throws IOException;
    }
}
