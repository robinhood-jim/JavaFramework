package com.robin.core.query.util;


public interface ICondition {
	
    public Object getValue();

    public Object[] getValues();

    public String getState();
    
    public String getName();
}