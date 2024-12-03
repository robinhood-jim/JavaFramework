package com.robin.dataming.spark.algorithm;


import com.robin.core.fileaccess.meta.DataCollectionMeta;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.tree.DecisionTree;
import org.apache.spark.mllib.tree.model.DecisionTreeModel;

import java.util.HashMap;
import java.util.Map;

public class DecisionTreeModeler extends AbstractSparkModeler {


    public DecisionTreeModeler(Map<String, String> configMap) {
        super(configMap);
    }
    public DecisionTreeModel train(DataCollectionMeta meta, int classNum, int numberClasses){
        JavaRDD<LabeledPoint> rdd=getInstances(meta,classNum);
        DecisionTreeModel model= DecisionTree.trainClassifier(rdd, numberClasses, new HashMap<>(), "gini", 4, 100);
        return model;
    }
}
