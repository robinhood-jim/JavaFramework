package com.robin.core.fileaccess.util;

import com.google.common.collect.Maps;
import com.robin.core.base.datameta.DataBaseColumnMeta;
import com.robin.core.base.exception.ConfigurationIncorrectException;
import com.robin.core.base.reflect.ReflectUtils;
import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;
import com.twitter.bijection.Injection;
import com.twitter.bijection.avro.GenericAvroCodecs;
import org.apache.avro.LogicalTypes;
import org.apache.avro.Protocol;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

import java.io.*;
import java.lang.reflect.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;

/**
 * <p>Project:  frame</p>
 * <p>
 * <p>Description:Avro schema Utils,Serialize and Unserialize</p>
 * <p>
 * <p>Copyright: Copyright (c) 2018 create at 2018年07月25日</p>
 * <p>
 * <p>Company: zhcx_DEV</p>
 *
 * @author robinjim
 * @version 1.0
 */
public class AvroUtils {
    private static Logger logger = LoggerFactory.getLogger(AvroUtils.class);

    public static Schema getSchemaForDbMeta(String namespace, String className, List<DataBaseColumnMeta> columnList) {
        Schema schema = null;
        String tmpnames = namespace;
        if (tmpnames == null) {
            tmpnames = "com.robin.avro";
        }
        if (columnList != null && !columnList.isEmpty()) {
            SchemaBuilder.FieldAssembler<Schema> fields = SchemaBuilder.record(className).namespace(tmpnames).fields();

            for (DataBaseColumnMeta meta : columnList) {
                if (meta.getColumnType().toString().equals(Const.META_TYPE_BIGINT)) {
                    fields = fields.name(meta.getColumnName()).type().nullable().longType().noDefault();
                } else if (meta.getColumnType().toString().equals(Const.META_TYPE_INTEGER)) {
                    fields = fields.name(meta.getColumnName()).type().nullable().intType().noDefault();
                } else if (meta.getColumnType().toString().equals(Const.META_TYPE_DOUBLE) || meta.getColumnType().toString().equals(Const.META_TYPE_NUMERIC)) {
                    fields = fields.name(meta.getColumnName()).type().nullable().doubleType().noDefault();
                } else if (meta.getColumnType().toString().equals(Const.META_TYPE_TIMESTAMP)) {
                    Schema timestampMilliType = LogicalTypes.timestampMillis().addToSchema(Schema.create(Schema.Type.LONG));
                    fields = fields.name(meta.getColumnName()).type(timestampMilliType).noDefault();
                } else if (meta.getColumnType().toString().equals(Const.META_TYPE_BOOLEAN)) {
                    fields = fields.name(meta.getColumnName()).type().nullable().booleanType().noDefault();
                } else if (meta.getColumnType().toString().equals(Const.META_TYPE_STRING)) {
                    fields = fields.name(meta.getColumnName()).type().nullable().stringType().noDefault();
                }
            }

            fields = fields.nullableBoolean("_UPDATE", false);
            fields = fields.nullableBoolean("_DELETE", false);
            schema = fields.endRecord();
        }
        return schema;
    }

