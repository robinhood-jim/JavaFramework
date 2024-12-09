package com.robin.core.fileaccess.writer;

import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;

import javax.naming.OperationNotSupportedException;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;


public interface IResourceWriter extends Closeable {
    void writeRecord(Map<String,Object> map) throws IOException,OperationNotSupportedException;
    void writeRecord(List<Object> map) throws IOException,OperationNotSupportedException;

    void initalize() throws IOException;
    String getIdentifier();
    void setWriter(BufferedWriter writer);
    void setOutputStream(OutputStream outputStream);
    void setAccessUtil(AbstractFileSystemAccessor accessUtil);

}
