package com.robin.comm.calculator.math;

import com.robin.comm.calculator.AbstractCalculator;
import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.ObjectUtils;

import java.util.Map;

public class Nvl extends AbstractCalculator {
    public Nvl(){
        functionName="NVL";
    }
    public Nvl(DataCollectionMeta collectionMeta,String[] parameters){
        super(collectionMeta,parameters);
        functionName="NVL";
    }
    @Override
    public Pair<Const.CLASSTYPE, Object> doCalculator(Map<String, Object> inputObj) {
        setInput(inputObj);
        for(int i=0;i<parameters.length;i++){
            if(!ObjectUtils.isEmpty(getValue(parameters[i]))){
                return Pair.of(getMetaTypeByPos(i),getValue(parameters[i]));
            }
        }
        return Pair.of(getMetaTypeByPos(0),null);
    }
}
