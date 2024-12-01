package com.robin.dataming.weka.algorithm;

import org.springframework.util.ObjectUtils;
import weka.clusterers.EM;
import weka.core.Instances;

import java.util.Map;

public class EMClusterModeler extends AbstractModeler<EM>{

    public EMClusterModeler(){
        model=new EM();
    }

    @Override
    public EM train(int classIndex, Map<String, String> optionMap, Instances trainInst, Instances testInst) throws Exception {
        trainInst.setClassIndex(classIndex);
        model.buildClusterer(trainInst);
        if(!ObjectUtils.isEmpty(optionMap.get("numberOfCluster"))){
            model.setNumClusters(Integer.parseInt(optionMap.get("numberOfCluster")));
        }
        if(!ObjectUtils.isEmpty(optionMap.get("kMeansRuns"))){
            model.setNumKMeansRuns(Integer.parseInt(optionMap.get("kMeansRuns")));
        }
        if(!ObjectUtils.isEmpty(optionMap.get("maxIter"))){
            model.setMaxIterations(Integer.parseInt(optionMap.get("maxIter")));
        }
        return model;
    }
}
