package com.robin.core.fileaccess.util;

import com.robin.core.base.util.Const;
import com.robin.core.exception.ConfigIncorrectException;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import org.apache.avro.LogicalTypes;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * <p>Project:  frame</p>
 * <p>
 * <p>Description:com.robin.core.fileaccess.util</p>
 * <p>
 * <p>Copyright: Copyright (c) 2018 create at 2018年07月25日</p>
 * <p>
 * <p>Company: zhcx_DEV</p>
 *
 * @author robinjim
 * @version 1.0
 */
public class AvroUtils {
    private static Logger logger= LoggerFactory.getLogger(AvroUtils.class);
    public static Schema getSchemaFromMeta(DataCollectionMeta colmeta){
        Schema schema=null;
        if(colmeta.getResourceCfgMap().containsKey(Const.AVRO_SCHEMA_FILE_PARAM)){
            String schemaPath=colmeta.getResourceCfgMap().get("schemaPath").toString();
            try{
                schema=new Schema.Parser().parse(new FileInputStream(new File(schemaPath)));
            }catch(IOException ex){
                throw new ConfigIncorrectException("avro schema file load exception:"+ex.getMessage());
            }
        }else if(colmeta.getResourceCfgMap().containsKey(Const.AVRO_SCHEMA_CONTENT_PARAM)){
            try{
                schema=new Schema.Parser().parse(colmeta.getResourceCfgMap().get(Const.AVRO_SCHEMA_CONTENT_PARAM).toString());
            }catch(RuntimeException ex){
                throw ex;
            }
        }
        else{
            if(colmeta.getColumnList()!=null){
                SchemaBuilder.FieldAssembler<Schema> fields=SchemaBuilder.record(colmeta.getValueClassName()).namespace(colmeta.getClassNamespace()).fields();

                for(DataCollectionMeta.DataSetColumnMeta meta:colmeta.getColumnList()){
                    if(meta.getColumnType().equals(Const.META_TYPE_BIGINT)) {
                        fields=fields.name(meta.getColumnName()).type().nullable().longType().noDefault();
                    }
                    else if(meta.getColumnType().equals(Const.META_TYPE_INTEGER)){
                       fields=fields.name(meta.getColumnName()).type().nullable().intType().noDefault();
                    }else if(meta.getColumnType().equals(Const.META_TYPE_DOUBLE) || meta.getColumnType().equals(Const.META_TYPE_NUMERIC)){
                        fields=fields.name(meta.getColumnName()).type().nullable().doubleType().noDefault();
                    }else if(meta.getColumnType().equals(Const.META_TYPE_TIMESTAMP)){
                        Schema timestampMilliType = LogicalTypes.timestampMillis().addToSchema(Schema.create(Schema.Type.LONG));
                        fields=fields.name(meta.getColumnName()).type(timestampMilliType).noDefault();
                    }else if(meta.getColumnType().equals(Const.META_TYPE_BOOLEAN)) {
                        fields=fields.name(meta.getColumnName()).type().nullable().booleanType().noDefault();
                    }else if(meta.getColumnType().equals(Const.META_TYPE_STRING)){
                        fields=fields.name(meta.getColumnName()).type().nullable().stringType().noDefault();
                    }
                }
                schema=fields.endRecord();
                logger.info(schema.toString(true));
            }else
                throw new ConfigIncorrectException("missing avro schema config file or Content");
        }
        return schema;
    }
}
