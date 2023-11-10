package com.robin.core.sql.util;

import com.robin.core.base.dao.util.AnnotationRetriever;
import com.robin.core.base.exception.ConfigurationIncorrectException;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FilterCondition {
	private String prefixOper;
	private String suffixOper= AND;
	private String operator;
	private String columnCode;
	private Object value;
	public static final String BETWEEN = "BETWEEN";
	public static final String GREATNESS = "GREATNESS";
	public static final String SMALLNESS = "SMALLNESS";
	public static final String GREATNESS_AND_EQUALS = "GREATNESS_AND_EQUALS";
	public static final String SMALLNESS_AND_EQUALS = "SMALLNESS_AND_EQUALS";
	public static final String LIKE = "LIKE";
	public static final String LEFT_LIKE="LLIKE";
	public static final String RIGHT_LIKE="RLIKE";
	public static final String EQS = "EQS";
    public static final String NOT_EQUALS = "NOT_EQUALS";
	public static final String IS_NOT_NULL = "NOT_NULL";
	public static final String IS_NULL = "IS_NULL";
	public static final String OR = "OR";
	public static final String IN = "IN";
	public static final String NOT = "NOT";
	public static final String AND ="AND";
	public static final String NOTIN = "NOTIN";
	private Map<String, AnnotationRetriever.FieldContent> fieldMap;
	public Map<String, AnnotationRetriever.FieldContent> getFieldMap() {
		return fieldMap;
	}

	public void setFieldMap(Map<String, AnnotationRetriever.FieldContent> fieldMap) {
		this.fieldMap = fieldMap;
	}

	public FilterCondition(String columnCode,String operator){
		this.operator = operator;
		this.columnCode = columnCode;
	}

	public FilterCondition(String columnCode,String operator,Object value){
		this.columnCode = columnCode;
		this.operator = operator;
		this.value = value;
	}
	public FilterCondition(String columnCode,String operator,Object value,String suffixOper){
		this.columnCode = columnCode;
		this.operator = operator;
		this.value = value;
		this.suffixOper=suffixOper;
	}
	public FilterCondition(String columnCode,String operator,Object value,String prefixOper,String suffixOper){
		this.columnCode = columnCode;
		this.operator = operator;
		this.value = value;
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
		if (BETWEEN.equals(operator)){
			if(ArrayList.class.isAssignableFrom(value.getClass())) {
				List values=(List) value;
				if (values.size() == 2) {
					sbSQLStr.append(" (");
					sbSQLStr.append(realColumn);
					sbSQLStr.append(" between ? and ?) ");
				}
			}

		} else 
		if (LIKE .equals(operator) || LEFT_LIKE.equals(operator) || RIGHT_LIKE.equals(operator)){
			sbSQLStr.append(" (");
			sbSQLStr.append(realColumn);
			sbSQLStr.append(" like ?)");
		}
		else 
		if (EQS.equals(operator)){
			sbSQLStr.append(" (");
			sbSQLStr.append(realColumn);
			sbSQLStr.append("=?) ");
		} else 
        if (NOT_EQUALS.equals(operator)){
            sbSQLStr.append(" (");
            sbSQLStr.append(realColumn);
            sbSQLStr.append("<>?) ");
        }else
		if (GREATNESS.equals(operator)){
			sbSQLStr.append(" (");
			sbSQLStr.append(realColumn);
			sbSQLStr.append(">?) ");
		}else 
		if (SMALLNESS.equals(operator)){
			sbSQLStr.append(" (");
			sbSQLStr.append(realColumn);
			sbSQLStr.append("<?) ");
		}else if (GREATNESS_AND_EQUALS .equals(operator)){
			sbSQLStr.append(" (");
			sbSQLStr.append(realColumn);
			sbSQLStr.append(">=?) ");
		}else 
		if (SMALLNESS_AND_EQUALS.equals(operator)){
			sbSQLStr.append(" (");
			sbSQLStr.append(realColumn);
			sbSQLStr.append("<=?) ");
		} else 
		if (IS_NOT_NULL.equals(operator)){
			sbSQLStr.append(" (");
			sbSQLStr.append(realColumn);
			sbSQLStr.append(" is not null) ");
		} else 
		if (IS_NULL.equals(operator)){
			sbSQLStr.append(" (");
			sbSQLStr.append(realColumn);
			sbSQLStr.append(" is null) ");
		} else 
		if (IN.equals(operator)){
			Assert.notNull(value,"");
			sbSQLStr.append(" (");
			sbSQLStr.append(realColumn);
			sbSQLStr.append(" in (");
			List values=(List) value;
            for (int i=0; i<values.size(); i++) {
            	if (i != 0) {
            		sbSQLStr.append(",");
            	}
    			sbSQLStr.append("?");
            }
			sbSQLStr.append(")) ");
		}else if(NOTIN.equals(operator)){
			Assert.notNull(value,"");
			sbSQLStr.append(" (");
			sbSQLStr.append(realColumn);
			sbSQLStr.append(" not in (");
			List values=(List) value;
            for (int i=0; i<values.size(); i++) {
            	if (i != 0) {
            		sbSQLStr.append(",");
            	}
    			sbSQLStr.append("?");
            }
			sbSQLStr.append(")) ");
		}
		else 
		if (OR.equals(operator)){
			Assert.notNull(value,"");

			if(ArrayList.class.isAssignableFrom(value.getClass())) {
				List<FilterCondition> values=(List<FilterCondition>) value;
				sbSQLStr.append("(");
				for (int i = 0; i < values.size(); i++) {
					values.get(i).setFieldMap(fieldMap);
					sbSQLStr.append(values.get(i).toSQLPart());
					if (i != values.size() - 1) {
						sbSQLStr.append(values.get(i).getSuffixOper());
					}
				}
				sbSQLStr.append("(");
			}else{
				throw new ConfigurationIncorrectException(" OR operator must include list elements");
			}
			sbSQLStr.append(") ");
		}else if(AND.equals(operator)){
			Assert.notNull(value,"");
			if(ArrayList.class.isAssignableFrom(value.getClass())){
				sbSQLStr.append("(");
				List<FilterCondition> values=(List<FilterCondition>) value;
				for (int i = 0; i < values.size(); i++) {
					values.get(i).setFieldMap(fieldMap);
					sbSQLStr.append(values.get(i).toSQLPart());
					if (i != values.size() - 1) {
						sbSQLStr.append(values.get(i).getSuffixOper());
					}
				}
				sbSQLStr.append(")");
			}else{
				throw new ConfigurationIncorrectException(" AND operator must include list elements");
			}
		}
		else if (NOT.equals(operator)){
			if (!ObjectUtils.isEmpty(value)){
				sbSQLStr.append(" (not (");
				sbSQLStr.append(((FilterCondition)value).toSQLPart());
				sbSQLStr.append(")) ");
			}
		}
		return sbSQLStr.toString();
	}

	public String getPrefixOper() {
		return prefixOper;
	}

	public void setPrefixOper(String prefixOper) {
		this.prefixOper = prefixOper;
	}

	public String getSuffixOper() {
		return suffixOper;
	}

	public void setSuffixOper(String suffixOper) {
		this.suffixOper = suffixOper;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}


	public String getColumnCode() {
		return columnCode;
	}

	public void setColumnCode(String columnCode) {
		this.columnCode = columnCode;
	}


	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}
	
}
