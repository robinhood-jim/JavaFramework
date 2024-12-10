package com.robin.dataming.weka.algorithm;


import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import weka.clusterers.SimpleKMeans;
import weka.core.EuclideanDistance;
import weka.core.Instances;
import weka.core.ManhattanDistance;

import java.util.Map;

public class KMeansModeler extends AbstractModeler<SimpleKMeans> {

    @Override
    public SimpleKMeans train(int classIndex, Map<String, String> optionMap, Instances trainInst) throws Exception {
        Assert.isTrue(!ObjectUtils.isEmpty(optionMap.get("numberOfCluster")) && !ObjectUtils.isEmpty(optionMap.get("maxIter")) ,"");
        if(!ObjectUtils.isEmpty(optionMap.get("numberOfCluster"))){
            model.setNumClusters(Integer.parseInt(optionMap.get("numberOfCluster")));
        }
        if(!ObjectUtils.isEmpty(optionMap.get("distanceFunc")) && "Manhattan".equalsIgnoreCase(optionMap.get("distanceFunc"))) {
            model.setDistanceFunction(new ManhattanDistance());
        }else{
            model.setDistanceFunction(new EuclideanDistance());
        }
        if(!ObjectUtils.isEmpty(optionMap.get("maxIter"))){
            model.setMaxIterations(Integer.parseInt(optionMap.get("maxIter")));
        }
        setOptions(optionMap);
        model.buildClusterer(trainInst);
        return model;
    }

}
