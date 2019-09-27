/*
 * Copyright (c) 2015,robinjim(robinjim@126.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.robin.core.query.util;

public class Condition implements ICondition{
	
	public static final String BETWEEN = "BETWEEN";
//	public static final String BETWEEN_AND_EQUALS = "BETWEEN_AND_EQUALS";
	public static final String GREATNESS = "GREATNESS";
	public static final String SMALLNESS = "SMALLNESS";
	public static final String GREATNESS_AND_EQUALS = "GREATNESS_AND_EQUALS";
	public static final String SMALLNESS_AND_EQUALS = "SMALLNESS_AND_EQUALS";
	public static final String LIKE = "LIKE";
	public static final String EQUALS = "EQS";
    public static final String NOT_EQUALS = "NOT_EQUALS";
	public static final String IS_NOT_NULL = "NOT_NULL";
	public static final String IS_NULL = "IS_NULL";
	public static final String OR = "OR";
	public static final String IN = "IN";
	public static final String NOT = "NOT";

	private String name; 

	private Object value;

	private Object[] values;

	private String state;

	public Condition(String name,String state){
		this.state = state;
		this.name = name;
	}

	public Condition(String name,String state,Object value){
		this.name = name;
		this.state = state;
		this.value = value;
	}
	

	public Condition(String name,String state,Object[] values){
		this.name = name;
		this.state = state;
		this.values = values;
	}
	
	
	
	public String toSQLString(String tablename) {
		StringBuffer sbSQLStr = new StringBuffer();
		if (BETWEEN == state){
			if (values.length < 2){
				throw new RuntimeException("between must has least two value");
			}
			sbSQLStr.append(" (");
			sbSQLStr.append(tablename);
			sbSQLStr.append(".");
			sbSQLStr.append(name);
			sbSQLStr.append(" between ? and ?) ");
		} else 
		if (LIKE == state){
			sbSQLStr.append(" (");
			sbSQLStr.append(tablename);
			sbSQLStr.append(".");
			sbSQLStr.append(name);
			sbSQLStr.append(" like ?)");
		} else 
		if (EQUALS == state){
			sbSQLStr.append(" (");
			sbSQLStr.append(tablename);
			sbSQLStr.append(".");
			sbSQLStr.append(name);
			sbSQLStr.append("=?) ");
		} else 
        if (NOT_EQUALS == state){
            sbSQLStr.append(" (");
            sbSQLStr.append(tablename);
            sbSQLStr.append(".");
            sbSQLStr.append(name);
            sbSQLStr.append("<>?) ");
        }else
		if (GREATNESS == state){
			sbSQLStr.append(" (");
			sbSQLStr.append(tablename);
			sbSQLStr.append(".");
			sbSQLStr.append(name);
			sbSQLStr.append(">?) ");
		}else 
		if (SMALLNESS == state){
			sbSQLStr.append(" (");
			sbSQLStr.append(tablename);
			sbSQLStr.append(".");
			sbSQLStr.append(name);
			sbSQLStr.append("<?) ");
		}else if (GREATNESS_AND_EQUALS == state){
			sbSQLStr.append(" (");
			sbSQLStr.append(tablename);
			sbSQLStr.append(".");
			sbSQLStr.append(name);
			sbSQLStr.append(">=?) ");
		}else 
		if (SMALLNESS_AND_EQUALS == state){
			sbSQLStr.append(" (");
			sbSQLStr.append(tablename);
			sbSQLStr.append(".");
			sbSQLStr.append(name);
			sbSQLStr.append("<=?) ");
		} else 
		if (IS_NOT_NULL == state){
			sbSQLStr.append(" (");
			sbSQLStr.append(tablename);
			sbSQLStr.append(".");
			sbSQLStr.append(name);
			sbSQLStr.append(" is not null) ");
		} else 
		if (IS_NULL == state){
			sbSQLStr.append(" (");
			sbSQLStr.append(tablename);
			sbSQLStr.append(".");
			sbSQLStr.append(name);
			sbSQLStr.append(" is null) ");
		} else 
		if (IN == state){
			if (values.length < 1 || values[0] == null){
				throw new RuntimeException("In must have at least one Value");
			}
			sbSQLStr.append(" (");
			sbSQLStr.append(tablename);
			sbSQLStr.append(".");
			sbSQLStr.append(name);
			sbSQLStr.append(" in (");
            for (int i=0; i<values.length; i++) {
            	if (i != 0) {
            		sbSQLStr.append(",");
            	}
    			sbSQLStr.append("?");
            }
			sbSQLStr.append(")) ");
		} else 
		if (OR == state){
			if (values.length < 2 || values[0] == null || values[0] == null){
				//throw new Exception("������ʾ:��ѯ��������Ϊ�գ�(in "+values+")");
			}
			sbSQLStr.append(" (");
			sbSQLStr.append(((Condition)values[0]).toSQLString(tablename));
			sbSQLStr.append(" or ");
			sbSQLStr.append(((Condition)values[1]).toSQLString(tablename));
			sbSQLStr.append(") ");
		} else 
		if (NOT == state){
			if (value == null){
				//throw new Exception("������ʾ:��ѯ��������Ϊ�գ�(in "+values+")");
			}
			sbSQLStr.append(" (not (");
			sbSQLStr.append(((Condition)value).toSQLString(tablename));
			sbSQLStr.append(")) ");
		}
		return sbSQLStr.toString();
	}
	public String toSQLPart() {
		StringBuffer sbSQLStr = new StringBuffer();
		if (BETWEEN == state){
			if (values.length < 2){
				//throw new Exception("������ʾ:��ѯ��������Ϊ�գ�(between "+values+")");
			}
			sbSQLStr.append(" (");
			sbSQLStr.append(name);
			sbSQLStr.append(" between ? and ?) ");
		} else 
		if (LIKE == state){
			sbSQLStr.append(" (");
			sbSQLStr.append(name);
			sbSQLStr.append(" like ?)");
		} else 
		if (EQUALS == state){
			sbSQLStr.append(" (");
			sbSQLStr.append(name);
			sbSQLStr.append("=?) ");
		} else 
        if (NOT_EQUALS == state){
            sbSQLStr.append(" (");
            sbSQLStr.append(name);
            sbSQLStr.append("<>?) ");
        }else
		if (GREATNESS == state){
			sbSQLStr.append(" (");
			sbSQLStr.append(name);
			sbSQLStr.append(">?) ");
		}else 
		if (SMALLNESS == state){
			sbSQLStr.append(" (");
			sbSQLStr.append(name);
			sbSQLStr.append("<?) ");
		}else if (GREATNESS_AND_EQUALS == state){
			sbSQLStr.append(" (");
			sbSQLStr.append(name);
			sbSQLStr.append(">=?) ");
		}else 
		if (SMALLNESS_AND_EQUALS == state){
			sbSQLStr.append(" (");
			sbSQLStr.append(name);
			sbSQLStr.append("<=?) ");
		} else 
		if (IS_NOT_NULL == state){
			sbSQLStr.append(" (");
			sbSQLStr.append(name);
			sbSQLStr.append(" is not null) ");
		} else 
		if (IS_NULL == state){
			sbSQLStr.append(" (");
			sbSQLStr.append(name);
			sbSQLStr.append(" is null) ");
		} else 
		if (IN == state){
			if (values.length < 1 || values[0] == null){
				//throw new Exception("������ʾ:��ѯ��������Ϊ�գ�(in "+values+")");
			}
			sbSQLStr.append(" (");
			sbSQLStr.append(name);
			sbSQLStr.append(" in (");
            for (int i=0; i<values.length; i++) {
            	if (i != 0) {
            		sbSQLStr.append(",");
            	}
    			sbSQLStr.append("?");
            }
			sbSQLStr.append(")) ");
		} else 
		if (OR == state){
			if (values.length < 2 || values[0] == null || values[0] == null){
				//throw new Exception("������ʾ:��ѯ��������Ϊ�գ�(in "+values+")");
			}
			sbSQLStr.append(" (");
			sbSQLStr.append(((Condition)values[0]).toSQLPart());
			sbSQLStr.append(" or ");
			sbSQLStr.append(((Condition)values[1]).toSQLPart());
			sbSQLStr.append(") ");
		} else 
		if (NOT == state){
			if (value == null){
				//throw new Exception("������ʾ:��ѯ��������Ϊ�գ�(in "+values+")");
			}
			sbSQLStr.append(" (not (");
			sbSQLStr.append(((Condition)value).toSQLPart());
			sbSQLStr.append(")) ");
		}
		return sbSQLStr.toString();
	}
	

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Object[] getValues() {
		return values;
	}

	public void setValues(Object[] values) {
		this.values = values;
	}
	
	public String getState(){
		return state;
	}
	
	public String getName(){
		return name;
	}
}
