package com.robin.core.sql.util;

import com.robin.core.base.dao.util.AnnotationRetriever;
import com.robin.core.base.dao.util.FieldContent;
import com.robin.core.base.dao.util.PropertyFunction;
import com.robin.core.base.exception.ConfigurationIncorrectException;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.util.Const;
import lombok.Data;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data
public class FilterCondition {
    private String prefixOper;
    private String suffixOper = Const.OPERATOR.LINK_AND.getValue();
    private Const.OPERATOR operator;
    private String columnCode;
    private Object value;
    private List<?> values;
    private List<FilterCondition> conditions;
    private String columnType;
    private String linkOper = Const.OPERATOR.LINK_AND.getSignal();
    private Class<? extends BaseObject> mappingClass;


    private Map<String, FieldContent> fieldMap;

    public Map<String, FieldContent> getFieldMap() {
        return fieldMap;
    }

    public void setFieldMap(Map<String, FieldContent> fieldMap) {
        this.fieldMap = fieldMap;
    }

    public FilterCondition(String columnCode,Const.OPERATOR operator){
        this.columnCode=columnCode;
        this.operator=operator;
    }
    public FilterCondition(Const.OPERATOR operator, String value) {
        this.operator = operator;
        this.value = value;
    }


    public FilterCondition(String columnCode, Const.OPERATOR operator, Object value) {
        this.columnCode = columnCode;
        this.operator = operator;
        this.value = value;
    }

    public FilterCondition(String columnCode, Const.OPERATOR operator, Object value, String columnType) {
        this.columnCode = columnCode;
        this.operator = operator;
        this.value = value;
        this.columnType = columnType;
    }

    public <T extends BaseObject> FilterCondition(PropertyFunction<T, ?> function, Const.OPERATOR operator, Object value) {
        String columnCode = AnnotationRetriever.getFieldName(function);
        this.columnCode = columnCode;
        this.operator = operator;
        this.value = value;
    }

    public FilterCondition(String columnCode, Const.OPERATOR operator, List<FilterCondition> conditions) {
        this.columnCode = columnCode;
        this.operator = operator;
        this.conditions = conditions;
    }


    public <T extends BaseObject> FilterCondition(PropertyFunction<T, ?> function, Const.OPERATOR operator, List<FilterCondition> values) {
        String columnCode = AnnotationRetriever.getFieldName(function);
        this.columnCode = columnCode;
        this.operator = operator;
        this.conditions = values;
    }


    public FilterCondition(String columnCode, Const.OPERATOR operator, List<FilterCondition> values, String prefixOper, String suffixOper) {
        this.columnCode = columnCode;
        this.operator = operator;
        this.conditions = values;
        this.prefixOper = prefixOper;
        this.suffixOper = suffixOper;
    }

    public FilterCondition(Const.OPERATOR operator, List<FilterCondition> values, String prefixOper, String suffixOper) {
        this.operator = operator;
        this.conditions = values;
        this.prefixOper = prefixOper;
        this.suffixOper = suffixOper;
    }

    public FilterCondition(Const.OPERATOR operator, List<FilterCondition> values) {
        this.operator = operator;
        this.conditions = values;
    }

    public FilterCondition(Const.OPERATOR operator, List<FilterCondition> values, Class<? extends BaseObject> mappingClass) {
        this.operator = operator;
        if (!CollectionUtils.isEmpty(values)) {
            values.forEach(f -> f.setMappingClass(mappingClass));
        }
        this.conditions = values;
        this.mappingClass = mappingClass;
    }

