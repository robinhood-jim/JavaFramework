package com.robin.core.fileaccess.writer;

import org.apache.avro.generic.GenericRecord;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * <p>Project:  frame</p>
 * <p>
 * <p>Description:com.robin.core.fileaccess.writer</p>
 * <p>
 * <p>Copyright: Copyright (c) 2018 create at 2018年10月31日</p>
 * <p>
 * <p>Company: zhcx_DEV</p>
 *
 * @author robinjim
 * @version 1.0
 */
public interface IResourceWriter extends Closeable {
    void writeRecord(Map<String,?> map) throws IOException;
    void writeRecord(List<Object> map) throws IOException;

    void initalize() throws IOException;


}
