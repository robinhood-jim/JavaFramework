package com.robin.dataming.smile.utils;

import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.iterator.IResourceIterator;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;
import org.springframework.util.CollectionUtils;
import smile.data.DataFrame;
import smile.data.Tuple;
import smile.data.type.DataType;
import smile.data.type.DataTypes;
import smile.data.type.StructField;
import smile.data.type.StructType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SmileUtils {
    public static DataFrame construct(DataCollectionMeta colMeta, IResourceIterator iterator){
        List<StructField> fields=new ArrayList<>();
        for(DataSetColumnMeta columnMeta:colMeta.getColumnList()){
            fields.add(new StructField(columnMeta.getColumnName(),wrapDataType(columnMeta)));
        }
        StructType structType=new StructType(fields);
        List<Tuple> datas=new ArrayList<>();

        while(iterator.hasNext()){
            Map<String, Object> map = iterator.next();
            List<Object> objects=wrapValue(colMeta.getColumnList(),map);
            datas.add(Tuple.of(objects.toArray(),structType));
        }
        StructType boxType=structType.boxed(datas);
        return DataFrame.of(datas,boxType);
    }
    private static DataType wrapDataType(DataSetColumnMeta columnMeta){
        DataType type=null;
        switch (columnMeta.getColumnType()){
            case Const.META_TYPE_BIGINT:
                type= DataTypes.LongType;
                break;
            case Const.META_TYPE_INTEGER:
                type=DataTypes.IntegerType;
                break;
            case Const.META_TYPE_DOUBLE:
                type=DataTypes.DoubleType;
                break;
            case Const.META_TYPE_DECIMAL:
                type=DataTypes.DecimalType;
                break;
            case  Const.META_TYPE_TIMESTAMP:
                type=DataTypes.TimeType;
                break;
            case Const.META_TYPE_SHORT:
                type=DataTypes.ShortType;
                break;
            case Const.META_TYPE_STRING:
                if(CollectionUtils.isEmpty(columnMeta.getNominalValues())) {
                    type = DataTypes.StringType;
                }else{
                    type=DataTypes.IntegerType;
                }
                break;
            default:
                type=DataTypes.StringType;
        }
        return type;
    }
    public static List<Object>  wrapValue(List<DataSetColumnMeta> columnMetas, Map<String,Object> map){
        List<Object> objects=new ArrayList<>(columnMetas.size());
        for(DataSetColumnMeta columnMeta:columnMetas){
            Object value=map.getOrDefault(columnMeta.getColumnName(),null);
            if(!CollectionUtils.isEmpty(columnMeta.getNominalValues())){
                Integer pos=columnMeta.getNominalValues().indexOf(value.toString());
                if(pos>=-1){
                    value=pos;
                }
            }
            objects.add(value);
        }
        return objects;
    }
}
