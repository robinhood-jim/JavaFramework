package com.robin.dataming.smile.algorithm;

import com.google.gson.Gson;
import com.robin.comm.util.json.GsonUtil;
import com.robin.core.fileaccess.iterator.IResourceIterator;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import smile.clustering.DBSCAN;
import smile.data.DataFrame;

import java.util.HashMap;
import java.util.Map;

public class DBSCANModeler extends AbstractModeler<DBSCAN<double[]>> {
    private Gson gson= GsonUtil.getGson();
    public DBSCANModeler(DataCollectionMeta collectionMeta, IResourceIterator iterator) {
        super(collectionMeta, iterator);
    }

    public DBSCAN<double[]> train(DataFrame trainDf, Map<String, Object> optionalMap){
        try{
            int minPts=(Integer)optionalMap.getOrDefault("minPts",4);
            double radius=(Double)optionalMap.getOrDefault("radius",0.1);
            model=DBSCAN.fit(trainDf.toArray(),minPts,radius);
            return model;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public String validate(DataFrame validateDf) {
        Map<Integer,Integer> rangeMap=new HashMap<>();
        validateDf.stream().forEach(f->{
            int range=model.predict(f.toArray());
            rangeMap.computeIfAbsent(range,key->1);
            rangeMap.computeIfPresent(range,(key,v)->v=v+1);
        });
        return gson.toJson(rangeMap);
    }
}
