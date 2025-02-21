package com.robin.test;

import com.robin.core.base.util.Const;
import com.robin.core.base.util.StringUtils;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.ByteBufferMapSerializer;
import com.robin.core.fileaccess.util.ByteBufferOutputStream;
import org.apache.flink.core.memory.MemorySegment;
import org.apache.flink.core.memory.MemorySegmentFactory;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class OffHeapMapTest {
    @Test
    public void testSerialAndUnSerial(){
        DataCollectionMeta.Builder builder=new DataCollectionMeta.Builder();
        builder.addColumn("id", Const.META_TYPE_BIGINT,null);
        builder.addColumn("name",Const.META_TYPE_STRING,null);
        builder.addColumn("description",Const.META_TYPE_STRING,null);
        builder.addColumn("sno",Const.META_TYPE_INTEGER,null);
        builder.addColumn("price",Const.META_TYPE_DOUBLE,null);
        builder.addColumn("amount",Const.META_TYPE_INTEGER,null);
        builder.addColumn("type",Const.META_TYPE_INTEGER,null);
        DataCollectionMeta colmeta=builder.build();

        Map<String, Object> recMap = new HashMap<>();
        Random random = new Random(123123123123L);
        Map<Integer,Double> priceMap=new HashMap<>();
        for (int i=1;i<11;i++){
            priceMap.put(i,i*10.0);
        }
        MemorySegment segment= MemorySegmentFactory.allocateOffHeapUnsafeMemory(1024*1024*32,this,new Thread(){});
        ByteBuffer byteBuffer=segment.getOffHeapBuffer();
        try(ByteBufferOutputStream outputStream=new ByteBufferOutputStream(byteBuffer)) {
            for (int i = 0; i < 1000; i++) {
                recMap.put("id", Long.valueOf(i));
                recMap.put("name", StringUtils.generateRandomChar(8));
                if(i%20!=0) {
                    recMap.put("description", StringUtils.generateRandomChar(8));
                }
                recMap.put("sno", random.nextInt(10) + 1);
                recMap.put("amount", random.nextInt(50) + 1);
                recMap.put("price", priceMap.get((Integer) recMap.get("sno")));
                recMap.put("type", random.nextInt(2));
                outputStream.seek(i*64);
                ByteBufferMapSerializer.serialize(outputStream,colmeta.getColumnList(),recMap,64,1024*1024*32);
            }
            List<Map<String,Object>> list= ByteBufferMapSerializer.deSerialize(byteBuffer,colmeta.getColumnList(),64);
            list.stream().forEach(System.out::println);
        }catch (IOException ex){
            ex.printStackTrace();
        }finally {
            segment.free();
        }



    }
}
