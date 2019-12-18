package com.robin.comm.dal.pool;

import com.robin.comm.dal.holder.fs.OutputStreamHolder;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;


public class OutputStreamPoolFactory implements PooledObjectFactory<OutputStreamHolder> {
    @Override
    public PooledObject<OutputStreamHolder> makeObject() throws Exception {
        OutputStreamHolder holder=new OutputStreamHolder();
        return new DefaultPooledObject<>(holder);
    }

    @Override
    public void destroyObject(PooledObject<OutputStreamHolder> pooledObject) throws Exception {
        pooledObject.getObject().close();
    }

    @Override
    public boolean validateObject(PooledObject<OutputStreamHolder> pooledObject) {
        return pooledObject.getObject().isBusyTag();
    }

    @Override
    public void activateObject(PooledObject<OutputStreamHolder> pooledObject) throws Exception {
        pooledObject.getObject().setBusyTag(true);
    }

    @Override
    public void passivateObject(PooledObject<OutputStreamHolder> pooledObject) throws Exception {

    }
}
