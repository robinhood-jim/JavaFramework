package com.robin.dataming.weka.algorithm;


import org.springframework.util.Assert;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.functions.LinearRegression;
import weka.core.Instances;



import java.util.Map;


public class LinearRegressionModeler extends AbstractModeler<LinearRegression> {


    public LinearRegressionModeler(){
        model=new LinearRegression();
    }
    @Override
    public LinearRegression train(int classIndex, Map<String,String> optionMap, Instances trainInst, Instances testInst) throws  Exception{
        Assert.isTrue(classIndex>0 && classIndex<trainInst.numAttributes(),"");
        trainInst.setClassIndex(classIndex);
        setOptions(optionMap);
        model.buildClassifier(trainInst);
        return model;
    }


}
