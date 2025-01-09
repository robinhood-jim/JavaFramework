package com.robin.core.fileaccess.util;

import com.robin.core.base.exception.GenericException;
import lombok.extern.slf4j.Slf4j;
import stormpot.BlazePool;
import stormpot.Config;
import stormpot.Pool;

import stormpot.Timeout;

import java.io.Closeable;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CalculatorPool implements Closeable {
    private BlazePool<Calculator> pool;
    private CalculatorAllocator allocator;
    public CalculatorPool(){
        allocator=new CalculatorAllocator();
        Config<Calculator> config = new Config<Calculator>().setSize(50).setAllocator(allocator);
        pool = new BlazePool<>(config);

    }
    public void close() {
        pool.shutdown();
    }
    public Calculator borrowObject() {
        try {
            return pool.claim(new Timeout(1,TimeUnit.SECONDS));
        }catch (InterruptedException ex){
            log.error("{}",ex.getMessage());
            throw new GenericException(ex);
        }
    }
    public void returnObject(Calculator calculator){
        calculator.release();
    }


}
