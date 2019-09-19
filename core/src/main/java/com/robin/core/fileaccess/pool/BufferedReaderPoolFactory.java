package com.robin.core.fileaccess.pool;


import com.robin.core.fileaccess.holder.BufferedReaderHolder;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class BufferedReaderPoolFactory implements PooledObjectFactory<BufferedReaderHolder> {

    @Override
    public PooledObject<BufferedReaderHolder> makeObject() throws Exception {
        BufferedReaderHolder holder=new BufferedReaderHolder();
        return new DefaultPooledObject<>(holder);
    }

    @Override
    public void destroyObject(PooledObject<BufferedReaderHolder> pooledObject) throws Exception {
        pooledObject.getObject().setBusyTag(false);
    }

    @Override
    public boolean validateObject(PooledObject<BufferedReaderHolder> pooledObject) {
        return pooledObject.getObject().isBusyTag();
    }

    @Override
    public void activateObject(PooledObject<BufferedReaderHolder> pooledObject) throws Exception {
        pooledObject.getObject().setBusyTag(true);
    }

    @Override
    public void passivateObject(PooledObject<BufferedReaderHolder> pooledObject) throws Exception {

    }
}
