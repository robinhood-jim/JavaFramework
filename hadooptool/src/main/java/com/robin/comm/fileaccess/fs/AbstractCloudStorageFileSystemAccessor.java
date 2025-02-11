package com.robin.comm.fileaccess.fs;

import com.robin.core.fileaccess.util.ByteBufferInputStream;
import com.robin.core.fileaccess.util.ByteBufferOutputStream;
import com.robin.core.base.exception.MissingConfigException;
import com.robin.core.base.exception.OperationNotSupportException;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.ResourceUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.flink.core.memory.MemorySegment;
import org.apache.flink.core.memory.MemorySegmentFactory;
import org.springframework.util.ObjectUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

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
    public synchronized Pair<BufferedReader, InputStream> getInResourceByReader(DataCollectionMeta meta, String resourcePath) throws IOException {
        InputStream inputStream = getInputStreamByConfig(meta);
        return Pair.of(getReaderByPath(resourcePath, inputStream, meta.getEncode()), inputStream);
    }

    @Override
    public synchronized Pair<BufferedWriter, OutputStream> getOutResourceByWriter(DataCollectionMeta meta, String resourcePath) throws IOException {
        OutputStream outputStream = getOutputStream(meta);
        return Pair.of(getWriterByPath(meta.getPath(), outputStream, meta.getEncode()), outputStream);
    }

    @Override
    public synchronized OutputStream getRawOutputStream(DataCollectionMeta meta, String resourcePath) throws IOException {
        return getOutputStream(meta);
    }

    @Override
    public synchronized OutputStream getOutResourceByStream(DataCollectionMeta meta, String resourcePath) throws IOException {
        return getOutputStreamByPath(resourcePath, getOutputStream(meta));
    }

    @Override
    public synchronized InputStream getInResourceByStream(DataCollectionMeta meta, String resourcePath) throws IOException {
        InputStream inputStream = getInputStreamByConfig(meta);
        return getInputStreamByPath(resourcePath, inputStream);
    }

    @Override
    public synchronized InputStream getRawInputStream(DataCollectionMeta meta, String resourcePath) throws IOException {
        return getInputStreamByConfig(meta);
    }

    @Override
    public synchronized void finishWrite(DataCollectionMeta meta, OutputStream outputStream) {
        if (!ObjectUtils.isEmpty(segment)) {
            segment.free();
            segment = null;
        }
        if (!ObjectUtils.isEmpty(tmpFilePath)) {
            FileUtils.deleteQuietly(new File(tmpFilePath));
        }

    }

    protected InputStream getInputStreamByConfig(DataCollectionMeta meta) {
        return getObject(getBucketName(meta), meta.getPath());
    }

    protected String getBucketName(DataCollectionMeta meta) {
        return !ObjectUtils.isEmpty(bucketName) ? bucketName : meta.getResourceCfgMap().get(ResourceConst.BUCKETNAME).toString();
    }

    /**
     * Cloud storage now only support ingest InputStream ,So use OffHeap ByteBuffer or use temp file to store data temporary
     *
     * @param meta
     * @return
     * @throws IOException
     */
    protected abstract OutputStream getOutputStream(DataCollectionMeta meta) throws IOException;


    /*protected boolean uploadStorage(String bucketName, DataCollectionMeta meta, OutputStream outputStream) {
        try {
            if (!useFileCache) {
                ByteBufferOutputStream out1=(ByteBufferOutputStream) outputStream;
                out1.reset();
                ByteBufferInputStream inputStream = new ByteBufferInputStream(out1.getByteBuffer(),out1.getCount());
                return putObject(bucketName, meta, inputStream, out1.getCount());
            } else {
                outputStream.close();
                return putObject(bucketName, meta, Files.newInputStream(Paths.get(tmpFilePath)), Files.size(Paths.get(tmpFilePath)));
            }
        } catch (IOException ex) {
            log.error("{}", ex.getMessage());
        }finally {
            try {
                if (!ObjectUtils.isEmpty(outputStream)) {
                    outputStream.close();
                }
            }catch (IOException ex){

            }
        }
        return false;
    }*/

    protected String getContentType(DataCollectionMeta meta) {
        return !ObjectUtils.isEmpty(meta.getContent()) && !ObjectUtils.isEmpty(meta.getContent().getContentType()) ? meta.getContent().getContentType() : ResourceConst.DEFAULTCONTENTTYPE;
    }

    protected abstract boolean putObject(String bucketName, DataCollectionMeta meta, InputStream inputStream, long size) throws IOException;

    protected abstract InputStream getObject(String bucketName, String objectName);
}
