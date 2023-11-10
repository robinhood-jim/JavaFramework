package com.robin.dataming.weka.algorithm;

import weka.core.Instances;

import java.util.Map;

public abstract class AbstractModeler<T> {
     public abstract T train(int classIndex, Map<String,String> optionMap, Instances trainInst, Instances testInst) throws  Exception;
     public abstract String evaluate(Instances testInst) throws Exception;
}
