package com.robin.comm.calculator.math;

import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.comm.calculator.AbstractCalculator;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.Assert;

import java.util.Map;

public class Floor extends AbstractCalculator {
    public Floor(){
        functionName="FLOOR";
    }

    public Floor(DataCollectionMeta collectionMeta, String[] parameters) {
        super(collectionMeta, parameters);
        functionName="FLOOR";
        Assert.isTrue(parameters.length==1,"one parameter required");
    }

    @Override
    public Pair<Const.CLASSTYPE, Object> doCalculator(Map<String, Object> inputObj) {
        setInput(inputObj);

        Object retValue=null;
        Const.CLASSTYPE metaType=getMetaTypeByPos(0);
        double floorVal=Math.floor(Double.parseDouble(getValue(parameters[0]).toString()));
        switch (metaType){
            case SHORT:
                retValue=Double.valueOf(floorVal).shortValue();
                break;
            case INTEGER:
                retValue=Double.valueOf(floorVal).intValue();
                break;
            case LONG:
                retValue=Double.valueOf(floorVal).longValue();
                break;
            case FLOAT:
                retValue=Double.valueOf(floorVal).floatValue();
            case DOUBLE:
                retValue=floorVal;
                break;
        }
        return Pair.of(metaType,retValue);
    }
    //public static String getFunctionName(){return "FLOOR";}
}
