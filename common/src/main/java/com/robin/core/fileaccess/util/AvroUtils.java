package com.robin.core.fileaccess.util;

import com.robin.core.base.datameta.DataBaseColumnMeta;
import com.robin.core.base.exception.ConfigurationIncorrectException;
import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;
import org.apache.avro.LogicalTypes;
import org.apache.avro.Protocol;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;

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
    public static Schema getSchemaForDbMeta(String namespace,String className,List<DataBaseColumnMeta> columnList){
        Schema schema=null;
        String tmpnames=namespace;
        if(tmpnames==null)
            tmpnames="com.zhcx.avro";
        if(columnList!=null && !columnList.isEmpty()){
            SchemaBuilder.FieldAssembler<Schema> fields=SchemaBuilder.record(className).namespace(tmpnames).fields();

            for(DataBaseColumnMeta meta:columnList){
                if(meta.getColumnType().toString().equals(Const.META_TYPE_BIGINT)) {
                    fields=fields.name(meta.getColumnName()).type().nullable().longType().noDefault();
                }
                else if(meta.getColumnType().toString().equals(Const.META_TYPE_INTEGER)){
                    fields=fields.name(meta.getColumnName()).type().nullable().intType().noDefault();
                }else if(meta.getColumnType().toString().equals(Const.META_TYPE_DOUBLE) || meta.getColumnType().toString().equals(Const.META_TYPE_NUMERIC)){
                    fields=fields.name(meta.getColumnName()).type().nullable().doubleType().noDefault();
                }else if(meta.getColumnType().toString().equals(Const.META_TYPE_TIMESTAMP)){
                    Schema timestampMilliType = LogicalTypes.timestampMillis().addToSchema(Schema.create(Schema.Type.LONG));
                    fields=fields.name(meta.getColumnName()).type(timestampMilliType).noDefault();
                }else if(meta.getColumnType().toString().equals(Const.META_TYPE_BOOLEAN)) {
                    fields=fields.name(meta.getColumnName()).type().nullable().booleanType().noDefault();
                }else if(meta.getColumnType().toString().equals(Const.META_TYPE_STRING)){
                     fields=fields.name(meta.getColumnName()).type().nullable().stringType().noDefault();
                }
            }

            fields=fields.nullableBoolean("_UPDATE",false);
            fields=fields.nullableBoolean("_DELETE",false);
            schema=fields.endRecord();
        }
        return schema;
    }
    public static Schema getSchemaFromMeta(DataCollectionMeta colmeta){
        Schema schema=null;
        if(colmeta.getResourceCfgMap().containsKey(Const.AVRO_SCHEMA_FILE_PARAM)){
            String schemaPath=colmeta.getResourceCfgMap().get("schemaPath").toString();
            try{
                schema=new Schema.Parser().parse(new FileInputStream(new File(schemaPath)));
            }catch(IOException ex){
                throw new ConfigurationIncorrectException("avro schema file load exception:"+ex.getMessage());
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

                for(DataSetColumnMeta meta:colmeta.getColumnList()){
                    if(meta.getColumnType().equals(Const.META_TYPE_BIGINT)) {
                        fields=fields.name(meta.getColumnName()).type().nullable().longType().noDefault();
                    }
                    else if(meta.getColumnType().equals(Const.META_TYPE_INTEGER)){
                       fields=fields.name(meta.getColumnName()).type().nullable().intType().noDefault();
                    }else if(meta.getColumnType().equals(Const.META_TYPE_DOUBLE) || meta.getColumnType().equals(Const.META_TYPE_NUMERIC)){
                        fields=fields.name(meta.getColumnName()).type().nullable().doubleType().noDefault();
                    }else if(meta.getColumnType().equals(Const.META_TYPE_TIMESTAMP) || meta.getColumnType().equals(Const.META_TYPE_DATE)){
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
                throw new ConfigurationIncorrectException("missing avro schema config file or Content");
        }
        return schema;
    }
    public static Protocol parseProtocolWithClassPath(String avroFile) throws IOException{
        return Protocol.parse(AvroUtils.class.getClassLoader().getResourceAsStream(avroFile));
    }
    public static Protocol parseProtocolWithFile(String avroFile) throws IOException{
        return Protocol.parse(new FileInputStream(new File(avroFile)));
    }
    public static Protocol parseProtocolWithString(String fileContent) throws IOException{
        return Protocol.parse(new ByteArrayInputStream(fileContent.getBytes()));
    }
    public static byte[] dataToByteArray(Schema schema, GenericRecord datum) throws IOException {
        GenericDatumWriter<GenericRecord> writer = new GenericDatumWriter<GenericRecord>(schema);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            Encoder e = EncoderFactory.get().binaryEncoder(os, null);
            writer.write(datum, e);
            e.flush();
            byte[] byteData = os.toByteArray();
            return byteData;
        } finally {
            os.close();
        }
    }

    public static GenericRecord byteArrayToData(Schema schema, byte[] byteData) {
        GenericDatumReader<GenericRecord> reader = new GenericDatumReader<GenericRecord>(schema);
        ByteArrayInputStream byteArrayInputStream = null;
        try {
            byteArrayInputStream = new ByteArrayInputStream(byteData);
            Decoder decoder = DecoderFactory.get().binaryDecoder(byteArrayInputStream, null);
            return reader.read(null, decoder);
        } catch (IOException e) {
            return null;
        } finally {
            try {
                byteArrayInputStream.close();
            } catch (IOException e) {

            }
        }
    }
}
