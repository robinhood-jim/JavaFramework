package com.robin.comm.calculator.math;

import com.robin.core.base.exception.OperationNotSupportException;
import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.comm.calculator.AbstractCalculator;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.Assert;

import java.util.Map;

public class Square extends AbstractCalculator {
    public Square(){
        functionName="SQUARE";
    }
    public Square(DataCollectionMeta collectionMeta, String[] parameters) {
        super(collectionMeta, parameters);
        functionName="SQUARE";
        Assert.isTrue(parameters.length==1,"one parameter required");
    }

    @Override
    public Pair<Const.CLASSTYPE, Object> doCalculator(Map<String, Object> inputObj) {
        setInput(inputObj);
        Const.CLASSTYPE metaType=getMetaTypeByPos(0);
        Object retValue=null;
        switch (metaType){
            case SHORT:
            case INTEGER:
            case LONG:
            case FLOAT:
            case DECIMAL:
            case NUMERIC:
            case DOUBLE:
                retValue=Math.pow(Double.parseDouble(getValue(parameters[0]).toString()),2);
                break;
            default:
                throw new OperationNotSupportException("type not supported!");
        }
        return Pair.of(Const.CLASSTYPE.DOUBLE,retValue);
    }
    //public static String getFunctionName(){return "SQUARE";}
}
