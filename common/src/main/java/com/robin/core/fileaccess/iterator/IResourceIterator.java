package com.robin.core.fileaccess.iterator;
import java.io.Closeable;
import java.util.Iterator;
import java.util.Map;

public interface IResourceIterator extends Iterator<Map<String,Object>>, Closeable {
    void init();
    void beforeProcess(String param);
    void afterProcess();
    String getIdentifier();
}