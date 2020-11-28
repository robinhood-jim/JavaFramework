package com.robin.es.util;

public enum FieldType {
    Text("text"),
    Byte("byte"),
    Short("short"),
    Integer("integer"),
    Long("long"),
    Date("date"),
    Half_Float("half_float"),
    Float("float"),
    Double("double"),
    Boolean("boolean"),
    Object("object"),
    Auto("auto"),
    Nested("nested"),
    Keyword("keyword");
    private String value;
    FieldType(String value){
        this.value=value;
    }
    public String getValue(){
        return value;
    }
}
