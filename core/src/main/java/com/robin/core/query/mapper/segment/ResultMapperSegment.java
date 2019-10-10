package com.robin.core.query.mapper.segment;

import com.robin.core.base.util.Const;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.dom4j.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ResultMapperSegment extends AbstractSegment {
    private String className;
    private Map<String, ImmutablePair<String, String>> columnMapper = new HashMap<>();

    public ResultMapperSegment(String nameSpace, String id, String value) {
        super(nameSpace, id, value);

    }

    public void parse(Element element) {
        className=element.attributeValue("type");
        List<Element> elements = element.elements();
        for (Element ele : elements) {
            String column = ele.attributeValue("column");
            String prop = ele.attributeValue("property");
            String type = ele.attributeValue("jdbcType");
            columnMapper.put(column,new ImmutablePair<>(prop,getMetaType(type)));
        }
    }

    private String getMetaType(String jdbcType) {
        String metaType = null;
        switch (jdbcType.toUpperCase()) {
            case "CHAR":
            case "VARCHAR":
                metaType = Const.META_TYPE_STRING;
                break;
            case "DOUBLE":
            case "NUMERIC":
                metaType = Const.META_TYPE_DOUBLE;
                break;
            case "BIGINT":
                metaType = Const.META_TYPE_BIGINT;
                break;
            case "INT":
                metaType = Const.META_TYPE_INTEGER;
                break;
            case "TIMESTAMP":
            case "DATE":
                metaType = Const.META_TYPE_TIMESTAMP;
                break;
            default:
                metaType = Const.META_TYPE_STRING;
                break;
        }
        return metaType;
    }

    public Map<String, ImmutablePair<String, String>> getColumnMapper() {
        return columnMapper;
    }

    public String getClassName() {
        return className;
    }

    @Override
    public String getSqlPart(Map<String, Object> params, Map<String, ImmutablePair<String, List<AbstractSegment>>> segmentsMap) {
        return null;
    }
}
