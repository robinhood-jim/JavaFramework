package com.robin.core.query.mapper.segment;

import java.util.List;


public class SelectSegment extends CompositeSegment {
    private String countRef;


    public SelectSegment(String nameSpace, String id, String value, String type, List<AbstractSegment> segments) {
        super(nameSpace, id, value, type, segments);
    }

    public SelectSegment(String nameSpace, String id, String value, String type, List<AbstractSegment> segments, String resultMap, String paramType) {
        super(nameSpace, id, value, type, segments, resultMap, paramType);
    }

    public void setCountRef(String countRef) {
        this.countRef = countRef;
    }

    public String getCountRef() {
        return countRef;
    }
}
