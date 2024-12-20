package com.robin.core.fileaccess.fs;

import com.robin.core.fileaccess.meta.DataCollectionMeta;
import org.apache.commons.lang3.tuple.Pair;

import java.io.*;

/**
 *  All FileSystem accessor interface,Extends By Local/VFS/HDFS and cloud storage supported.
 */
public interface IFileSystemAccessor {
    /**
     * get BufferedReader or inputStream with compress format
     * @param meta
     * @param resourcePath
     * @return Pair(BufferedReader,InputStream)
     * @throws IOException
     */
    Pair<BufferedReader,InputStream> getInResourceByReader(DataCollectionMeta meta, String resourcePath) throws IOException;

    /**
     * get BufferedWriter or OutputStream with compress format
     * @param meta
     * @param resourcePath
     * @return
     * @throws IOException
     */
    Pair<BufferedWriter,OutputStream> getOutResourceByWriter(DataCollectionMeta meta, String resourcePath) throws IOException;

    /**
     * get OutputStream ignore compress format
     * @param meta
     * @param resourcePath
     * @return
     * @throws IOException
     */
    OutputStream getRawOutputStream(DataCollectionMeta meta, String resourcePath) throws IOException;

    /**
     * get OutputStream with compress format support
     * @param meta
     * @param resourcePath
     * @return
     * @throws IOException
     */
    OutputStream getOutResourceByStream(DataCollectionMeta meta, String resourcePath) throws IOException;

    /**
     * get InputStream with compress format support
     * @param meta
     * @param resourcePath
     * @return
     * @throws IOException
     */
    InputStream getInResourceByStream(DataCollectionMeta meta, String resourcePath) throws IOException;

    /**
     *  get InputStream ignore compress format
     * @param meta
     * @param resourcePath
     * @return
     * @throws IOException
     */
    InputStream getRawInputStream(DataCollectionMeta meta, String resourcePath) throws IOException;
    boolean exists(DataCollectionMeta meta, String resourcePath) throws IOException;
    long getInputStreamSize(DataCollectionMeta meta, String resourcePath) throws IOException;
    void init(DataCollectionMeta meta);
    String getIdentifier();
}
