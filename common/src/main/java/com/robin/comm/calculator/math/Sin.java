package com.robin.comm.calculator.math;

import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.comm.calculator.AbstractCalculator;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.Assert;

import java.util.Map;

public class Sin extends AbstractCalculator {
    public Sin(){
        functionName="SIN";
    }
    public Sin(DataCollectionMeta collectionMeta, String[] parameters) {
        super(collectionMeta, parameters);
        functionName="SIN";
        Assert.isTrue(parameters.length==1,"one parameter required");
        Const.CLASSTYPE metaType=getMetaTypeByPos(0);
        Assert.isTrue(metaType== Const.CLASSTYPE.DOUBLE,"must be double column");
    }

    @Override
    public Pair<Const.CLASSTYPE, Object> doCalculator(Map<String, Object> inputObj) {
        setInput(inputObj);
        return Pair.of(Const.CLASSTYPE.DOUBLE,Math.sin(Double.valueOf(getValue(parameters[0]).toString())));
    }
    //public static String getFunctionName(){return "SIN";}
}
