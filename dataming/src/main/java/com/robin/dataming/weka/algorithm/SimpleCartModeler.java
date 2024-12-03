package com.robin.dataming.weka.algorithm;


import com.robin.core.base.exception.MissingConfigException;
import weka.classifiers.trees.J48;
import weka.core.Instances;

import java.util.Map;

public class SimpleCartModeler extends AbstractModeler<J48> {


    @Override
    public J48 train(int classIndex, Map<String, String> optionMap, Instances trainInst) throws Exception {
        if(!trainInst.attribute(classIndex).isNominal()){
            throw new MissingConfigException("class index not nomianl");
        }
        long minNum=trainInst.numInstances()/1000;

        model.setOptions(new String[]{"-A"});
        if(minNum>5L){
            model.setMinNumObj(Integer.valueOf(String.valueOf(minNum)));
        }
        model.buildClassifier(trainInst);
        return model;
    }

}
