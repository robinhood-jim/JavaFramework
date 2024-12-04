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
import smile.clustering.DBSCAN;
import smile.data.DataFrame;

import java.util.HashMap;

public class DBSCANTest {
    public static void main(String[] args){
        DBSCANModeler modeler=new DBSCANModeler();
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

        try{
            IResourceIterator iterator= TextFileIteratorFactory.getProcessIteratorByType(meta);
            DataFrame dataFrame= SmileUtils.construct(meta,iterator);
            DBSCAN<double[]> dbscan= modeler.train(dataFrame,new HashMap<>());
            System.out.println(dbscan.y);
            System.out.println(dbscan.toString());
        }catch (Exception ex){
            ex.printStackTrace();
        }

    }
}
