package com.robin.comm.fileaccess.iterator;

import com.robin.core.base.util.Const;
import org.apache.parquet.schema.LogicalTypeAnnotation;
import org.apache.parquet.schema.PrimitiveType;


public class ParquetReaderUtil {
    private ParquetReaderUtil(){

    }
    public static String parseColumnType(PrimitiveType type){
        String rettype= Const.META_TYPE_STRING;
        if(type.getPrimitiveTypeName().equals(PrimitiveType.PrimitiveTypeName.INT32)){
            rettype=Const.META_TYPE_INTEGER;
        }else if(type.getPrimitiveTypeName().equals(PrimitiveType.PrimitiveTypeName.INT64)){
            if(LogicalTypeAnnotation.dateType().equals(type.getLogicalTypeAnnotation()) || LogicalTypeAnnotation.timestampType(true, LogicalTypeAnnotation.TimeUnit.MILLIS).equals(type.getLogicalTypeAnnotation())){
                rettype=Const.META_TYPE_TIMESTAMP;
            }else {
                rettype = Const.META_TYPE_BIGINT;
            }
        }else if(type.getPrimitiveTypeName().equals(PrimitiveType.PrimitiveTypeName.DOUBLE)){
            rettype=Const.META_TYPE_DOUBLE;
        }else if(type.getPrimitiveTypeName().equals(PrimitiveType.PrimitiveTypeName.FLOAT)){
            rettype=Const.META_TYPE_DOUBLE;
        }else if(type.getPrimitiveTypeName().equals(PrimitiveType.PrimitiveTypeName.INT96)){
            rettype=Const.META_TYPE_TIMESTAMP;
        }
        return rettype;
    }
}
