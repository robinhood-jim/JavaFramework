package com.robin.dataming.smile.algorithm;

import smile.clustering.DBSCAN;
import smile.data.DataFrame;


import java.util.Map;

public class DBSCANModeler extends AbstractModeler<DBSCAN> {
    public DBSCAN<double[]> train(DataFrame trainDf, Map<String, Object> optionalMap){
        try{
            int minPts=(Integer)optionalMap.getOrDefault("minPts",5);
            double radius=(Double)optionalMap.getOrDefault("radius",0.01);
            DBSCAN<double[]> dbscan=DBSCAN.fit(trainDf.toArray(),minPts,radius);

            return dbscan;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }
}
