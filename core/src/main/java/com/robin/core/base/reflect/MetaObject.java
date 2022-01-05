package com.robin.core.base.reflect;

import com.robin.core.base.dao.util.AnnotationRetriver;

import java.util.Map;


public class MetaObject {
    private final Object originalObject;
    private final Map<String, AnnotationRetriver.FieldContent> fieldContentMap;
    public MetaObject(Object object,Map<String, AnnotationRetriver.FieldContent> contentMap){
        this.originalObject=object;
        this.fieldContentMap=contentMap;
    }

    public Object getOriginalObject() {
        return originalObject;
    }

    public Map<String, AnnotationRetriver.FieldContent> getFieldContentMap() {
        return fieldContentMap;
    }
    public Object getValue(String columnName){
        try {
            if (fieldContentMap.containsKey(columnName)) {
                return fieldContentMap.get(columnName).getGetMethod().invoke(originalObject, null);
            }
        }catch (Exception ex){

        }
        return null;
    }
    public void setValue(String fieldName,Object fieldValue){
        try{
            if(fieldContentMap.containsKey(fieldName)){
                fieldContentMap.get(fieldName).getSetMethod().invoke(originalObject,fieldValue);
            }
        }catch (Exception ex){

        }
    }
    public boolean hasGetter(String columnName){
        return fieldContentMap.containsKey(columnName) && fieldContentMap.get(columnName).getGetMethod()!=null;
    }
    public boolean hasSetter(String columnName){
        return fieldContentMap.containsKey(columnName) && fieldContentMap.get(columnName).getSetMethod()!=null;
    }
}
