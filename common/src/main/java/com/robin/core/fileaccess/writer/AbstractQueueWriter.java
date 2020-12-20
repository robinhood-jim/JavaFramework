package com.robin.core.fileaccess.writer;

import com.robin.core.fileaccess.meta.DataCollectionMeta;
import org.apache.avro.generic.GenericRecord;

import javax.naming.OperationNotSupportedException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public abstract class AbstractQueueWriter extends AbstractResourceWriter {
    public AbstractQueueWriter(DataCollectionMeta collectionMeta){
        super(collectionMeta);
    }
    @Override
    public void writeRecord(GenericRecord genericRecord) throws IOException,OperationNotSupportedException {
        throw new OperationNotSupportedException("operation not Supported!");
    }

    @Override
    public void writeRecord(Map<String, ?> map) throws IOException, OperationNotSupportedException {
        throw new OperationNotSupportedException("operation not Supported!");
    }

    @Override
    public void writeRecord(List<Object> map) throws IOException,OperationNotSupportedException {
        throw new OperationNotSupportedException("operation not Supported!");
    }
    public abstract void writeMessage(String queue,Map<String, ?> map) throws IOException;
}
