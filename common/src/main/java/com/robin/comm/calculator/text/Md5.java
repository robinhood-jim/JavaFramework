package com.robin.comm.calculator.text;

import cn.hutool.crypto.digest.DigestUtil;
import com.robin.comm.calculator.AbstractCalculator;
import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import java.util.Map;

public class Md5 extends AbstractCalculator {
    public Md5(){
        functionName="MD5";
    }

    public Md5(DataCollectionMeta collectionMeta) {
        super(collectionMeta);
        functionName="MD5";

    }

    @Override
    public Pair<Const.CLASSTYPE, Object> doCalculator(Map<String, Object> inputObj,String[] parameters) {
        setInput(inputObj);
        Const.CLASSTYPE metaType=getMetaTypeByPos(0,parameters);
        Assert.isTrue(metaType.equals(Const.CLASSTYPE.STRING),"");
        Object value=getValue(parameters[0]);
        if(!ObjectUtils.isEmpty(value)){
            String str= DigestUtil.md5Hex(value.toString());
            return Pair.of(Const.CLASSTYPE.STRING,str);
        }
        return Pair.of(Const.CLASSTYPE.STRING,null);
    }

}
