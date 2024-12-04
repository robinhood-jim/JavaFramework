package com.robin.dataming.smile.algorithm;

import smile.data.DataFrame;

import java.util.Map;

public abstract class AbstractModeler<T> {
    public  abstract T train(DataFrame trainDf, Map<String, Object> optionalMap);
}
