package com.robin.comm.calculator.math;

import com.robin.comm.calculator.AbstractCalculator;
import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.Assert;

import java.util.Map;

public class Subtract extends AbstractCalculator {
    public Subtract(){
        functionName="SUBTRACT";
    }
    public Subtract(DataCollectionMeta collectionMeta) {
        super(collectionMeta);
        functionName="SUBTRACT";

    }

    @Override
    public Pair<Const.CLASSTYPE, Object> doCalculator(Map<String, Object> inputObj,String[] parameters) {
        setInput(inputObj);
        Assert.isTrue(parameters.length==2,"Add parameter must be two");
        Const.CLASSTYPE metaType=getMetaTypeByPos(0,parameters);
        Object retValue=null;
        switch (metaType){
            case SHORT:
                retValue=Math.subtractExact(Short.parseShort(getValue(parameters[0]).toString()),Short.parseShort(getValue(parameters[1]).toString()));
                metaType= Const.CLASSTYPE.INTEGER;
                break;
            case INTEGER:
                retValue=Math.subtractExact(Integer.parseInt(getValue(parameters[0]).toString()),Integer.parseInt(getValue(parameters[1]).toString()));
                break;
            case LONG:
                retValue=Math.subtractExact(Long.parseLong(getValue(parameters[0]).toString()),Long.parseLong(getValue(parameters[1]).toString()));
                break;
            case FLOAT:
                retValue=Float.parseFloat(getValue(parameters[0]).toString())-Float.parseFloat(getValue(parameters[1]).toString());
                break;
            case DOUBLE:
                retValue=Double.parseDouble(getValue(parameters[0]).toString())-Double.parseDouble(getValue(parameters[1]).toString());
                break;
        }
        return Pair.of(metaType,retValue);
    }
    //public static String getFunctionName(){return "SUBTRACT";}
}