    public static Schema getSchemaFromMeta(DataCollectionMeta colmeta) {
        Schema schema = null;
        if (colmeta.getResourceCfgMap().containsKey(Const.AVRO_SCHEMA_FILE_PARAM)) {
            String schemaPath = colmeta.getResourceCfgMap().get("schemaPath").toString();
            try {
                schema = new Schema.Parser().parse(new FileInputStream(new File(schemaPath)));
            } catch (IOException ex) {
                throw new ConfigurationIncorrectException("avro schema file load exception:" + ex.getMessage());
            }
        } else if (colmeta.getResourceCfgMap().containsKey(Const.AVRO_SCHEMA_CONTENT_PARAM)) {
            try {
                schema = new Schema.Parser().parse(colmeta.getResourceCfgMap().get(Const.AVRO_SCHEMA_CONTENT_PARAM).toString());
            } catch (RuntimeException ex) {
                throw ex;
            }
        } else {
            if (colmeta.getColumnList() != null) {
                SchemaBuilder.FieldAssembler<Schema> fields = SchemaBuilder.record(colmeta.getValueClassName()).namespace(colmeta.getClassNamespace()).fields();

                for (DataSetColumnMeta meta : colmeta.getColumnList()) {
                    if (meta.getColumnType().equals(Const.META_TYPE_BIGINT)) {
                        fields = fields.name(meta.getColumnName()).type().nullable().longType().noDefault();
                    } else if (meta.getColumnType().equals(Const.META_TYPE_INTEGER)) {
                        fields = fields.name(meta.getColumnName()).type().nullable().intType().noDefault();
                    } else if (meta.getColumnType().equals(Const.META_TYPE_DOUBLE) || meta.getColumnType().equals(Const.META_TYPE_NUMERIC)) {
                        fields = fields.name(meta.getColumnName()).type().nullable().doubleType().noDefault();
                    } else if (meta.getColumnType().equals(Const.META_TYPE_TIMESTAMP) || meta.getColumnType().equals(Const.META_TYPE_DATE)) {
                        Schema timestampMilliType = LogicalTypes.timestampMillis().addToSchema(Schema.create(Schema.Type.LONG));
                        fields = fields.name(meta.getColumnName()).type(timestampMilliType).noDefault();
                    } else if (meta.getColumnType().equals(Const.META_TYPE_BOOLEAN)) {
                        fields = fields.name(meta.getColumnName()).type().nullable().booleanType().noDefault();
                    } else if (meta.getColumnType().equals(Const.META_TYPE_STRING)) {
                        fields = fields.name(meta.getColumnName()).type().nullable().stringType().noDefault();
                    }
                }
                schema = fields.endRecord();
                logger.info(schema.toString(true));
            } else {
                throw new ConfigurationIncorrectException("missing avro schema config file or Content");
            }
        }
        return schema;
    }

    public static Protocol parseProtocolWithClassPath(String avroFile) throws IOException {
        return Protocol.parse(AvroUtils.class.getClassLoader().getResourceAsStream(avroFile));
    }

    public static Protocol parseProtocolWithFile(String avroFile) throws IOException {
        return Protocol.parse(new FileInputStream(new File(avroFile)));
    }

    public static Protocol parseProtocolWithString(String fileContent) throws IOException {
        return Protocol.parse(new ByteArrayInputStream(fileContent.getBytes()));
    }
    public static GenericRecord parse(Schema schema,byte[] value){
        GenericDatumReader<GenericRecord> reader = new GenericDatumReader<>(schema);
        ByteArrayInputStream os = new ByteArrayInputStream(value);
        try{
            GenericRecord record=new GenericData.Record(schema);
            Decoder de = DecoderFactory.get().binaryDecoder(os, null);
            reader.read(record,de);
            return record;
        }catch (IOException ex){
            logger.error("{}",ex);
        }
        return null;
    }

