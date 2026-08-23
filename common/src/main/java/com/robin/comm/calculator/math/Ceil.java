package com.robin.comm.calculator.math;

import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.comm.calculator.AbstractCalculator;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.Assert;

import java.util.Map;

public class Ceil  extends AbstractCalculator {
    public Ceil(){
        functionName="CEIL";
    }

    public Ceil(DataCollectionMeta collectionMeta, String[] parameters) {
        super(collectionMeta, parameters);
        functionName="CEIL";
        Assert.isTrue(parameters.length==1,"one parameter required");
    }

    @Override
    public Pair<Const.CLASSTYPE, Object> doCalculator(Map<String, Object> inputObj) {
        setInput(inputObj);
        Const.CLASSTYPE metaType=getMetaTypeByPos(0);
        Object retValue=null;
        Double ceilVal=Math.ceil(Double.parseDouble(getValue(parameters[0]).toString()));
        switch (metaType){
            case SHORT:
                retValue=Double.valueOf(ceilVal).shortValue();
                break;
            case INTEGER:
                retValue=Double.valueOf(ceilVal).intValue();
                break;
            case LONG:
                retValue=Double.valueOf(ceilVal).longValue();
                break;
            case FLOAT:
                retValue=Double.valueOf(ceilVal).floatValue();
                break;
            case DOUBLE:
                retValue=ceilVal;
                break;
        }
        return Pair.of(metaType,retValue);
    }
    //public static String getFunctionName(){return "CEIL";}
}
