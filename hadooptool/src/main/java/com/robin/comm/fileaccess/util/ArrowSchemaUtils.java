package com.robin.comm.fileaccess.util;

import com.robin.core.base.exception.ConfigurationIncorrectException;
import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;
import org.apache.arrow.vector.types.TimeUnit;
import org.apache.arrow.vector.types.pojo.ArrowType;
import org.apache.arrow.vector.types.pojo.Field;
import org.apache.arrow.vector.types.pojo.FieldType;
import org.apache.arrow.vector.types.pojo.Schema;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class ArrowSchemaUtils {
    private ArrowSchemaUtils(){

    }

    public static Schema getSchema(DataCollectionMeta colmeta){
        List<Field> fieldList=new ArrayList<>();
        if(!CollectionUtils.isEmpty(colmeta.getColumnList())){
            for(DataSetColumnMeta meta:colmeta.getColumnList()){

                switch (meta.getColumnType()){
                    case Const.META_TYPE_INTEGER:
                        fieldList.add(new Field(meta.getColumnName(), FieldType.nullable(new ArrowType.Int(32,true)),null));
                        break;
                    case Const.META_TYPE_BIGINT:
                        fieldList.add(new Field(meta.getColumnName(), FieldType.nullable(new ArrowType.Int(64,true)),null));
                        break;
                    case Const.META_TYPE_FLOAT:
                    case Const.META_TYPE_DOUBLE:
                        fieldList.add(new Field(meta.getColumnName(), FieldType.nullable(new ArrowType.Decimal(30,1)),null));
                        break;
                    case Const.META_TYPE_TIMESTAMP:
                        fieldList.add(new Field(meta.getColumnName(), FieldType.nullable(new ArrowType.Timestamp(TimeUnit.MILLISECOND,"utc")),null));
                        break;
                    case Const.META_TYPE_STRING:
                        fieldList.add(new Field(meta.getColumnName(), FieldType.nullable(new ArrowType.Utf8()),null));
                        break;
                    default:
                        fieldList.add(new Field(meta.getColumnName(), FieldType.nullable(new ArrowType.Utf8()),null));
                }
            }
            return new Schema(fieldList);
        }else{
            throw new ConfigurationIncorrectException(" meta defined missing");
        }
    }
}
