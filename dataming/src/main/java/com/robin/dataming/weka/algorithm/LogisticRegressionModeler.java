package com.robin.dataming.weka.algorithm;


import org.springframework.util.ObjectUtils;
import weka.classifiers.Classifier;
import weka.classifiers.functions.Logistic;
import weka.core.Instances;

import java.util.Map;

public class LogisticRegressionModeler extends AbstractModeler<Logistic> {

    @Override
    public Logistic train(int classIndex, Map<String,String> optionMap, Instances trainInst) throws Exception {
        if(!ObjectUtils.isEmpty(optionMap.get("maxIts"))) {
            model.setMaxIts(Integer.parseInt(optionMap.get("maxIts")));
        }
        if(!ObjectUtils.isEmpty(optionMap.get("ridge"))){
            model.setRidge(Double.parseDouble(optionMap.get("ridge")));
        }
        trainInst.setClassIndex(classIndex);
        setOptions(optionMap);
        model.buildClassifier(trainInst);
        return model;
    }

}
