package com.robin.comm.calculator.math;

import com.robin.comm.calculator.AbstractCalculator;
import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.Assert;

import java.util.Map;

public class Abs  extends AbstractCalculator {
    public Abs(){
        functionName="ABS";
    }

    public Abs(DataCollectionMeta collectionMeta) {
        super(collectionMeta);
        functionName="ABS";
    }
    @Override
    public Pair<Const.CLASSTYPE, Object> doCalculator(Map<String, Object> inputObj,String[] parameters) {
        setInput(inputObj);
        Assert.isTrue(parameters.length==1,"one parameter required");
        Const.CLASSTYPE metaType=getMetaTypeByPos(0,parameters);
        Object retValue=null;
        switch (metaType){
            case SHORT:
                retValue=Math.abs(Short.parseShort(getValue(parameters[0]).toString()));
                break;
            case INTEGER:
                retValue=Math.abs(Integer.parseInt(getValue(parameters[0]).toString()));
                break;
            case LONG:
                retValue=Math.abs(Long.parseLong(getValue(parameters[0]).toString()));
                break;
            case FLOAT:
                retValue=Math.abs(Float.parseFloat(getValue(parameters[0]).toString()));
                break;
            case DOUBLE:
                retValue=Math.abs(Double.parseDouble(getValue(parameters[0]).toString()));
                break;
        }
        return Pair.of(metaType,retValue);
    }
    //public static String getFunctionName(){return "ABS";}
}
