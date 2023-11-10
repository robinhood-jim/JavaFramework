package com.robin.core.base.reflect;

import io.github.classgraph.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.lang.NonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


public class ClassGraphReflector implements InitializingBean {
    private ScanResult scanResult;
    //default Scan package
    private String scanPackage="com.robin";


    @Override
    public void afterPropertiesSet() {
        scanResult=new ClassGraph().enableAllInfo().whitelistPackages(scanPackage).scan();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> scanResult.close()));
    }


    public void setScanPackage(String scanPackage) {
        this.scanPackage = scanPackage;
    }
    public Map<String,Method> returnGetMethods(Class<?> clazz){
        ClassInfo classInfo=scanResult.getClassInfo(clazz.getCanonicalName());
        final Map<String,Method> map=new HashMap<>();
        MethodInfoList mList=classInfo.getMethodInfo().filter(methodInfo -> methodInfo.getName().startsWith("get") && methodInfo.getParameterInfo().length==0);
        mList.forEach(methodInfo -> {
            String fieldName= StringUtils.uncapitalize(methodInfo.getName().substring(3));
            map.put(fieldName,methodInfo.loadClassAndGetMethod());
        });
        return map;
    }
    @NonNull
    public Map<String, Field> returnAllField(Class<?> clazz){
        ClassInfo classInfo=scanResult.getClassInfo(clazz.getCanonicalName());
        final Map<java.lang.String, java.lang.reflect.Field> map=new HashMap<>();
        classInfo.getFieldInfo().forEach(f->
            map.put(f.getName(),f.loadClassAndGetField())
        );
        return map;
    }
    public Map<String,Method> returnSetMethods(Class<?> clazz){
        ClassInfo classInfo=scanResult.getClassInfo(clazz.getCanonicalName());
        final Map<java.lang.String, java.lang.reflect.Method> map=new HashMap<>();
        MethodInfoList mList=classInfo.getMethodInfo().filter(methodInfo -> methodInfo.getName().startsWith("set") && methodInfo.getParameterInfo().length==1);
        mList.forEach(methodInfo -> {
            java.lang.String fieldName= StringUtils.uncapitalize(methodInfo.getName().substring(3));
            map.put(fieldName,methodInfo.loadClassAndGetMethod());
        });
        return map;
    }
    public FieldInfo returnField(Class<?> clazz,String fieldName){
        return scanResult.getClassInfo(clazz.getCanonicalName()).getFieldInfo(fieldName);
    }
    public ClassInfoList getAnnotationClasses(Class<?> clazz){
        return scanResult.getClassesWithAnnotation(clazz.getCanonicalName());
    }
    public ClassInfoList getClassesByAnnotationFields(Class<?> fieldClass){
        return scanResult.getClassesWithFieldAnnotation(fieldClass.getCanonicalName());
    }
    public boolean isAnnotationClassWithAnnotationFields(Class<?> clazz, Class<? extends Annotation> annotationClazz, Class<?> annotationFields){
        return scanResult.getClassInfo(clazz.getCanonicalName()).hasAnnotation(annotationClazz.getCanonicalName())
                && scanResult.getClassInfo(clazz.getCanonicalName()).hasFieldAnnotation(annotationFields.getCanonicalName());
    }


}