    public String toPreparedSQLPart() {
        StringBuilder sbSQLStr = new StringBuilder();
        String realColumn = columnCode;
        if (fieldMap.containsKey(columnCode)) {
            realColumn = fieldMap.get(columnCode).getFieldName();
        }
        switch (operator) {
            case BETWEEN:
                Assert.notNull(values, "");
                sbSQLStr.append(" (");
                sbSQLStr.append(realColumn);
                sbSQLStr.append(" between ? and ?) ");
                break;
            case LIKE:
            case RLIKE:
            case LLIKE:
                sbSQLStr.append(" (");
                sbSQLStr.append(realColumn);
                sbSQLStr.append(" like ?)");
                break;
            case EQ:
                sbSQLStr.append(" (");
                sbSQLStr.append(realColumn);
                sbSQLStr.append("=?) ");
                break;
            case NE:
                sbSQLStr.append(" (");
                sbSQLStr.append(realColumn);
                sbSQLStr.append("<>?) ");
                break;
            case GT:
                sbSQLStr.append(" (");
                sbSQLStr.append(realColumn);
                sbSQLStr.append(">?) ");
                break;
            case LT:
                sbSQLStr.append(" (");
                sbSQLStr.append(realColumn);
                sbSQLStr.append("<?) ");
                break;
            case LE:
                sbSQLStr.append(" (");
                sbSQLStr.append(realColumn);
                sbSQLStr.append("<=?) ");
                break;
            case GE:
                sbSQLStr.append(" (");
                sbSQLStr.append(realColumn);
                sbSQLStr.append(">=?) ");
                break;
            case NOTNULL:
                sbSQLStr.append(" (");
                sbSQLStr.append(realColumn);
                sbSQLStr.append(" is not null) ");
                break;
            case NULL:
                sbSQLStr.append(" (");
                sbSQLStr.append(realColumn);
                sbSQLStr.append(" is null) ");
                break;
            case IN:
            case NOTIN:
                Assert.notNull(values, "");
                sbSQLStr.append(" (");
                sbSQLStr.append(realColumn);
                if (Const.OPERATOR.IN.equals(operator)) {
                    sbSQLStr.append(" in (");
                } else {
                    sbSQLStr.append(" not in (");
                }
                for (int i = 0; i < values.size(); i++) {
                    if (i != 0) {
                        sbSQLStr.append(",");
                    }
                    sbSQLStr.append("?");
                }

                sbSQLStr.append(")) ");
                break;
            case LINK_OR:
                Assert.isTrue(!ObjectUtils.isEmpty(conditions), "");
                if (!CollectionUtils.isEmpty(conditions)) {
                    sbSQLStr.append("(");
                    for (int i = 0; i < conditions.size(); i++) {
                        conditions.get(i).setFieldMap(fieldMap);
                        sbSQLStr.append(conditions.get(i).toPreparedSQLPart());
                        if (i != conditions.size() - 1) {
                            sbSQLStr.append(conditions.get(i).getSuffixOper());
                        }
                    }
                    sbSQLStr.append("(");
                } else {
                    throw new ConfigurationIncorrectException(" OR operator must include list elements");
                }
                sbSQLStr.append(") ");
                break;
            case LINK_AND:
                Assert.notNull(conditions, "");
                sbSQLStr.append("(");
                for (int i = 0; i < conditions.size(); i++) {
                    conditions.get(i).setFieldMap(fieldMap);
                    sbSQLStr.append(conditions.get(i).toPreparedSQLPart());
                    if (i != conditions.size() - 1) {
                        sbSQLStr.append(conditions.get(i).getSuffixOper());
                    }
                }
                sbSQLStr.append(")");

                break;
            case NOT:
                if (!ObjectUtils.isEmpty(value)) {
                    sbSQLStr.append(" (not (");
                    sbSQLStr.append(value.toString());
                    sbSQLStr.append(")) ");
                }
                break;
            case EXISTS:
                if (!ObjectUtils.isEmpty(value)) {
                    sbSQLStr.append(" (exists (");
                    sbSQLStr.append(value.toString());
                    sbSQLStr.append(")) ");
                }
                break;
            case NOTEXIST:
                if (!ObjectUtils.isEmpty(value)) {
                    sbSQLStr.append(" (not exists (");
                    sbSQLStr.append(value.toString());
                    sbSQLStr.append(")) ");
                }
                break;
            default:
                sbSQLStr.append(" (");
                sbSQLStr.append(realColumn);
                sbSQLStr.append("=?) ");
        }

        return sbSQLStr.toString();
    }
    @SuppressWarnings("unchecked")
    public void fillValue(List<Object> objList) {
        switch (getOperator()) {
            case LIKE:
                objList.add("%" + getValue() + "%");
                break;
            case LLIKE:
                objList.add("%" + getValue());
                break;
            case RLIKE:
                objList.add(getValue() + "%");
                break;
            case BETWEEN:
                if (!CollectionUtils.isEmpty(values) && values.size() == 2) {
                    objList.add(values.get(0));
                    objList.add(values.get(1));
                }
                break;
            case EQ:
            case GT:
            case LT:
            case NE:
            case LE:
            case GE:
                objList.add(getValue());
                break;
            case IN:
            case NOTIN:
                Optional.ofNullable(getValues()).get().forEach(o -> objList.add(o));
                break;
            case LINK_AND:
            case LINK_OR:
                List<FilterCondition> filterConditions = (List<FilterCondition>) value;
                filterConditions.forEach(f -> f.fillValue(objList));
                break;
            case NOT:
            case EXISTS:

                break;
            default:
                objList.add(getValue());
        }
    }

}
