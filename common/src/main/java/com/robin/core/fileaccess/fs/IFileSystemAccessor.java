package com.robin.core.fileaccess.fs;

import com.robin.core.fileaccess.meta.DataCollectionMeta;
import org.apache.commons.lang3.tuple.Pair;

import java.io.*;

/**
 *  All FileSystem accessor interface,Extends By Local/VFS/HDFS and cloud storage supported.
 */
public interface IFileSystemAccessor extends Closeable {
    /**
     * get BufferedReader or inputStream with compress format
     * @param resourcePath
     * @return Pair(BufferedReader,InputStream)
     * @throws IOException
     */
     Pair<BufferedReader,InputStream> getInResourceByReader(String resourcePath) throws IOException;

    /**
     * get BufferedWriter or OutputStream with compress format
     * @param resourcePath
     * @return
     * @throws IOException
     */
    Pair<BufferedWriter,OutputStream> getOutResourceByWriter(String resourcePath) throws IOException;

    /**
     * get OutputStream ignore compress format
     * @param resourcePath
     * @return
     * @throws IOException
     */
    OutputStream getRawOutputStream(String resourcePath) throws IOException;

    /**
     * get OutputStream with compress format support
     * @param resourcePath
     * @return
     * @throws IOException
     */
    OutputStream getOutResourceByStream(String resourcePath) throws IOException;

    /**
     * get InputStream with compress format support
     * @param resourcePath
     * @return
     * @throws IOException
     */
    InputStream getInResourceByStream(String resourcePath) throws IOException;

    /**
     *  get InputStream ignore compress format
     * @param resourcePath
     * @return
     * @throws IOException
     */
    InputStream getRawInputStream(String resourcePath) throws IOException;
    boolean exists( String resourcePath) throws IOException;
    long getInputStreamSize(String resourcePath) throws IOException;
    void init(DataCollectionMeta meta);
    void finishReadOrWrite() throws IOException;
    String getIdentifier();
}