    /**
     * unserailize model object
     * @param schema
     * @param bytes
     * @param valueObj
     * @param additionalClazz
     */
    public static void byteToObject(Schema schema,byte[] bytes,Object valueObj,Class... additionalClazz){
        Assert.notNull(valueObj,"object should not be null");
        GenericRecord record=parse(schema,bytes);
        try {
            if (valueObj.getClass().isAssignableFrom(List.class)) {
                List list = (List) valueObj;
                if (!CollectionUtils.isEmpty(list)) {
                    Class targetClazz = list.get(0).getClass();
                    Map<String, Method> setMap = ReflectUtils.returnSetMethods(targetClazz);
                    if(record.getSchema().getType().equals(Schema.Type.ARRAY)){
                        List<GenericRecord> flist= (List<GenericRecord>)record.get(0);
                        for(GenericRecord g:flist){
                            Object t = targetClazz.newInstance();
                            Iterator<Map.Entry<String,Method>> iter=setMap.entrySet().iterator();
                            while(iter.hasNext()){
                                Map.Entry<String,Method> entry=iter.next();
                                if(g.get(entry.getKey())!=null){
                                    Schema eleType=schema.getField(entry.getKey()).schema().getTypes().get(0).getElementType();
                                    entry.getValue().invoke(t, acquireGenericRecord(entry.getKey(),g.get(entry.getKey()),eleType));
                                }
                            }
                            list.add(t);
                        }
                    }
                }
            } else if (valueObj.getClass().isAssignableFrom(Map.class)) {
                Assert.isTrue(additionalClazz.length>0,"");
                Map<String,Object> map=(Map<String, Object>)valueObj;
                List<Schema.Field> fields=schema.getFields();
                for(Schema.Field field:fields){
                    if(record.get(field.name())!=null){
                        Object vobj=additionalClazz[0].newInstance();
                        acquireModel(record,vobj);
                        map.put(field.name(),vobj);
                    }
                }

            } else if (valueObj.getClass().isAssignableFrom(Serializable.class)) {
                acquireModel(record,valueObj);
            }
        }catch (Exception ex){

        }
    }
    public static Object acquireGenericRecord(String key, Object value, Schema schema){
        try {
            if(value!=null) {
                if(value instanceof List){
                    List<GenericRecord> records=new ArrayList<>();
                    List<?> list=(List<?>) value;
                    if(CollectionUtils.isEmpty(list)){
                        return null;
                    }
                    Map<String, Method> getMethods = ReflectUtils.returnGetMethods(list.get(0).getClass());
                    Schema eleType=schema.getField(key).schema().getTypes().get(0).getElementType();
                    for(Object t:list){
                        GenericRecord record=new GenericData.Record(eleType);
                        Iterator<Map.Entry<String, Method>> iter = getMethods.entrySet().iterator();
                        while(iter.hasNext()){
                            Map.Entry<String, Method> entry = iter.next();
                            record.put(entry.getKey(), acquireGenericRecord(entry.getKey(),entry.getValue().invoke(t, null),eleType));
                        }
                        records.add(record);
                    }
                    return records;
                }else if (value.getClass().getInterfaces()!=null && value.getClass().getInterfaces().length>0 && value.getClass().getInterfaces()[0].isAssignableFrom(Map.class)){
                    Map<String,Object> map1=(Map<String,Object>) value;
                    Class clazz=map1.values().iterator().next().getClass();
                    if(ClassUtils.isPrimitiveOrWrapper(clazz) || clazz.isAssignableFrom(String.class)){
                        return value;
                    }else{
                        Schema mapschema=schema.getField(key).schema().getTypes().get(0).getValueType();
                        Iterator<Map.Entry<String,Object>> iterator=map1.entrySet().iterator();
                        Map<String,GenericRecord> retMap=new HashMap<>();

                        while(iterator.hasNext()){
                            GenericRecord genericRecord;
                            Map.Entry<String,Object> entry=iterator.next();
                            genericRecord=(GenericRecord) acquireGenericRecord(entry.getKey(),entry.getValue(),mapschema);
                            retMap.put(entry.getKey(),genericRecord);
                        }
                        return retMap;
                    }
                }
                else if(value.getClass().isAssignableFrom(Date.class)){
                    return ((Date) value).getTime();
                }else if(value.getClass().isAssignableFrom(Timestamp.class)){
                    return ((Timestamp) value).getTime();
                }else if(value.getClass().isAssignableFrom(LocalDateTime.class)){
                    return ((LocalDateTime) value).toInstant(ZoneOffset.of("+8")).toEpochMilli();
                }else if(value.getClass().isAssignableFrom(String.class)){
                    return value;
                }else if(ClassUtils.isPrimitiveOrWrapper(value.getClass())){
                    return value;
                }
                else {
                    GenericRecord record = new GenericData.Record(schema);
                    Map<String, Method> getMethods = ReflectUtils.returnGetMethods(value.getClass());
                    Iterator<Map.Entry<String, Method>> iter = getMethods.entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry<String, Method> entry = iter.next();
                        if (schema.getField(entry.getKey()) != null) {
                            record.put(entry.getKey(), acquireGenericRecord(entry.getKey(),entry.getValue().invoke(value, null),schema.getField(entry.getKey()).schema()));
                        }
                    }
                    return record;
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }
    public static void acquireModel(GenericRecord record,Object targetObj) throws Exception{
        List<Schema.Field> fields=record.getSchema().getFields();
        Map<String,Method> setMap=ReflectUtils.returnSetMethods(targetObj.getClass());
        for(Schema.Field field:fields){

            if((field.schema().getType().equals(Schema.Type.UNION) && field.schema().getTypes().get(0).getType().equals(Schema.Type.LONG)) || field.schema().getType().equals(Schema.Type.LONG)){
                Long val=(Long)record.get(field.name());
                if(setMap.get(field.name()).getParameterTypes()[0].isAssignableFrom(Date.class)){
                    setMap.get(field.name()).invoke(targetObj,new Date(val));
                }else if(setMap.get(field.name()).getParameterTypes()[0].isAssignableFrom(Timestamp.class)){
                    setMap.get(field.name()).invoke(targetObj,new Timestamp(val));
                }else if(setMap.get(field.name()).getParameterTypes()[0].isAssignableFrom(LocalDateTime.class)){
                    setMap.get(field.name()).invoke(targetObj,LocalDateTime.ofInstant(Instant.ofEpochMilli(val), ZoneId.systemDefault()));
                }else if(setMap.get(field.name()).getParameterTypes()[0].isAssignableFrom(Long.class)){
                    setMap.get(field.name()).invoke(targetObj,val);
                }
            }
            else if(field.schema().getType().equals(Schema.Type.RECORD)){
                Object vobj=setMap.get(field.name()).getParameterTypes()[0].newInstance();
                acquireModel((GenericRecord)record.get(field.name()),vobj);
                setMap.get(field.name()).invoke(targetObj,vobj);
            }else if(field.schema().getTypes().get(0).getType().equals(Schema.Type.MAP)){
                Type[] genericClazzs=((ParameterizedType)setMap.get(field.name()).getGenericParameterTypes()[0]).getActualTypeArguments();
                if(!genericClazzs[1].getTypeName().endsWith(".Object")){

                }else{

                }
            }
            else if(field.schema().getTypes().size()>0 && field.schema().getTypes().get(0).getType().equals(Schema.Type.ARRAY)){
                Type genericClazz=((ParameterizedType)setMap.get(field.name()).getGenericParameterTypes()[0]).getActualTypeArguments()[0];
                List list=new ArrayList();
                List<GenericRecord> records=(List<GenericRecord>) record.get(field.name());
                if(!CollectionUtils.isEmpty(records)){
                    for(GenericRecord rec:records) {
                        Object vobj = Class.forName(genericClazz.getTypeName()).newInstance();
                        acquireModel(rec,vobj);
                        list.add(vobj);
                    }
                }
                setMap.get(field.name()).invoke(targetObj,list);
            }
            else{
                if(field.schema().getTypes().get(0).getType().equals(Schema.Type.STRING)){
                    setMap.get(field.name()).invoke(targetObj, record.get(field.name()).toString());
                }else {
                    setMap.get(field.name()).invoke(targetObj, record.get(field.name()));
                }
            }
        }
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

    public static byte[] dataToByteWithBijection(GenericRecord record, Injection<GenericRecord, byte[]> injection) {
        return injection.apply(record);
    }

    public static byte[] dataToByteWithBijection(Schema schema, GenericRecord record) {
        Injection<GenericRecord, byte[]> injection = GenericAvroCodecs.apply(schema);
        return injection.apply(record);
    }


    /**
     * return schema with specify Serializable object
     *
     * @param serialClazz
     * @return
     */
    public static Schema getSchemaFromModel(Class serialClazz) {
        Map<String, Field> methodMap = ReflectUtils.getAllField(serialClazz);
        Iterator<Map.Entry<String, Field>> iter = methodMap.entrySet().iterator();
        SchemaBuilder.FieldAssembler<Schema> fields = SchemaBuilder.record(serialClazz.getName()).fields();
        while (iter.hasNext()) {
            Map.Entry<String, Field> entry = iter.next();
            Field field = entry.getValue();
            String key = entry.getKey();
            Class parameterType = field.getType();
            if (parameterType.isAssignableFrom(Long.class)) {
                fields = fields.name(key).type().nullable().longType().noDefault();
            } else if (parameterType.isAssignableFrom(Float.class)) {
                fields = fields.name(key).type().nullable().floatType().noDefault();
            } else if (parameterType.isAssignableFrom(Double.class)) {
                fields = fields.name(key).type().nullable().doubleType().noDefault();
            } else if (parameterType.isAssignableFrom(Integer.class)) {
                fields = fields.name(key).type().nullable().intType().noDefault();
            } else if (parameterType.isAssignableFrom(Date.class) || parameterType.isAssignableFrom(Timestamp.class) || parameterType.isAssignableFrom(LocalDateTime.class)) {
                Schema timestampMilliType = LogicalTypes.timestampMillis().addToSchema(Schema.create(Schema.Type.LONG));
                fields = fields.name(key).type(timestampMilliType).noDefault();
            } else if (parameterType.isAssignableFrom(Boolean.class)) {
                fields = fields.name(key).type().nullable().booleanType().noDefault();
            } else if (parameterType.isAssignableFrom(Map.class)) {
                Type acutalType=((ParameterizedType) field.getGenericType()).getActualTypeArguments()[1];
                if(((Class) acutalType).isAssignableFrom(Object.class)){
                    fields = fields.name(key).type().nullable().map().values().nullable().stringType().noDefault();
                }else{
                    fields = fields.name(key).type().nullable().map().values(getSchemaFromModel((Class) acutalType)).noDefault();
                }
            } else if (parameterType.isAssignableFrom(List.class)) {
                fields = fields.name(key).type().nullable().array().items(getSchemaFromModel((Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0])).noDefault();
            } else if (parameterType.isAssignableFrom(String.class)) {
                fields = fields.name(key).type().nullable().stringType().noDefault();
            }

        }
        Schema schema = fields.endRecord();
        return schema;
    }


    public static Map<String, Object> byteArrayBijectionToMap(Schema schema, Injection<GenericRecord, byte[]> injection, byte[] input) {
        GenericRecord record = injection.invert(input).get();
        List<Schema.Field> fields = schema.getFields();
        Map<String, Object> map = Maps.newHashMap();
        for (Schema.Field field : fields) {
            if (null != record.get(field.name())) {
                map.put(field.name(), record.get(field.name()));
            }
        }
        return map;
    }

    public static Map<String, Object> byteArrayToMap(DataCollectionMeta meta, Schema schema, byte[] byteData) {
        GenericRecord genericRecord = byteArrayToData(schema, byteData);
        Map<String, Object> map = new HashMap<>();
        for (DataSetColumnMeta columnMeta : meta.getColumnList()) {
            if (null != genericRecord.get(columnMeta.getColumnName())) {
                map.put(columnMeta.getColumnName(), genericRecord.get(columnMeta.getColumnName()));
            }
        }
        return map;
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
                if (byteArrayInputStream != null) {
                    byteArrayInputStream.close();
                }
            } catch (IOException e) {

            }
        }
    }
}
