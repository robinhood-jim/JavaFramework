package com.robin.comm.calculator.math;

import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.comm.calculator.AbstractCalculator;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Percentile extends AbstractCalculator {
    List<Double> inputValues=new ArrayList<>();
    org.apache.commons.math3.stat.descriptive.rank.Percentile percentile;
    public Percentile(){
        functionName="PERCENTILE";
    }

    public Percentile(DataCollectionMeta collectionMeta) {
        super(collectionMeta);
        functionName="PERCENTILE";

        percentile=new org.apache.commons.math3.stat.descriptive.rank.Percentile();
    }

    @Override
    public Pair<Const.CLASSTYPE, Object> doCalculator(Map<String, Object> inputObj,String[] parameters) {
        setInput(inputObj);
        inputValues.clear();
        for(int i=0;i<parameters.length;i++){
            inputValues.add(getParameterValue(0,parameters));
        }
        double p50=percentile.evaluate(inputValues.stream().mapToDouble(Double::doubleValue).toArray(),50);
        return Pair.of(Const.CLASSTYPE.DOUBLE,p50);

    }
    private double getParameterValue(int pos,String[] parameters){
        Const.CLASSTYPE metaType=getMetaTypeByPos(0,parameters);
        Object originValue=getValue(parameters[pos]);
        double retValue = 0.0;
        if(!ObjectUtils.isEmpty(originValue)) {
            switch (metaType) {
                case INTEGER:
                case SHORT:
                case FLOAT:
                case DOUBLE:
                case NUMERIC:
                case LONG:
                    retValue = Double.valueOf(originValue.toString());
                    break;
            }
        }
        return retValue;
    }
    //public static String getFunctionName(){return "PERCENTILE";}
}
