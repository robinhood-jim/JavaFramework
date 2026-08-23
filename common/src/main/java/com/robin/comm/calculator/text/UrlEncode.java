package com.robin.comm.calculator.text;

import com.robin.comm.calculator.AbstractCalculator;
import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.ObjectUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class UrlEncode extends AbstractCalculator {
    public UrlEncode(){
        functionName="URLENCODE";
    }
    public UrlEncode(DataCollectionMeta collectionMeta){
        super(collectionMeta);
        functionName="URLENCODE";
    }

    @Override
    public Pair<Const.CLASSTYPE, Object> doCalculator(Map<String, Object> inputObj,String[] parameters) {
        setInput(inputObj);
        Object value=getValue(parameters[0]);
        String encodeStr=null;
        if(!ObjectUtils.isEmpty(value)){
            encodeStr= URLEncoder.encode(value.toString(), StandardCharsets.UTF_8);
        }
        return Pair.of(Const.CLASSTYPE.STRING,encodeStr);
    }
}
