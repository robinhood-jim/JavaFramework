package com.robin.core.sql.util;

import com.robin.core.base.dao.util.FieldContent;
import com.robin.core.base.exception.ConfigurationIncorrectException;
import com.robin.core.base.util.Const;
import lombok.Data;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class FilterCondition {
	private String prefixOper;
	private String suffixOper= Const.OPERATOR.LINK_AND.getValue();
	private Const.OPERATOR operator;
	private String columnCode;
	private Object value;
	private List<?> values;


	private Map<String, FieldContent> fieldMap;
	public Map<String, FieldContent> getFieldMap() {
		return fieldMap;
	}

	public void setFieldMap(Map<String, FieldContent> fieldMap) {
		this.fieldMap = fieldMap;
	}

	public FilterCondition(String columnCode,Const.OPERATOR operator){
		this.operator = operator;
		this.columnCode = columnCode;
	}

	public FilterCondition(String columnCode,Const.OPERATOR operator,Object value){
		this.columnCode = columnCode;
		this.operator = operator;
		this.value = value;
	}
	public FilterCondition(String columnCode,Const.OPERATOR operator,List<?> values){
		this.columnCode = columnCode;
		this.operator = operator;
		this.values = values;
	}
	public FilterCondition(String columnCode,Const.OPERATOR operator,Object value,String suffixOper){
		this.columnCode = columnCode;
		this.operator = operator;
		this.value = value;
		this.suffixOper=suffixOper;
	}
	public FilterCondition(String columnCode,Const.OPERATOR operator,List<?> values,String suffixOper){
		this.columnCode = columnCode;
		this.operator = operator;
		this.values = values;
		this.suffixOper=suffixOper;
	}
	public FilterCondition(String columnCode,Const.OPERATOR operator,Object value,String prefixOper,String suffixOper){
		this.columnCode = columnCode;
		this.operator = operator;
		this.value = value;
		this.prefixOper=prefixOper;
		this.suffixOper=suffixOper;
	}
	public FilterCondition(String columnCode,Const.OPERATOR operator,List<?> values,String prefixOper,String suffixOper){
		this.columnCode = columnCode;
		this.operator = operator;
		this.values = values;
		this.prefixOper=prefixOper;
		this.suffixOper=suffixOper;
	}
	
	public String toSQLPart()
	{
		StringBuilder sbSQLStr = new StringBuilder();
		String realColumn=columnCode;
		if(fieldMap.containsKey(columnCode)) {
            realColumn=fieldMap.get(columnCode).getFieldName();
        }
		switch (operator){
			case BETWEEN:
				if(ArrayList.class.isAssignableFrom(value.getClass())) {
					List values=(List) value;
					if (values.size() == 2) {
						sbSQLStr.append(" (");
						sbSQLStr.append(realColumn);
						sbSQLStr.append(" between ? and ?) ");
					}
				}
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
				Assert.notNull(value,"");
				sbSQLStr.append(" (");
				sbSQLStr.append(realColumn);
				sbSQLStr.append(" in (");
				if(ArrayList.class.isAssignableFrom(value.getClass())) {
					List<?> values = (List<?>) value;
					for (int i = 0; i < values.size(); i++) {
						if (i != 0) {
							sbSQLStr.append(",");
						}
						sbSQLStr.append("?");
					}
				}else{
					sbSQLStr.append("?");
				}
				sbSQLStr.append(")) ");
				break;
			case NOTIN:
				Assert.notNull(value,"");
				sbSQLStr.append(" (");
				sbSQLStr.append(realColumn);
				sbSQLStr.append(" not in (");
				if(ArrayList.class.isAssignableFrom(value.getClass())) {
					List<?> values1 = (List<?>) value;
					for (int i = 0; i < values1.size(); i++) {
						if (i != 0) {
							sbSQLStr.append(",");
						}
						sbSQLStr.append("?");
					}
				}else{
					sbSQLStr.append("?");
				}
				sbSQLStr.append(")) ");
				break;
			case LINK_OR:
				Assert.isTrue(!ObjectUtils.isEmpty(values),"");
				if(ArrayList.class.isAssignableFrom(value.getClass())) {
					List<FilterCondition> filterConditions=(List<FilterCondition>) value;
					sbSQLStr.append("(");
					for (int i = 0; i < filterConditions.size(); i++) {
						filterConditions.get(i).setFieldMap(fieldMap);
						sbSQLStr.append(filterConditions.get(i).toSQLPart());
						if (i != filterConditions.size() - 1) {
							sbSQLStr.append(filterConditions.get(i).getSuffixOper());
						}
					}
					sbSQLStr.append("(");
				}else{
					throw new ConfigurationIncorrectException(" OR operator must include list elements");
				}
				sbSQLStr.append(") ");
				break;
			case LINK_AND:
				Assert.notNull(value,"");
				if(ArrayList.class.isAssignableFrom(value.getClass())){
					sbSQLStr.append("(");
					List<FilterCondition> filterConditions1=(List<FilterCondition>) value;
					for (int i = 0; i < filterConditions1.size(); i++) {
						filterConditions1.get(i).setFieldMap(fieldMap);
						sbSQLStr.append(filterConditions1.get(i).toSQLPart());
						if (i != filterConditions1.size() - 1) {
							sbSQLStr.append(filterConditions1.get(i).getSuffixOper());
						}
					}
					sbSQLStr.append(")");
				}else{
					throw new ConfigurationIncorrectException(" AND operator must include list elements");
				}
				break;
			case NOT:
				if (!ObjectUtils.isEmpty(value)){
					sbSQLStr.append(" (not (");
					sbSQLStr.append(((FilterCondition)value).toSQLPart());
					sbSQLStr.append(")) ");
				}
				break;
			case EXISTS:
				if (!ObjectUtils.isEmpty(value)){
					sbSQLStr.append(" (exists (");
					sbSQLStr.append(((FilterCondition)value).toSQLPart());
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
				if(ArrayList.class.isAssignableFrom(value.getClass())) {
					List<?> values = (List<?>) value;
					if (values.size() == 2) {
						objList.add(values.get(0));
						objList.add(values.get(1));
					}
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
				if(ArrayList.class.isAssignableFrom(value.getClass())){
					List<?> values=(List<?>)value;
					values.forEach(f->objList.add(f));
				}else{
					objList.add(value);
				}
				break;
			case LINK_AND:
			case LINK_OR:
				List<FilterCondition> filterConditions=(List<FilterCondition>) value;
				filterConditions.forEach(f->f.fillValue(objList));
				break;
			case NOT:
			case EXISTS:
				FilterCondition condition=(FilterCondition) value;
				condition.fillValue(objList);
				break;
			default:
				objList.add(getValue());
		}
	}

	
}
