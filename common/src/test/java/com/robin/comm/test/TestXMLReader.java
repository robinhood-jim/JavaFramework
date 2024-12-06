package com.robin.comm.test;

import com.google.gson.Gson;
import com.robin.comm.util.redis.JedisClientFactory;
import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.iterator.IResourceIterator;
import com.robin.core.fileaccess.iterator.TextFileIteratorFactory;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.AvroUtils;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericRecord;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressWarnings("unchecked")
public class TestXMLReader {
    public static void main(String[] args) {
        DataCollectionMeta colmeta = new DataCollectionMeta();
        colmeta.addColumnMeta("col1", Const.META_TYPE_BIGINT, null);
        colmeta.addColumnMeta("col2", Const.META_TYPE_STRING, null);
        colmeta.addColumnMeta("id", Const.META_TYPE_STRING, null);
        colmeta.addColumnMeta("key", Const.META_TYPE_STRING, null);
        colmeta.setEncode("UTF-8");
        colmeta.setPath("f:/test.xml");
        colmeta.setFileFormat(Const.FILETYPE_XML);
        try {

            IResourceIterator iter = TextFileIteratorFactory.getProcessIteratorByType(colmeta);
            //iter.beforeProcess(colmeta.getPath());
            while (iter.hasNext()) {
                System.out.println(iter.next());
            }
            iter.afterProcess();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    @Test
    public void testArffRead(){
        DataCollectionMeta colmeta = new DataCollectionMeta();
        colmeta.setPath("file:///f:/iris.arff");
        colmeta.setFileFormat(Const.FILEFORMATSTR.ARFF.getValue());
        try(IResourceIterator iterator=TextFileIteratorFactory.getProcessIteratorByType(colmeta)){
            while(iterator.hasNext()){
                System.out.println(iterator.next());
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }

    }

    @Test
    public void test1() throws Exception {
        //Schema schema= AvroUtils.getSchemaFromModel(TestCompondObj.class);
        List<TestCompondObj> compondObjs = new ArrayList<>();
        Gson gson = new Gson();
        TestCompondObj t1 = new TestCompondObj();
        t1.setId(1L);
        t1.setSqlDate(new Date());
        t1.setTname("t1");
        InnerClass in1 = new InnerClass();
        in1.setId(1111L);
        in1.setName("inner1");
        in1.setDate(new Timestamp(System.currentTimeMillis()));
        List<InnerClass> innerClasses = new ArrayList<>();
        innerClasses.add(in1);
        t1.setInner(innerClasses);
        compondObjs.add(t1);
        JedisClientFactory.JedisClient client = JedisClientFactory.getInstance();
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream outputStream = new ObjectOutputStream(arrayOutputStream);
        outputStream.writeObject(compondObjs);
        System.out.println(arrayOutputStream.toByteArray().length);
        System.out.println(gson.toJson(compondObjs).length());
        Schema schema = AvroUtils.getSchemaFromModel(TestCompondObj.class);

        SchemaBuilder.FieldAssembler<Schema> assembler = SchemaBuilder.record("nested").fields();
        assembler.name("list").type().nullable().array().items(schema).noDefault();
        Schema nestedSchema = assembler.endRecord();
        byte[] bytes = client.putSetWithSchema(schema, nestedSchema, compondObjs);

        GenericRecord record = AvroUtils.parse(nestedSchema, bytes);
        List<GenericRecord> list = (List<GenericRecord>) record.get(0);
        for (GenericRecord rec : list) {
            TestCompondObj v = new TestCompondObj();
            AvroUtils.acquireModel(rec, v);
        }
        System.out.println(list);
    }
}
