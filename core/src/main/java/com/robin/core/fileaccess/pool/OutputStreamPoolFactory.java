package com.robin.core.fileaccess.pool;

import com.robin.core.fileaccess.holder.OutputStreamHolder;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * <p>Created at: 2019-09-19 17:15:11</p>
 *
 * @author robinjim
 * @version 1.0
 */
public class OutputStreamPoolFactory implements PooledObjectFactory<OutputStreamHolder> {
    @Override
    public PooledObject<OutputStreamHolder> makeObject() throws Exception {
        OutputStreamHolder holder=new OutputStreamHolder();
        return new DefaultPooledObject<>(holder);
    }

    @Override
    public void destroyObject(PooledObject<OutputStreamHolder> pooledObject) throws Exception {
        pooledObject.getObject().setBusyTag(false);
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
