package com.robin.core.fileaccess.fs;

import com.robin.core.fileaccess.meta.DataCollectionMeta;
import org.apache.commons.lang3.tuple.Pair;

import java.io.*;

public interface IFileSystemAccessor {
    Pair<BufferedReader,InputStream> getInResourceByReader(DataCollectionMeta meta, String resourcePath) throws IOException;
    Pair<BufferedWriter,OutputStream> getOutResourceByWriter(DataCollectionMeta meta, String resourcePath) throws IOException;
    OutputStream getOutResourceByStream(DataCollectionMeta meta, String resourcePath) throws IOException;
    OutputStream getRawOutputStream(DataCollectionMeta meta, String resourcePath) throws IOException;
    InputStream getInResourceByStream(DataCollectionMeta meta, String resourcePath) throws IOException;
    InputStream getRawInputStream(DataCollectionMeta meta, String resourcePath) throws IOException;
    boolean exists(DataCollectionMeta meta, String resourcePath) throws IOException;
    long getInputStreamSize(DataCollectionMeta meta, String resourcePath) throws IOException;
    void init(DataCollectionMeta meta);
    String getIdentifier();
}
