package com.robin.comm.fileaccess.fs;

import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.flink.core.memory.MemorySegment;
import org.springframework.util.ObjectUtils;

import java.io.*;

/**
 * Cloud Storage FileSystemAccessor Abstract super class,not singleton,must init individual
 */
@Slf4j
public abstract class AbstractCloudStorageFileSystemAccessor extends AbstractFileSystemAccessor {
    protected String bucketName;
    protected String tmpFilePath;

    protected MemorySegment segment;
    protected boolean useFileCache = false;

    public void init() {

    }

    @Override
    public synchronized Pair<BufferedReader, InputStream> getInResourceByReader(String resourcePath) throws IOException {
        InputStream inputStream = getInputStreamByConfig(resourcePath);
        return Pair.of(getReaderByPath(resourcePath, inputStream, colmeta.getEncode()), inputStream);
    }

    @Override
    public synchronized Pair<BufferedWriter, OutputStream> getOutResourceByWriter(String resourcePath) throws IOException {
        OutputStream outputStream = getOutputStream(resourcePath);
        return Pair.of(getWriterByPath(resourcePath, outputStream, colmeta.getEncode()), outputStream);
    }

    @Override
    public synchronized OutputStream getRawOutputStream(String resourcePath) throws IOException {
        return getOutputStream(resourcePath);
    }

    @Override
    public synchronized OutputStream getOutResourceByStream(String resourcePath) throws IOException {
        return getOutputStreamByPath(resourcePath, getOutputStream(resourcePath));
    }

    @Override
    public synchronized InputStream getInResourceByStream(String resourcePath) throws IOException {
        InputStream inputStream = getInputStreamByConfig(resourcePath);
        return getInputStreamByPath(resourcePath, inputStream);
    }

    @Override
    public synchronized InputStream getRawInputStream(String resourcePath) throws IOException {
        return getInputStreamByConfig(resourcePath);
    }

    @Override
    public synchronized void finishWrite(OutputStream outputStream) {
        if (!ObjectUtils.isEmpty(segment)) {
            segment.free();
            segment = null;
        }
        if (!ObjectUtils.isEmpty(tmpFilePath)) {
            FileUtils.deleteQuietly(new File(tmpFilePath));
        }

    }

    protected InputStream getInputStreamByConfig(String path) {
        return getObject(getBucketName(colmeta), path);
    }

    protected String getBucketName(DataCollectionMeta meta) {
        return !ObjectUtils.isEmpty(bucketName) ? bucketName : meta.getResourceCfgMap().get(ResourceConst.BUCKETNAME).toString();
    }

    /**
     * Cloud storage now only support ingest InputStream ,So use OffHeap ByteBuffer or use temp file to store data temporary
     *
     * @return
     * @throws IOException
     */
    protected abstract OutputStream getOutputStream(String path) throws IOException;


    protected String getContentType(DataCollectionMeta meta) {
        return !ObjectUtils.isEmpty(meta.getContent()) && !ObjectUtils.isEmpty(meta.getContent().getContentType()) ? meta.getContent().getContentType() : ResourceConst.DEFAULTCONTENTTYPE;
    }

    protected abstract boolean putObject(String bucketName, DataCollectionMeta meta, InputStream inputStream, long size) throws IOException;

    protected abstract InputStream getObject(String bucketName, String objectName);
}
