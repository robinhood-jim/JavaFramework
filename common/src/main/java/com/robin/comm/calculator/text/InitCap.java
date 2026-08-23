package com.robin.comm.calculator.text;

import com.robin.comm.calculator.AbstractCalculator;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.StringUtils;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.ObjectUtils;

import java.util.Map;

public class InitCap extends AbstractCalculator {
    public InitCap(){
        functionName="INIT_CAP";
    }
    public InitCap(DataCollectionMeta collectionMeta,String[] paramters){
        super(collectionMeta,paramters);
        functionName="INIT_CAP";
    }
    @Override
    public Pair<Const.CLASSTYPE, Object> doCalculator(Map<String, Object> inputObj) {
        setInput(inputObj);
        Object value=getValue(parameters[0]);
        String retStr=null;
        if(!ObjectUtils.isEmpty(value)){
            retStr= StringUtils.returnCamelCaseByFieldName(value.toString());
        }
        return Pair.of(Const.CLASSTYPE.STRING,retStr);
    }
}
