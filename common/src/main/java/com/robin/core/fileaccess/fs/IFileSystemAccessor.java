package com.robin.core.fileaccess.fs;

import com.robin.core.fileaccess.meta.DataCollectionMeta;

import java.io.*;

public interface IFileSystemAccessor {
    BufferedReader getInResourceByReader(DataCollectionMeta meta, String resourcePath) throws IOException;
    BufferedWriter getOutResourceByWriter(DataCollectionMeta meta, String resourcePath) throws IOException;
    OutputStream getOutResourceByStream(DataCollectionMeta meta, String resourcePath) throws IOException;
    OutputStream getRawOutputStream(DataCollectionMeta meta, String resourcePath) throws IOException;
    InputStream getInResourceByStream(DataCollectionMeta meta, String resourcePath) throws IOException;
    InputStream getRawInputStream(DataCollectionMeta meta, String resourcePath) throws IOException;
    boolean exists(DataCollectionMeta meta, String resourcePath) throws IOException;
    long getInputStreamSize(DataCollectionMeta meta, String resourcePath) throws IOException;
    void init(DataCollectionMeta meta);
    String getIdentifier();
}
