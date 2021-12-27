package com.robin.comm.fileaccess.util;

import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;
import org.apache.orc.TypeDescription;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.InputMismatchException;


public class OrcUtil {
    public static TypeDescription getSchema(DataCollectionMeta colmeta){
        TypeDescription schema=TypeDescription.createStruct();
        if(!CollectionUtils.isEmpty(colmeta.getColumnList())){
            for(DataSetColumnMeta columnMeta:colmeta.getColumnList()){
                if(StringUtils.isEmpty(columnMeta.getColumnType())){
                    continue;
                }
                switch (columnMeta.getColumnType()){
                    case Const.META_TYPE_SHORT:
                        schema.addField(columnMeta.getColumnName(),TypeDescription.createShort());
                        break;
                    case Const.META_TYPE_INTEGER:
                        schema.addField(columnMeta.getColumnName(),TypeDescription.createInt());
                        break;
                    case Const.META_TYPE_NUMERIC:
                    case Const.META_TYPE_DOUBLE:
                    case Const.META_TYPE_FLOAT:
                    case Const.META_TYPE_DECIMAL:
                        schema.addField(columnMeta.getColumnName(),TypeDescription.createDouble());
                        break;
                    case Const.META_TYPE_BIGINT:
                        schema.addField(columnMeta.getColumnName(),TypeDescription.createLong());
                        break;
                    case Const.META_TYPE_DATE:
                        schema.addField(columnMeta.getColumnName(),TypeDescription.createDate());
                        break;
                    case Const.META_TYPE_TIMESTAMP:
                        schema.addField(columnMeta.getColumnName(),TypeDescription.createTimestamp());
                        break;
                    case Const.META_TYPE_STRING:
                        schema.addField(columnMeta.getColumnName(),TypeDescription.createString());
                        break;
                    case Const.META_TYPE_BINARY:
                    case Const.META_TYPE_BLOB:
                        schema.addField(columnMeta.getColumnName(),TypeDescription.createBinary());
                        break;
                    default:
                        throw new InputMismatchException("input type not support!");
                }
            }
        }
        return schema;
    }
}
