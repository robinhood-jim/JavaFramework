package com.robin.core.query.mapper.segment;

import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.List;
import java.util.Map;


public class ConstantSegment extends AbstractSegment {

    public ConstantSegment(String nameSpace,String id, String inputStr) {
        super(nameSpace,id,inputStr);
    }


    @Override
    public String getSqlPart(Map<String,Object> params, Map<String, ImmutablePair<String, List<AbstractSegment>>> segmentsMap) {
        return value;
    }
}
