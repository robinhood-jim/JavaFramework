package com.robin.dataming.weka.algorithm;


import org.springframework.util.ObjectUtils;
import weka.classifiers.Classifier;
import weka.classifiers.functions.Logistic;
import weka.core.Instances;

import java.util.Map;

public class LogisticRegressionModeler extends AbstractModeler<Classifier> {
    private Logistic logistic;
    public LogisticRegressionModeler(){
        logistic=new Logistic();
    }
    @Override
    public Classifier train(int classIndex, Map<String,String> optionMap, Instances trainInst, Instances testInst) throws Exception {
        if(!ObjectUtils.isEmpty(optionMap.get("maxIts"))) {
            logistic.setMaxIts(Integer.parseInt(optionMap.get("maxIts")));
        }
        if(!ObjectUtils.isEmpty(optionMap.get("ridge"))){
            logistic.setRidge(Double.parseDouble(optionMap.get("ridge")));
        }
        trainInst.setClassIndex(classIndex);
        logistic.buildClassifier(trainInst);
        return logistic;
    }

    @Override
    public String evaluate(Instances testInst) throws Exception {
        return null;
    }
}
