package com.robin.core.base.dao.util;

import cn.hutool.core.util.ObjectUtil;
import com.robin.core.base.annotation.MappingField;
import lombok.Data;
import org.springframework.util.ObjectUtils;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

@Data
public class FieldContent {
    private String propertyName;
    private String fieldName;
    private String dataType;
    private String sequenceName;
    private boolean required;
    private boolean increment;
    private boolean sequential;
    private boolean primary;
    private boolean keyword;
    private Field field;
    private MethodHandle getMethod;
    private MethodHandle setMethod;
    private int scale;
    private int precise;
    private int length;
    private String defaultValue;
    private Object value;
    //composite primary keys
    private List<FieldContent> primaryKeys;

    public FieldContent() {

    }


    public FieldContent(String propertyName, String fieldName, Field field, MethodHandle getMethod, MethodHandle setMethod) {
        this.propertyName = propertyName;
        this.fieldName = fieldName;
        this.field = field;
        this.getMethod = getMethod;
        this.setMethod = setMethod;
    }
    public static class Builder{
        private final FieldContent content=new FieldContent();
        public Builder setPropertyName(String propertyName){
            content.setPropertyName(propertyName);
            return this;
        }
        public Builder setFieldName(String fieldName){
            content.setFieldName(fieldName);
            return this;
        }
        public Builder setGetMethod(MethodHandle getMethod){
            content.setGetMethod(getMethod);
            return this;
        }
        public Builder setSetMethod(MethodHandle setMethod){
            content.setSetMethod(setMethod);
            return this;
        }
        public Builder setField(Field field){
            content.setField(field);
            return this;
        }
        public Builder setDataType(String dataType){
            content.setDataType(dataType);
            return this;
        }
        public Builder setSequenceName(String sequenceName){
            content.setSequenceName(sequenceName);
            return this;
        }
        public Builder setRequired(boolean required){
            content.setRequired(required);
            return this;
        }
        public Builder setIncrement(boolean increment){
            content.setIncrement(increment);
            return this;
        }
        public Builder setSequential(boolean sequential){
            content.setSequential(sequential);
            return this;
        }
        public Builder setPrimary(boolean primary){
            content.setPrimary(primary);
            return this;
        }
        public Builder setScale(int scale){
            content.setScale(scale);
            return this;
        }
        public Builder setPrecise(int precise){
            content.setPrecise(precise);
            return this;
        }
        public Builder setLength(int length){
            content.setLength(length);
            return this;
        }
        public Builder setDefaultValue(String value){
            content.setDefaultValue(value);
            return this;
        }
        public Builder withMappingField(MappingField mapfield){
            String fieldName=null;

            if(!ObjectUtil.isEmpty(mapfield)) {
                String colfield = mapfield.value();
                if (!ObjectUtils.isEmpty(colfield)) {
                    fieldName = colfield;
                }
                if (mapfield.precise() != 0) {
                    content.setPrecise(mapfield.precise());
                }
                if (mapfield.scale() != 0) {
                    content.setScale(mapfield.scale());
                }
                if (mapfield.length() != 0) {
                    content.setLength(mapfield.length());
                }
                if (mapfield.increment()) {
                    content.setIncrement(true);
                }
                if (mapfield.primary()) {
                    content.setPrimary(true);
                }
                if (!ObjectUtils.isEmpty(mapfield.sequenceName())) {
                    content.setSequential(true);
                    content.setSequenceName(mapfield.sequenceName());
                }
                content.setPropertyName(fieldName);
                content.setKeyword(mapfield.keyword());
                content.setDataType(mapfield.datatype());
            }
            return this;
        }
        public FieldContent build(){
            return content;
        }


    }


}
