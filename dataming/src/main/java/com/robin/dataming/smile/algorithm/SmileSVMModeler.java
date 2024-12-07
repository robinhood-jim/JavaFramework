package com.robin.dataming.smile.algorithm;

import com.robin.core.fileaccess.iterator.IResourceIterator;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;
import org.springframework.util.CollectionUtils;
import smile.classification.Classifier;
import smile.classification.SVM;
import smile.data.DataFrame;
import smile.validation.metric.Accuracy;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SmileSVMModeler extends AbstractModeler<Classifier>{


    public SmileSVMModeler(DataCollectionMeta collectionMeta, IResourceIterator iterator) {
        super(collectionMeta, iterator);
    }

    @Override
    public Classifier train(DataFrame trainDf, Map<String, Object> optionalMap) {
        List<DataSetColumnMeta> fields=collectionMeta.getColumnList().stream().filter(f-> CollectionUtils.isEmpty(f.getNominalValues())).collect(Collectors.toList());
        DataSetColumnMeta labelField=collectionMeta.getColumnList().stream().filter(f->!CollectionUtils.isEmpty(f.getNominalValues())).findFirst().orElse(null);
        double[][] dsArr= trainDf.select(fields.stream().map(DataSetColumnMeta::getColumnName).collect(Collectors.toList()).toArray(new String[]{})).toArray();
        model=SVM.fit(dsArr,trainDf.intVector(labelField.getColumnName()).array(),1,1);
        return model;
    }

    @Override
    public String validate(DataFrame validateDf) {
        List<DataSetColumnMeta> fields=collectionMeta.getColumnList().stream().filter(f-> CollectionUtils.isEmpty(f.getNominalValues())).collect(Collectors.toList());
        DataSetColumnMeta labelField=collectionMeta.getColumnList().stream().filter(f->!CollectionUtils.isEmpty(f.getNominalValues())).findFirst().orElse(null);
        int[] trueIndex=validateDf.intVector(labelField.getColumnName()).array();
        int[] predictIndex=model.predict(validateDf.select(fields.stream().map(DataSetColumnMeta::getColumnName).collect(Collectors.toList()).toArray(new String[]{})).toArray());
        System.out.println(String.format("Accuracy：%f", Accuracy.of(trueIndex,predictIndex)));
        return model.toString();
    }
}
