package com.robin.core.sql.util;

import com.robin.core.base.dao.util.AnnotationRetriever;
import com.robin.core.base.dao.util.FieldContent;
import com.robin.core.base.dao.util.PropertyFunction;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.util.Const;
import lombok.Data;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class FilterCondition {
    private String prefixOper;
    private String suffixOper = Const.LINKOPERATOR.LINK_AND.getValue();
    private Const.OPERATOR operator;
    private String columnCode;
    private Object value;
    private List<?> values;
    private List<FilterCondition> conditions;
    private FilterCondition condition;
    private String columnType;
    private Const.LINKOPERATOR linkOper = Const.LINKOPERATOR.LINK_AND;
    private Class<? extends BaseObject> mappingClass;
    private String orderByStr;


    public FilterCondition(String columnCode,Const.OPERATOR operator){
        this.columnCode=columnCode;
        this.operator=operator;
    }
    public FilterCondition(String columnCode,String columnType,Const.OPERATOR operator){
        this.columnCode=columnCode;
        this.columnType=columnType;
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

    public FilterCondition(String columnCode, String columnType, Const.OPERATOR operator, Object value) {
        this.columnCode = columnCode;
        this.operator = operator;
        this.value = value;
        this.columnType = columnType;
    }

    public <T extends BaseObject> FilterCondition(PropertyFunction<T, ?> function,Class<T> clazz,Const.OPERATOR operator, Object value) {
        String columnCode = AnnotationRetriever.getFieldName(function);
        Map<String,FieldContent> map= AnnotationRetriever.getMappingFieldsMapCache(clazz);
        Assert.notNull(map.get(columnCode),"columnCode "+columnCode+" not have mapping");
        this.columnCode = map.get(columnCode).getFieldName();
        this.operator = operator;
        this.mappingClass=clazz;
        if(Collection.class.isAssignableFrom(value.getClass())){
            this.values=(List<?>)value;
        }else {
            this.value = value;
        }
    }

    public FilterCondition(String columnCode,Const.OPERATOR operator, List<FilterCondition> conditions) {
        this.columnCode = columnCode;
        this.operator = operator;
        this.conditions = conditions;
    }
    public FilterCondition(Class<? extends BaseObject> clazz,Const.LINKOPERATOR linkOper,List<FilterCondition> conditions){
        this.linkOper=linkOper;
        this.conditions=conditions;
        this.mappingClass=clazz;
    }


    public <T extends BaseObject> FilterCondition(PropertyFunction<T, ?> function,Class<T> clazz, Const.OPERATOR operator, List<FilterCondition> values) {
        String columnCode = AnnotationRetriever.getFieldName(function);
        Map<String,FieldContent> map= AnnotationRetriever.getMappingFieldsMapCache(clazz);
        Assert.notNull(map.get(columnCode),"columnCode "+columnCode+" not have mapping");
        this.columnCode = map.get(columnCode).getFieldName();
        this.mappingClass=clazz;
        this.operator = operator;
        this.conditions = values;
    }
    public <T extends BaseObject> FilterCondition(PropertyFunction<T, ?> function,Class<T> clazz, FilterCondition condition) {
        String columnCode = AnnotationRetriever.getFieldName(function);
        Map<String,FieldContent> map= AnnotationRetriever.getMappingFieldsMapCache(clazz);
        Assert.notNull(map.get(columnCode),"columnCode "+columnCode+" not have mapping");
        this.columnCode = map.get(columnCode).getFieldName();
        this.mappingClass=clazz;
        this.setCondition(condition);
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

    public String toPreparedSQLPart(List<Object> params) {
        StringBuilder sbSQLStr = new StringBuilder();
        String realColumn = columnCode;
        if (ObjectUtils.isEmpty(value) && ObjectUtils.isEmpty(values) && !CollectionUtils.isEmpty(getConditions())) {
            if (Const.LINKOPERATOR.LINK_OR.equals(getLinkOper())) {
                sbSQLStr.append("(");
            }
            for (int i = 0; i < getConditions().size(); i++) {
                sbSQLStr.append(getConditions().get(i).toPreparedSQLPart(params));
                if (i < getConditions().size() - 1) {
                    sbSQLStr.append(getConditions().get(i+1).getLinkOper().getSignal());
                }
            }
            if (Const.LINKOPERATOR.LINK_OR.equals(getLinkOper())) {
                sbSQLStr.append(")");
            }
        }else {
            switch (operator) {
                case BETWEEN:
                    Assert.notNull(values, "");
                    Assert.isTrue(!CollectionUtils.isEmpty(values) && values.size() >= 2, "");
                    sbSQLStr.append(realColumn);
                    sbSQLStr.append(" between ? and ? ");
                    params.add(getConditions().get(0));
                    params.add(getConditions().get(1));
                    break;
                case LIKE:
                case RLIKE:
                case LLIKE:
                    sbSQLStr.append(realColumn);
                    sbSQLStr.append(" like ?");
                    if (Const.OPERATOR.LLIKE.equals(operator)) {
                        params.add(getValue() + "%");
                    } else if (Const.OPERATOR.RLIKE.equals(operator)) {
                        params.add("%" + getValue());
                    } else {
                        params.add("%" + getValue() + "%");
                    }
                    break;
                case EQ:
                case NE:
                case GT:
                case LT:
                case LE:
                case GE:
                    Assert.notNull(value, "");
                    sbSQLStr.append(realColumn);
                    sbSQLStr.append(operator.getSignal()).append("?");
                    params.add(getValue());
                    break;
                case NOTNULL:
                    sbSQLStr.append(realColumn);
                    sbSQLStr.append(" is not null ");
                    break;
                case NULL:
                    sbSQLStr.append(realColumn);
                    sbSQLStr.append(" is null ");
                    break;
                case IN:
                case NOTIN:
                    sbSQLStr.append(realColumn);
                    if (Const.OPERATOR.IN.equals(operator)) {
                        sbSQLStr.append(" in (");
                    } else {
                        sbSQLStr.append(" not in (");
                    }
                    if(!ObjectUtils.isEmpty(values)) {
                        for (int i = 0; i < values.size(); i++) {
                            if (i != 0) {
                                sbSQLStr.append(",");
                            }
                            sbSQLStr.append("?");
                        }
                        params.add(values);
                    }else if(!ObjectUtils.isEmpty(getCondition())){
                        parseConditions(sbSQLStr,params);
                    }
                    sbSQLStr.append(")");
                    break;
                case NOT:
                    if (!ObjectUtils.isEmpty(value)) {
                        sbSQLStr.append(" not (");
                        parseConditions(sbSQLStr, params);
                        sbSQLStr.append(") ");
                    }
                    break;
                case EXISTS:
                    if (!ObjectUtils.isEmpty(value)) {
                        sbSQLStr.append(" exists (");
                        parseConditions(sbSQLStr, params);
                        sbSQLStr.append(") ");
                    }
                    break;
                case NOTEXIST:
                    if (!ObjectUtils.isEmpty(value)) {
                        sbSQLStr.append(" not exists (");
                        parseConditions(sbSQLStr, params);
                        sbSQLStr.append(") ");
                    }
                    break;
                default:
                    sbSQLStr.append(realColumn);
                    sbSQLStr.append("=?");
            }
        }

        return sbSQLStr.toString();
    }

    private void parseConditions(StringBuilder sbSQLStr,List<Object> params) {
        if(!CollectionUtils.isEmpty(getConditions())){
            for(int i=0;i<getConditions().size();i++) {
                if(Const.LINKOPERATOR.LINK_OR.equals(getConditions().get(i).getLinkOper())){
                    sbSQLStr.append(" OR (");
                }
                sbSQLStr.append(getConditions().get(i).toPreparedSQLPart(params));
                if(i<=getConditions().size()-1){
                    sbSQLStr.append(getConditions().get(i).getLinkOper().getSignal());
                }
                if(Const.LINKOPERATOR.LINK_OR.equals(getConditions().get(i).getLinkOper())){
                    sbSQLStr.append("(");
                }
            }
        }else if(!ObjectUtils.isEmpty(getCondition()) && !ObjectUtils.isEmpty(getCondition().getCondition())){
            sbSQLStr.append(Const.SQL_SELECT).append(getCondition().getColumnCode()).append(Const.SQL_FROM);
            AnnotationRetriever.EntityContent tableDef = AnnotationRetriever.getMappingTableByCache(getCondition().getMappingClass());
            sbSQLStr.append(tableDef.getTableName()).append(Const.SQL_WHERE);
            sbSQLStr.append(getCondition().getCondition().toPreparedSQLPart(params));
        }
    }


}
