package com.robin.core.fileaccess.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.PooledObject;

import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
@Slf4j
public class CalculatorPoolFactory implements PooledObjectFactory<Calculator> {

    @Override
    public PooledObject<Calculator> makeObject() throws Exception {
        Calculator calculator=new Calculator();
        return new DefaultPooledObject<>(calculator);
    }

    @Override
    public void destroyObject(PooledObject<Calculator> pooledObject) throws Exception {
        pooledObject.getObject().close();
    }

    @Override
    public boolean validateObject(PooledObject<Calculator> pooledObject) {
        return !pooledObject.getObject().isBusyTag();
    }

    @Override
    public void activateObject(PooledObject<Calculator> pooledObject) throws Exception {
        pooledObject.getObject().setBusyTag(true);
    }

    @Override
    public void passivateObject(PooledObject<Calculator> pooledObject) throws Exception {
        pooledObject.getObject().setBusyTag(false);
    }
}
