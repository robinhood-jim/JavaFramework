package com.robin.core.query.util;


public interface ICondition {
	
    Object getValue();

    Object[] getValues();

    String getState();
    
    String getName();
}