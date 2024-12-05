package com.robin.dataming.smile.algorithm;

import com.robin.core.fileaccess.iterator.IResourceIterator;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import smile.data.DataFrame;

import java.util.Map;

public abstract class AbstractModeler<T> {
    protected T model;
    protected DataCollectionMeta collectionMeta;
    protected IResourceIterator iterator;

    public AbstractModeler(DataCollectionMeta collectionMeta,IResourceIterator iterator){
        this.collectionMeta=collectionMeta;
        this.iterator=iterator;
    }

    public abstract T train(DataFrame trainDf, Map<String, Object> optionalMap);
    public abstract  String validate(DataFrame validateDf);

    public T getModel(){
        return model;
    }
}
