package com.robin.comm.calculator.text;

import com.robin.comm.calculator.AbstractCalculator;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.StringUtils;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.ObjectUtils;

import java.util.Map;

public class LowerCase extends AbstractCalculator {
    public LowerCase(){
        functionName="LOWERCASE";
    }
    public LowerCase(DataCollectionMeta collectionMeta){
        super(collectionMeta);
        functionName="LOWERCASE";
    }

    @Override
    public Pair<Const.CLASSTYPE, Object> doCalculator(Map<String, Object> inputObj,String[] parameters) {
        setInput(inputObj);
        Object value=getValue(parameters[0]);
        String retStr=null;
        if(!ObjectUtils.isEmpty(value)){
            retStr= StringUtils.lowerCase(value.toString());
        }
        return Pair.of(Const.CLASSTYPE.STRING,retStr);
    }
}
