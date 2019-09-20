package com.robin.comm.dal.pool;

import com.robin.comm.dal.holder.RecordWriterHolder;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class RecordWriterHolderPoolFactory implements PooledObjectFactory<RecordWriterHolder> {
    @Override
    public PooledObject<RecordWriterHolder> makeObject() throws Exception {
        RecordWriterHolder holder=new RecordWriterHolder();
        return new DefaultPooledObject<>(holder);
    }

    @Override
    public void destroyObject(PooledObject<RecordWriterHolder> pooledObject) throws Exception {
        pooledObject.getObject().close();
    }

    @Override
    public boolean validateObject(PooledObject<RecordWriterHolder> pooledObject) {
        return pooledObject.getObject().isBusyTag();
    }

    @Override
    public void activateObject(PooledObject<RecordWriterHolder> pooledObject) throws Exception {
        pooledObject.getObject().setBusyTag(true);
    }

    @Override
    public void passivateObject(PooledObject<RecordWriterHolder> pooledObject) throws Exception {

    }
}
