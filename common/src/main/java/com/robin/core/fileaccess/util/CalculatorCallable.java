package com.robin.core.fileaccess.util;

import java.util.concurrent.Callable;

public class CalculatorCallable implements Callable<Boolean> {
    private Calculator calculator;
    private CalculatorPool pool;


    public CalculatorCallable(Calculator calculator,CalculatorPool pool){
        this.calculator=calculator;
        this.pool=pool;
    }

    @Override
    public Boolean call() throws Exception {
        calculator.setBusyTag(true);
        try{
            return calculator.doCalculate(calculator.getValueParts());
        }catch (Exception ex){
            throw ex;
        }finally {
            pool.returnObject(calculator);
        }
    }
}
