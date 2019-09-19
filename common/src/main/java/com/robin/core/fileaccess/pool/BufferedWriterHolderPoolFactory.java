package com.robin.core.fileaccess.pool;

import com.robin.core.fileaccess.holder.BufferedWriterHolder;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class BufferedWriterHolderPoolFactory implements PooledObjectFactory<BufferedWriterHolder> {
    @Override
    public PooledObject<BufferedWriterHolder> makeObject() throws Exception {
        BufferedWriterHolder holder=new BufferedWriterHolder();
        return new DefaultPooledObject<>(holder);
    }

    @Override
    public void destroyObject(PooledObject<BufferedWriterHolder> pooledObject) throws Exception {
        pooledObject.getObject().close();
    }

    @Override
    public boolean validateObject(PooledObject<BufferedWriterHolder> pooledObject) {
        return pooledObject.getObject().isBusyTag();
    }

    @Override
    public void activateObject(PooledObject<BufferedWriterHolder> pooledObject) throws Exception {
        pooledObject.getObject().setBusyTag(true);
    }

    @Override
    public void passivateObject(PooledObject<BufferedWriterHolder> pooledObject) throws Exception {

    }
}
