package com.robin.dataming.weka.algorithm;


import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import weka.clusterers.Clusterer;
import weka.clusterers.SimpleKMeans;
import weka.core.EuclideanDistance;
import weka.core.Instances;
import weka.core.ManhattanDistance;

import java.util.Map;

public class KMeansModeler extends AbstractModeler<Clusterer> {
    private SimpleKMeans kMeans;
    public KMeansModeler(){
        kMeans=new SimpleKMeans();
    }
    @Override
    public Clusterer train(int classIndex, Map<String, String> optionMap, Instances trainInst, Instances testInst) throws Exception {
        Assert.isTrue(!ObjectUtils.isEmpty(optionMap.get("numberOfCluster")) && !ObjectUtils.isEmpty(optionMap.get("maxIter")) ,"");
        if(!ObjectUtils.isEmpty(optionMap.get("numberOfCluster"))){
            kMeans.setNumClusters(Integer.parseInt(optionMap.get("numberOfCluster")));
        }
        if(!ObjectUtils.isEmpty(optionMap.get("distanceFunc")) && "Manhattan".equalsIgnoreCase(optionMap.get("distanceFunc"))) {
            kMeans.setDistanceFunction(new ManhattanDistance());
        }else{
            kMeans.setDistanceFunction(new EuclideanDistance());
        }
        if(!ObjectUtils.isEmpty(optionMap.get("maxIter"))){
            kMeans.setMaxIterations(Integer.parseInt(optionMap.get("maxIter")));
        }
        kMeans.buildClusterer(trainInst);
        return kMeans;
    }

    @Override
    public String evaluate(Instances testInst) throws Exception {
        return null;
    }
}
