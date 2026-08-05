package com.robin.comm.fileaccess.iterator;

import com.robin.core.base.util.Const;
import org.apache.parquet.schema.LogicalTypeAnnotation;
import org.apache.parquet.schema.PrimitiveType;


public class ParquetReaderUtil {
    private ParquetReaderUtil(){

    }
    public static Const.CLASSTYPE parseColumnType(PrimitiveType type){
        Const.CLASSTYPE rettype= Const.CLASSTYPE.STRING;
        if(type.getPrimitiveTypeName().equals(PrimitiveType.PrimitiveTypeName.INT32)){
            rettype= Const.CLASSTYPE.INTEGER;
        }else if(type.getPrimitiveTypeName().equals(PrimitiveType.PrimitiveTypeName.INT64)){
            if(LogicalTypeAnnotation.dateType().equals(type.getLogicalTypeAnnotation()) || LogicalTypeAnnotation.timestampType(true, LogicalTypeAnnotation.TimeUnit.MILLIS).equals(type.getLogicalTypeAnnotation())){
                rettype= Const.CLASSTYPE.TIMESTAMP;
            }else {
                rettype = Const.CLASSTYPE.LONG;
            }
        }else if(type.getPrimitiveTypeName().equals(PrimitiveType.PrimitiveTypeName.DOUBLE)){
            rettype= Const.CLASSTYPE.DOUBLE;
        }else if(type.getPrimitiveTypeName().equals(PrimitiveType.PrimitiveTypeName.FLOAT)){
            rettype= Const.CLASSTYPE.DOUBLE;
        }else if(type.getPrimitiveTypeName().equals(PrimitiveType.PrimitiveTypeName.INT96)){
            rettype= Const.CLASSTYPE.TIMESTAMP;
        }
        return rettype;
    }
}
