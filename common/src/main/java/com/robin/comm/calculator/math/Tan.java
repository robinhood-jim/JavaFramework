package com.robin.comm.calculator.math;

import com.robin.comm.calculator.AbstractCalculator;
import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.Assert;

import java.util.Map;

public class Tan extends AbstractCalculator {
    public Tan(){
        functionName="TAN";
    }

    public Tan(DataCollectionMeta collectionMeta, String[] parameters) {
        super(collectionMeta, parameters);
        functionName="TAN";
        Assert.isTrue(parameters.length==1,"one parameter required");
        Const.CLASSTYPE metaType=getMetaTypeByPos(0);
        Assert.isTrue(metaType== Const.CLASSTYPE.DOUBLE,"must be double column");
    }

    @Override
    public Pair<Const.CLASSTYPE, Object> doCalculator(Map<String, Object> inputObj) {
        setInput(inputObj);
        return Pair.of(Const.CLASSTYPE.DOUBLE,Math.tan(Double.valueOf(getValue(parameters[0]).toString())));
    }
    //public static String getFunctionName(){return "TAN";}
}
