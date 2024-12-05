package com.robin.dataming.smile;

import com.google.common.collect.Lists;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.iterator.IResourceIterator;
import com.robin.core.fileaccess.iterator.TextFileIteratorFactory;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;
import com.robin.dataming.smile.algorithm.DBSCANModeler;
import com.robin.dataming.smile.utils.SmileUtils;
import org.apache.commons.lang3.tuple.Pair;
import smile.clustering.DBSCAN;
import smile.data.DataFrame;

import java.util.HashMap;
import java.util.Map;

public class DBSCANTest {
    public static void main(String[] args){

        DataCollectionMeta meta=new DataCollectionMeta();
        meta.setResType(ResourceConst.ResourceType.TYPE_LOCALFILE.getValue());
        meta.setSourceType(ResourceConst.IngestType.TYPE_LOCAL.getValue());
        meta.setFileFormat(Const.FILESUFFIX_CSV);
        meta.setPath("file:///e:/iris.csv");
        //"erwidth","banlength","banwidth","class"
        meta.addColumnMeta("erlength", Const.META_TYPE_DOUBLE,null);
        meta.addColumnMeta("erwidth", Const.META_TYPE_DOUBLE,null);
        meta.addColumnMeta("banlength", Const.META_TYPE_DOUBLE,null);
        meta.addColumnMeta("banwidth", Const.META_TYPE_DOUBLE,null);

        try{
            IResourceIterator iterator= TextFileIteratorFactory.getProcessIteratorByType(meta);
            DBSCANModeler modeler=new DBSCANModeler(meta,iterator);
            Pair<DataFrame,DataFrame> dfPair=SmileUtils.splitTrainAndValidate(meta,iterator,80);
            Map<String,Object> optionalMap=new HashMap<>();
            optionalMap.put("radius",0.6);
            DBSCAN<double[]> dbscan= modeler.train(dfPair.getKey(),optionalMap);
            System.out.println(dbscan.k);
            System.out.println(dbscan.toString());
            System.out.println(modeler.validate(dfPair.getValue()));
        }catch (Exception ex){
            ex.printStackTrace();
        }

    }
}
