package com.robin.comm.fileaccess.iterator;

import com.robin.core.base.util.Const;
import org.apache.parquet.schema.PrimitiveType;

/**
 * <p>Project:  frame</p>
 * <p>
 * <p>Description:com.robin.comm.fileaccess.iterator</p>
 * <p>
 * <p>Copyright: Copyright (c) 2019 create at 2019年08月09日</p>
 * <p>
 * <p>Company: zhcx_DEV</p>
 *
 * @author robinjim
 * @version 1.0
 */
public class ParquetReaderUtil {
    public static String parseColumnType(PrimitiveType type){
        String rettype= Const.META_TYPE_STRING;
        if(type.getPrimitiveTypeName().equals(PrimitiveType.PrimitiveTypeName.INT32)){
            rettype=Const.META_TYPE_INTEGER;
        }else if(type.getPrimitiveTypeName().equals(PrimitiveType.PrimitiveTypeName.INT64)){
            rettype=Const.META_TYPE_BIGINT;
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
