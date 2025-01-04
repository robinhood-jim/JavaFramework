package com.robin.comm.sql;

import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.calcite.sql.type.SqlTypeName;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

public class DataMetaCalciteTable extends AbstractTable {
    private DataCollectionMeta colmeta;
    public DataMetaCalciteTable(DataCollectionMeta colmeta){
        this.colmeta=colmeta;
    }

    @Override
    public RelDataType getRowType(RelDataTypeFactory typeFactory) {
        Assert.isTrue(!ObjectUtils.isEmpty(colmeta) && !CollectionUtils.isEmpty(colmeta.getColumnList()),"meta define is missing!");
        RelDataTypeFactory.Builder builder = typeFactory.builder();
        for(DataSetColumnMeta columnMeta:colmeta.getColumnList()){
            if(Const.META_TYPE_INTEGER.equals(columnMeta.getColumnType())){
                builder.add(columnMeta.getColumnName(), SqlTypeName.INTEGER);
            }else if(Const.META_TYPE_BIGINT.equals(columnMeta.getColumnType())){
                builder.add(columnMeta.getColumnName(), SqlTypeName.BIGINT);
            }
            else if(Const.META_TYPE_DOUBLE.equals(columnMeta.getColumnType())){
                builder.add(columnMeta.getColumnName(), SqlTypeName.DOUBLE);
            }
            else if(Const.META_TYPE_DECIMAL.equals(columnMeta.getColumnType())){
                builder.add(columnMeta.getColumnName(), SqlTypeName.DECIMAL);
            }
            else if(Const.META_TYPE_TIMESTAMP.equals(columnMeta.getColumnType())){
                builder.add(columnMeta.getColumnName(), SqlTypeName.TIMESTAMP);
            }
            else if(Const.META_TYPE_DATE.equals(columnMeta.getColumnType())){
                builder.add(columnMeta.getColumnName(), SqlTypeName.DATE);
            }
            else if(Const.META_TYPE_CLOB.equals(columnMeta.getColumnType()) || Const.META_TYPE_BLOB.equals(columnMeta.getColumnType())){
                builder.add(columnMeta.getColumnName(), SqlTypeName.BINARY);
            }else if(Const.META_TYPE_STRING.equals(columnMeta.getColumnType())){
                builder.add(columnMeta.getColumnName(), SqlTypeName.VARCHAR);
            }
        }
        return builder.build();
    }
}
