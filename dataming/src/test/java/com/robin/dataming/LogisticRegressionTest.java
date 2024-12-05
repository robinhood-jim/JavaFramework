package com.robin.dataming;


import com.google.common.collect.Lists;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.iterator.IResourceIterator;
import com.robin.core.fileaccess.iterator.TextFileIteratorFactory;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;
import com.robin.dataming.weka.algorithm.LogisticRegressionModeler;
import com.robin.dataming.weka.utils.WekaUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import weka.classifiers.functions.Logistic;
import weka.core.Instances;

import java.util.HashMap;


public class LogisticRegressionTest {
    @Test
    public void testIris() throws Exception{
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
        DataSetColumnMeta columnMeta=meta.createColumnMeta("class",Const.META_TYPE_STRING,null);
        columnMeta.setNominalValues(Lists.newArrayList(new String[]{"setosa", "versicolor","virginica"}));
        meta.addColumnMeta(columnMeta);

        IResourceIterator iterator= TextFileIteratorFactory.getProcessIteratorByType(meta);
        Instances instances= WekaUtils.getInstancesByResource(meta,iterator,4);
        Pair<Instances,Instances> datas=WekaUtils.splitTrainAndValidates(instances,80.0);
        LogisticRegressionModeler modeler=new LogisticRegressionModeler();
        Logistic classifier= modeler.train(4,new HashMap<>(),datas.getLeft());
        System.out.println(classifier.toString());
        System.out.println(modeler.evaluate(datas.getRight()));
    }
}
