package com.robin.dataming.weka.algorithm;


import org.springframework.util.Assert;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.functions.LinearRegression;
import weka.core.Instances;



import java.util.Map;


public class LinearRegressionModeler extends AbstractModeler<Classifier> {
    private LinearRegression linearRegression;

    public LinearRegressionModeler(){
        linearRegression=new LinearRegression();
    }
    @Override
    public Classifier train(int classIndex, Map<String,String> optionMap, Instances trainInst, Instances testInst) throws  Exception{
        Assert.isTrue(classIndex>0 && classIndex<trainInst.numAttributes(),"");
        trainInst.setClassIndex(classIndex);
        linearRegression.buildClassifier(trainInst);
        Evaluation evaluation=new Evaluation(testInst);
        for(int i=0;i<testInst.numInstances();i++) {
            evaluation.evaluateModelOnceAndRecordPrediction(linearRegression, testInst.instance(i));
        }
        System.out.println(evaluation.toSummaryString());
        return linearRegression;
    }

    @Override
    public String evaluate(Instances testInst) throws Exception{
        Evaluation evaluation=new Evaluation(testInst);
        for(int i=0;i<testInst.numInstances();i++) {
            evaluation.evaluateModelOnceAndRecordPrediction(linearRegression, testInst.instance(i));
        }
        return evaluation.toSummaryString();
    }
}
