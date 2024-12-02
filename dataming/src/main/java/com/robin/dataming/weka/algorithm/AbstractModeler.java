package com.robin.dataming.weka.algorithm;

import com.robin.core.base.exception.MissingConfigException;
import com.robin.dataming.weka.utils.WekaUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.clusterers.AbstractClusterer;
import weka.clusterers.Clusterer;
import weka.core.Instances;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

@Slf4j
public abstract class AbstractModeler<T> {
     Class<T> clazz;
     T model;
     protected AbstractModeler(){
          Type genericSuperClass = getClass().getGenericSuperclass();
          ParameterizedType parametrizedType;
          if (genericSuperClass instanceof ParameterizedType) { // class
               parametrizedType = (ParameterizedType) genericSuperClass;
          } else if (genericSuperClass instanceof Class) { // in case of CGLIB proxy
               parametrizedType = (ParameterizedType) ((Class<?>) genericSuperClass).getGenericSuperclass();
          } else {
               throw new IllegalStateException("class " + getClass() + " is not subtype of ParametrizedType.");
          }
          clazz = (Class<T>) parametrizedType.getActualTypeArguments()[0];
          try{
               model=clazz.newInstance();
          }catch (IllegalAccessException |InstantiationException ex){
               log.info("{}",ex.getMessage());
          }
     }
     public abstract T train(int classIndex, Map<String,String> optionMap, Instances trainInst, Instances testInst) throws  Exception;
     public String evaluate(Instances testInst) throws Exception{
          if(Classifier.class.isAssignableFrom(clazz)){
               return WekaUtils.evaluateClassifier((Classifier) model,testInst);
          }else if(Clusterer.class.isAssignableFrom(clazz)){
               return WekaUtils.evaluateCluster((Clusterer) model,testInst);
          }else{
               throw  new MissingConfigException("");
          }
     }
     protected void setOptions(Map<String,String> optionMap) throws Exception{
          if(!ObjectUtils.isEmpty(optionMap.get("options"))){
               if(AbstractClassifier.class.isAssignableFrom(clazz)){
                    ((AbstractClassifier)model).setOptions(optionMap.get("options").split(","));
               }else if(AbstractClusterer.class.isAssignableFrom(clazz)){
                    ((AbstractClusterer)model).setOptions(optionMap.get("options").split(","));
               }
          }
     }
}
