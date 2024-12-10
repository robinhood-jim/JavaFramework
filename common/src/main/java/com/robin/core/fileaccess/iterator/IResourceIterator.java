package com.robin.core.fileaccess.iterator;
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

public interface IResourceIterator extends Iterator<Map<String,Object>>, Closeable {
    void beforeProcess();
    void afterProcess();
    String getIdentifier();
    void setInputStream(InputStream inputStream);
    void setReader(BufferedReader reader);
    void setAccessUtil(AbstractFileSystemAccessor accessUtil);
}