package com.robin.dataming.weka.algorithm;

import org.springframework.util.ObjectUtils;
import weka.classifiers.lazy.IBk;
import weka.core.Instances;
import weka.core.neighboursearch.KDTree;

import java.util.Map;

public class KNNModeler extends AbstractModeler<IBk> {

    @Override
    public IBk train(int classIndex, Map<String, String> optionMap, Instances trainInst, Instances testInst) throws Exception {
        trainInst.setClassIndex(classIndex);

        if(!ObjectUtils.isEmpty(optionMap.get("numberOfCluster"))){
            model.setKNN(Integer.parseInt(optionMap.get("numberOfCluster")));
        }
        if(!ObjectUtils.isEmpty(optionMap.get("algorithm"))){
            if("KdTree".equalsIgnoreCase(optionMap.get("algorithm"))){
                model.setNearestNeighbourSearchAlgorithm(new KDTree());
            }
        }
        setOptions(optionMap);
        model.buildClassifier(trainInst);
        return model;
    }
}
