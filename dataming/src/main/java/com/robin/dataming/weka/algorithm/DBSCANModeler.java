package com.robin.dataming.weka.algorithm;

import org.springframework.util.ObjectUtils;
import weka.clusterers.DBSCAN;
import weka.core.EuclideanDistance;
import weka.core.Instances;
import weka.core.ManhattanDistance;

import java.util.Map;

public class DBSCANModeler extends AbstractModeler<DBSCAN>{
    @Override
    public DBSCAN train(int classIndex, Map<String, String> optionMap, Instances trainInst, Instances testInst) throws Exception {

        if(!ObjectUtils.isEmpty(optionMap.get("distanceFunc")) && "Manhattan".equalsIgnoreCase(optionMap.get("distanceFunc"))) {
            model.setDistanceFunction(new ManhattanDistance());
        }else{
            model.setDistanceFunction(new EuclideanDistance());
        }
        if(!ObjectUtils.isEmpty(optionMap.get("epsilon"))){
            model.setEpsilon(Double.parseDouble(optionMap.get("epsilon")));
        }
        if(!ObjectUtils.isEmpty(optionMap.get("minPoints"))){
            model.setMinPoints(Integer.parseInt(optionMap.get("minPoints")));
        }
        model.buildClusterer(trainInst);
        return model;
    }
}
