package com.robin.comm.dal.pool;


import com.robin.comm.dal.holder.FsRecordIteratorHolder;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class RecordIteratorHolderPoolFactory implements PooledObjectFactory<FsRecordIteratorHolder> {

    @Override
    public PooledObject<FsRecordIteratorHolder> makeObject() throws Exception {
        FsRecordIteratorHolder holder=new FsRecordIteratorHolder();
        return new DefaultPooledObject<>(holder);
    }

    @Override
    public void destroyObject(PooledObject<FsRecordIteratorHolder> pooledObject) throws Exception {
        pooledObject.getObject().close();
    }

    @Override
    public boolean validateObject(PooledObject<FsRecordIteratorHolder> pooledObject) {
        return pooledObject.getObject().isBusyTag();
    }

    @Override
    public void activateObject(PooledObject<FsRecordIteratorHolder> pooledObject) throws Exception {
        pooledObject.getObject().setBusyTag(true);
    }

    @Override
    public void passivateObject(PooledObject<FsRecordIteratorHolder> pooledObject) throws Exception {

    }
}
