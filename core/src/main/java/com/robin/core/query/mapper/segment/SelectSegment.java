package com.robin.core.query.mapper.segment;

import java.util.List;

/**
 * <p>Created at: 2019-10-09 14:22:27</p>
 *
 * @author robinjim
 * @version 1.0
 */
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
