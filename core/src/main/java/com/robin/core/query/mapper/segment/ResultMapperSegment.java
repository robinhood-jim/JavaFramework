package com.robin.core.query.mapper.segment;

import com.robin.core.base.exception.ConfigurationIncorrectException;
import com.robin.core.base.util.Const;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.dom4j.Element;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
public class  ResultMapperSegment extends AbstractSegment {
    private String className;
    private Class<?> resultClass;
    private Map<String, ImmutablePair<String, Const.CLASSTYPE>> columnMapper = new HashMap<>();

    public ResultMapperSegment(String nameSpace, String id, String value) {
        super(nameSpace, id, value);

    }

    public void parse(Element element) {
        className=element.attributeValue("resultType");
        if(!ObjectUtils.isEmpty(className)) {
            try {
                resultClass = Class.forName(className);
            } catch (ClassNotFoundException ex) {
                throw new ConfigurationIncorrectException("can not load resultMap class " + className);
            }
        }
        List<Element> elements = element.elements();
        for (Element ele : elements) {
            String column = ele.attributeValue("column");
            String prop = ele.attributeValue("property");
            String type = ele.attributeValue("jdbcType");
            columnMapper.put(column,new ImmutablePair<>(prop,getMetaType(type)));
        }
    }

    private Const.CLASSTYPE getMetaType(String jdbcType) {
        Const.CLASSTYPE metaType = null;
        switch (jdbcType.toUpperCase()) {
            case "CHAR":
            case "VARCHAR":
                metaType = Const.CLASSTYPE.STRING;
                break;
            case "DOUBLE":
            case "NUMERIC":
                metaType = Const.CLASSTYPE.DOUBLE;
                break;
            case "BIGINT":
                metaType = Const.CLASSTYPE.LONG;
                break;
            case "INT":
                metaType = Const.CLASSTYPE.INTEGER;
                break;
            case "TIMESTAMP":
            case "DATE":
                metaType = Const.CLASSTYPE.TIMESTAMP;
                break;
            default:
                metaType = Const.CLASSTYPE.STRING;
                break;
        }
        return metaType;
    }

    public Map<String, ImmutablePair<String, Const.CLASSTYPE>> getColumnMapper() {
        return columnMapper;
    }

    public String getClassName() {
        return className;
    }
    public Class<?> getResultClass(){
        return resultClass;
    }

    @Override
    public String getSqlPart(Map<String, Object> params, Map<String, ImmutablePair<String, List<AbstractSegment>>> segmentsMap) {
        return null;
    }
}
