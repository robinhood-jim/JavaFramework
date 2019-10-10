package com.robin.core.sql.util;

import com.robin.core.base.dao.util.AnnotationRetrevior;

import java.util.Map;

public class FilterCondition {
	private String prefixOper;
	private String suffixOper;
	private String operator;
	private String anOrOper;
	private String columnCode;
	private Object[] values;
	private Object value;
	public static final String BETWEEN = "BETWEEN";
//	public static final String BETWEEN_AND_EQUALS = "BETWEEN_AND_EQUALS";
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
	public static final String NOTIN = "NOTIN";
	private Map<String, AnnotationRetrevior.FieldContent> fieldMap;
	public Map<String, AnnotationRetrevior.FieldContent> getFieldMap() {
		return fieldMap;
	}

	public void setFieldMap(Map<String, AnnotationRetrevior.FieldContent> fieldMap) {
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
	

	public FilterCondition(String columnCode,String operator,Object[] values){
		this.columnCode = columnCode;
		this.operator = operator;
		this.values = values;
	}
	
	public String toSQLPart()
	{
		StringBuffer sbSQLStr = new StringBuffer();
		String realColumn=columnCode;
		if(fieldMap.containsKey(columnCode))
			realColumn=fieldMap.get(columnCode).getFieldName();
		if (BETWEEN.equals(operator)){
			if (values.length < 2){
			}
			sbSQLStr.append(" (");
			sbSQLStr.append(realColumn);
			sbSQLStr.append(" between ? and ?) ");
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
			if (values.length < 1 || values[0] == null){
			}
			sbSQLStr.append(" (");
			sbSQLStr.append(realColumn);
			sbSQLStr.append(" in (");
            for (int i=0; i<values.length; i++) {
            	if (i != 0) {
            		sbSQLStr.append(",");
            	}
    			sbSQLStr.append("?");
            }
			sbSQLStr.append(")) ");
		}else if(NOTIN.equals(operator)){
			if (values.length < 1 || values[0] == null){
			}
			sbSQLStr.append(" (");
			sbSQLStr.append(realColumn);
			sbSQLStr.append(" not in (");
            for (int i=0; i<values.length; i++) {
            	if (i != 0) {
            		sbSQLStr.append(",");
            	}
    			sbSQLStr.append("?");
            }
			sbSQLStr.append(")) ");
		}
		else 
		if (OR == operator){
			if(values==null){
				//throw new Exception("must define");
			}
			sbSQLStr.append("(");
			for (int i = 0; i < values.length; i++) {
				FilterCondition cond=(FilterCondition)values[i];
				cond.setFieldMap(fieldMap);
				sbSQLStr.append(cond.toSQLPart());
				if(i!=values.length-1)
					sbSQLStr.append(" and ");
			}
			sbSQLStr.append(") ");
		} else 
		if (NOT == operator){
			if (value == null){
			}
			sbSQLStr.append(" (not (");
			sbSQLStr.append(((FilterCondition)value).toSQLPart());
			sbSQLStr.append(")) ");
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

	public String getAnOrOper() {
		return anOrOper;
	}

	public void setAnOrOper(String anOrOper) {
		this.anOrOper = anOrOper;
	}

	public String getColumnCode() {
		return columnCode;
	}

	public void setColumnCode(String columnCode) {
		this.columnCode = columnCode;
	}

	public Object[] getValues() {
		return values;
	}

	public void setValues(Object[] values) {
		this.values = values;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}
	
}
