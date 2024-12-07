package com.robin.dataming.smile.algorithm;

import com.robin.core.fileaccess.iterator.IResourceIterator;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;
import org.springframework.util.CollectionUtils;
import smile.classification.RandomForest;
import smile.data.DataFrame;
import smile.data.formula.Formula;
import smile.validation.metric.Accuracy;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RandomForestModeler  extends AbstractModeler<RandomForest> {
    public RandomForestModeler(DataCollectionMeta collectionMeta, IResourceIterator iterator) {
        super(collectionMeta, iterator);
    }

    @Override
    public RandomForest train(DataFrame trainDf, Map<String, Object> optionalMap) {
        List<DataSetColumnMeta> fields=collectionMeta.getColumnList().stream().filter(f->CollectionUtils.isEmpty(f.getNominalValues())).collect(Collectors.toList());
        DataSetColumnMeta labelField=collectionMeta.getColumnList().stream().filter(f->!CollectionUtils.isEmpty(f.getNominalValues())).findFirst().orElse(null);

        Formula formula=Formula.of(labelField.getColumnName(),fields.stream().map(DataSetColumnMeta::getColumnName).collect(Collectors.toList()).toArray(new String[]{}));
        model=RandomForest.fit(formula,trainDf);
        return model;
    }

    @Override
    public String validate(DataFrame validateDf) {
        DataSetColumnMeta labelField=collectionMeta.getColumnList().stream().filter(f->!CollectionUtils.isEmpty(f.getNominalValues())).findFirst().orElse(null);
        int[]  predictIndex=model.predict(validateDf);
        int[] trueIndex=validateDf.intVector(labelField.getColumnName()).stream().toArray();
        System.out.println(String.format("Accuracy：%f", Accuracy.of(trueIndex,predictIndex)));
        return model.trees()[0].toString();
    }
}
