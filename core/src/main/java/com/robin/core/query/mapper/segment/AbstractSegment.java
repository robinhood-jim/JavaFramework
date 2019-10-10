package com.robin.core.query.mapper.segment;

import lombok.Data;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.List;
import java.util.Map;

@Data
public abstract class AbstractSegment {
    protected String value;
    protected String id;
    protected String nameSpace;

    public AbstractSegment(String nameSpace,String id,String value){
        this.value=value;
        this.nameSpace=nameSpace;
        this.id=id;
    }
    public abstract String getSqlPart(Map<String,Object> params,Map<String, ImmutablePair<String, List<AbstractSegment>>> segmentsMap);
}
