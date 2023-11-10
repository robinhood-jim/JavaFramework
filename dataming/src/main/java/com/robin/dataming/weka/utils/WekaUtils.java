package com.robin.dataming.weka.utils;


import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.iterator.AbstractFileIterator;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Weka Utils return Instances by meta define
@Slf4j
public class WekaUtils {
    private WekaUtils() {

    }

    public static Instances getInstancesByResource(DataCollectionMeta collectionMeta, AbstractFileIterator iterator, int classIndex) {
        Assert.notNull(collectionMeta, "columnMeat should not be null!");
        ArrayList<Attribute> attributes = new ArrayList<>();
        Map<String, Attribute> attributeMap = new HashMap<>();

        try {
            if (!CollectionUtils.isEmpty(collectionMeta.getColumnList())) {
                for (int i = 0; i < collectionMeta.getColumnList().size(); i++) {
                    DataSetColumnMeta setColumnMeta = collectionMeta.getColumnList().get(i);
                    Attribute attribute = null;
                    if (!CollectionUtils.isEmpty(setColumnMeta.getNominalValues())) {
                        List<String> normalList = new ArrayList<>();
                        for (int j = 0; j < setColumnMeta.getNominalValues().size(); j++) {
                            normalList.add(String.valueOf(j));
                        }
                        attribute = new Attribute(setColumnMeta.getColumnName(), normalList);
                    } else if (setColumnMeta.getColumnType().equals(Const.META_TYPE_BIGINT) || setColumnMeta.getColumnType().equals(Const.META_TYPE_INTEGER) || setColumnMeta.getColumnType().equals(Const.META_TYPE_DECIMAL) || setColumnMeta.getColumnType().equals(Const.META_TYPE_DOUBLE)) {
                        attribute = new Attribute(setColumnMeta.getColumnName());
                    } else if (setColumnMeta.getColumnType().equals(Const.META_TYPE_DATE) || setColumnMeta.getColumnType().equals(Const.META_TYPE_TIMESTAMP)) {
                        attribute = new Attribute(setColumnMeta.getColumnName(), "yyyy-MM-dd");
                    } else {
                        attribute = new Attribute(setColumnMeta.getColumnName());
                    }
                    attributes.add(attribute);
                    attributeMap.put(setColumnMeta.getColumnName(), attribute);
                }
                Instances instances = new Instances("test", attributes, 1000);
                while (iterator.hasNext()) {
                    Map<String, Object> map = iterator.next();
                    Instance instance = new DenseInstance(collectionMeta.getColumnList().size());
                    for (int i = 0; i < collectionMeta.getColumnList().size(); i++) {
                        DataSetColumnMeta setColumnMeta = collectionMeta.getColumnList().get(i);
                        if (!ObjectUtils.isEmpty(map.get(setColumnMeta.getColumnName()))) {
                            if (!CollectionUtils.isEmpty(setColumnMeta.getNominalValues())) {
                                String value = map.get(setColumnMeta.getColumnName()).toString();
                                int pos = 0;
                                if (setColumnMeta.getNominalValues().contains(value)) {
                                    pos = setColumnMeta.getNominalValues().indexOf(value);
                                }
                                instance.setValue(attributeMap.get(setColumnMeta.getColumnName()), Double.parseDouble(String.valueOf(pos)));
                            } else if (setColumnMeta.getColumnType().equals(Const.META_TYPE_BIGINT) || setColumnMeta.getColumnType().equals(Const.META_TYPE_INTEGER) || setColumnMeta.getColumnType().equals(Const.META_TYPE_DECIMAL) || setColumnMeta.getColumnType().equals(Const.META_TYPE_DOUBLE)) {
                                instance.setValue(attributeMap.get(setColumnMeta.getColumnName()), Double.valueOf(map.get(setColumnMeta.getColumnName()).toString()));
                            } else if (setColumnMeta.getColumnType().equals(Const.META_TYPE_DATE) || setColumnMeta.getColumnType().equals(Const.META_TYPE_TIMESTAMP)) {

                            } else {
                                instance.setValue(attributeMap.get(setColumnMeta.getColumnName()), map.get(setColumnMeta.getColumnName()).toString());
                            }
                        }
                    }
                    instances.add(instance);
                }
                return instances;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("{}", ex.getMessage());
        }
        return null;
    }
}
