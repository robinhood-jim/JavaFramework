package com.robin.dataming;


import com.robin.core.base.util.Const;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;
import com.robin.dataming.spark.algorithm.DecisionTreeModeler;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.mllib.tree.model.DecisionTreeModel;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class DecisionTreeTest {
    @Test
    public void testSpark(){
        DataCollectionMeta meta=new DataCollectionMeta();
        meta.setSourceType(ResourceConst.IngestType.TYPE_LOCAL.getValue());
        meta.setFileFormat(Const.FILESUFFIX_CSV);
        //change to your machine local path
        meta.setPath("file:///e:/iris.csv");
        meta.addColumnMeta("erlength", Const.META_TYPE_DOUBLE,null);
        meta.addColumnMeta("erwidth", Const.META_TYPE_DOUBLE,null);
        meta.addColumnMeta("banlength", Const.META_TYPE_DOUBLE,null);
        meta.addColumnMeta("banwidth", Const.META_TYPE_DOUBLE,null);
        //nominal target column
        DataSetColumnMeta columnMeta=meta.createColumnMeta("class",Const.META_TYPE_STRING,null);
        columnMeta.setNominalValues(Arrays.asList(new String[]{"Iris-setosa", "Iris-versicolor","Iris-virginica"}));
        meta.addColumnMeta(columnMeta);
        Map<String,String> configMap=new HashMap<>();
        configMap.put("masterUrl","local");
        configMap.put("appName","test1");
        DecisionTreeModeler modeler=new DecisionTreeModeler(configMap);
        DecisionTreeModel model=modeler.train(meta,4,10);
        log.info("{}",model.toDebugString());
    }
}
