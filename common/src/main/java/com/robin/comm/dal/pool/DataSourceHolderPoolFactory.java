package com.robin.comm.dal.pool;


import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;

public class DataSourceHolderPoolFactory implements PooledObjectFactory {
    @Override
    public PooledObject makeObject() throws Exception {
        return null;
    }

    @Override
    public void destroyObject(PooledObject pooledObject) throws Exception {

    }

    @Override
    public boolean validateObject(PooledObject pooledObject) {
        return false;
    }

    @Override
    public void activateObject(PooledObject pooledObject) throws Exception {

    }

    @Override
    public void passivateObject(PooledObject pooledObject) throws Exception {

    }
}
