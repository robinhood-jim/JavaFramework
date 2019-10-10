package com.robin.core.query.mapper.segment;

import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.List;
import java.util.Map;

/**
 * <p>Created at: 2019-09-30 16:58:37</p>
 *
 * @author robinjim
 * @version 1.0
 */
public class SqlSegment extends AbstractSegment {

    public SqlSegment(String nameSpace, String id, String value) {
        super(nameSpace, id, value);
    }

    @Override
    public String getSqlPart(Map<String, Object> params, Map<String, ImmutablePair<String, List<AbstractSegment>>> segmentsMap) {
        return value;
    }
}
