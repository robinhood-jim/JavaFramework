package com.robin.core.query.mapper.segment;

import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.List;
import java.util.Map;

public class CompositeSegment extends AbstractSegment {
    private String type;
    private String paramType;
    private List<AbstractSegment> segments;

    private String resultMap;

    public CompositeSegment(String nameSpace, String id, String value,String type,List<AbstractSegment> segments) {
        super(nameSpace, id, value);
        this.type =type;
        this.segments=segments;
    }
    public CompositeSegment(String nameSpace, String id, String value,String type,List<AbstractSegment> segments,String resultMap,String paramType) {
        super(nameSpace, id, value);
        this.type =type;
        this.segments=segments;
        this.resultMap=resultMap;
        this.paramType=paramType;
    }

    @Override
    public String getSqlPart(Map<String, Object> params, Map<String, ImmutablePair<String, List<AbstractSegment>>> segmentsMap) {
       StringBuilder builder=new StringBuilder();
        for(AbstractSegment segment:segments){
            builder.append(segment.getSqlPart(params,segmentsMap));
        }
        return builder.toString();
    }

    public String getType() {
        return type;
    }

    public String getResultMap() {
        return resultMap;
    }

    public String getParamType() {
        return paramType;
    }
}
