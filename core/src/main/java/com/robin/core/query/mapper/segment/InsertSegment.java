package com.robin.core.query.mapper.segment;

import java.util.List;


public class InsertSegment extends CompositeSegment{
    private boolean useGenerateKeys=false;
    private String keyProperty;


    public InsertSegment(String nameSpace, String id, String value, String type, List<AbstractSegment> segments) {
        super(nameSpace, id, value, type, segments);
    }

    public InsertSegment(String nameSpace, String id, String value, String type, List<AbstractSegment> segments, String resultMap, String paramType) {
        super(nameSpace, id, value, type, segments, resultMap, paramType);
    }
    public InsertSegment(String nameSpace, String id, String value, String type, List<AbstractSegment> segments, String resultMap, String paramType,boolean useGenerateKeys,String keyProperty) {
        super(nameSpace, id, value, type, segments, resultMap, paramType);
        this.useGenerateKeys=useGenerateKeys;
        this.keyProperty=keyProperty;
    }

    public String getKeyProperty() {
        return keyProperty;
    }

    public boolean isUseGenerateKeys() {
        return useGenerateKeys;
    }
}
