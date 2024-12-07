package com.robin.dataming.weka;


import com.robin.core.base.util.Const;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.iterator.IResourceIterator;
import com.robin.core.fileaccess.iterator.TextFileIteratorFactory;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
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
        meta.setFileFormat(Const.FILEFORMATSTR.ARFF.getValue());
        meta.setPath("file:///f:/iris.arff");


        try(IResourceIterator iterator= TextFileIteratorFactory.getProcessIteratorByType(meta)) {
            Instances instances = WekaUtils.getInstancesByResource(meta, iterator, 4);
            Pair<Instances, Instances> datas = WekaUtils.splitTrainAndValidates(instances, 80.0);
            LogisticRegressionModeler modeler = new LogisticRegressionModeler();
            Logistic classifier = modeler.train(4, new HashMap<>(), datas.getLeft());
            System.out.println(classifier.toString());
            System.out.println(modeler.evaluate(datas.getRight()));
        }catch (Exception ex){

        }
    }
}
