package com.robin.comm.fileaccess.iterator;

import com.robin.core.base.util.Const;
import org.apache.parquet.schema.OriginalType;
import org.apache.parquet.schema.PrimitiveType;


public class ParquetReaderUtil {
    private ParquetReaderUtil(){

    }
    public static String parseColumnType(PrimitiveType type){
        String rettype= Const.META_TYPE_STRING;
        if(type.getPrimitiveTypeName().equals(PrimitiveType.PrimitiveTypeName.INT32)){
            rettype=Const.META_TYPE_INTEGER;
        }else if(type.getPrimitiveTypeName().equals(PrimitiveType.PrimitiveTypeName.INT64)){
            if(type.getOriginalType().equals(OriginalType.DATE) || type.getOriginalType().equals(OriginalType.TIME_MILLIS)){
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
