package com.robin.comm.calculator.math;

import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.comm.calculator.AbstractCalculator;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.Assert;

import java.util.Map;

public class Multiply extends AbstractCalculator {
    public Multiply(){
        functionName="MULTIPLY";
    }

    public Multiply(DataCollectionMeta collectionMeta) {
        super(collectionMeta);
        functionName="MULTIPLY";

    }

    @Override
    public Pair<Const.CLASSTYPE, Object> doCalculator(Map<String, Object> inputObj,String[] parameters) {
        setInput(inputObj);
        Assert.isTrue(parameters.length==2,"Add parameter must be two");
        Const.CLASSTYPE metaType=getMetaTypeByPos(0,parameters);
        Const.CLASSTYPE retMetaType= Const.CLASSTYPE.LONG;
        Object retValue=null;
        switch (metaType){
            case SHORT:
            case INTEGER:
                retValue=Math.multiplyExact(Integer.parseInt(getValue(parameters[0]).toString()),Integer.parseInt(getValue(parameters[1]).toString()));
                retMetaType= Const.CLASSTYPE.INTEGER;
                break;
            case LONG:
                retValue=Math.multiplyExact(Long.parseLong(getValue(parameters[0]).toString()),Long.parseLong(getValue(parameters[1]).toString()));
                retMetaType= Const.CLASSTYPE.LONG;
                break;
            case FLOAT:
            case DOUBLE:
                retValue=Double.parseDouble(getValue(parameters[0]).toString())*Double.parseDouble(getValue(parameters[1]).toString());
                retMetaType= Const.CLASSTYPE.DOUBLE;
                break;
        }
        return Pair.of(retMetaType,retValue);
    }
    //public static String getFunctionName(){return "MULTIPLY";}
}
