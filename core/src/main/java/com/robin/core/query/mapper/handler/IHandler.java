package com.robin.core.query.mapper.handler;

import com.robin.core.query.mapper.segment.AbstractSegment;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.dom4j.Element;

import java.util.List;
import java.util.Map;


public interface IHandler {
    void analyse(Element element,String nameSpace, List<AbstractSegment> segments);
    String getValue(Map<String,Object> params, List<AbstractSegment> segments, Map<String, ImmutablePair<String, List<AbstractSegment>>> segmentsMap);
}
