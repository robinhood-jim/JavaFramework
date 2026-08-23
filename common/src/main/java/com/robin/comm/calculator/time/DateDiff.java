package com.robin.comm.calculator.time;

import com.robin.comm.calculator.AbstractCalculator;
import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;

public class DateDiff extends AbstractCalculator {
    public DateDiff(){
        functionName="DATE_DIFF";
    }
    public DateDiff(DataCollectionMeta collectionMeta){
        super(collectionMeta);
        functionName="DATE_DIFF";
    }

    @Override
    public Pair<Const.CLASSTYPE, Object> doCalculator(Map<String, Object> inputObj,String[] parameters) {
        return null;
    }
}
