package com.robin.dataming.weka.algorithm;

import com.robin.core.base.exception.MissingConfigException;
import weka.classifiers.Classifier;
import weka.classifiers.trees.SimpleCart;
import weka.core.Instances;

import java.util.Map;


public class SimpleCartModeler extends AbstractModeler<Classifier>{
    private SimpleCart cart;
    public SimpleCartModeler(){
        cart=new SimpleCart();
    }

    @Override
    public Classifier train(int classIndex, Map<String, String> optionMap, Instances trainInst, Instances testInst) throws Exception {
        if(!trainInst.attribute(classIndex).isNominal()){
            throw new MissingConfigException("class index not nomianl");
        }
        long minNum=trainInst.numInstances()/1000;
        cart.setOptions(new String[]{"-A"});
        if(minNum>5L){
            cart.setMinNumObj(Double.valueOf(String.valueOf(minNum)));
        }
        cart.buildClassifier(trainInst);
        return cart;
    }

    @Override
    public String evaluate(Instances testInst) throws Exception {
        return null;
    }
}
