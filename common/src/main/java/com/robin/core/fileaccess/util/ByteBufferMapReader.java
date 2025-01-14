package com.robin.core.fileaccess.util;

import com.robin.core.base.exception.OperationNotSupportException;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class ByteBufferMapReader {

    public static List<Map<String,Object>> unSerialize(ByteBuffer byteBuffer,List<DataSetColumnMeta> colmeta,int recordlength){
        return null;
    }
    public Map<String,Object> unSerialize(ByteBufferInputStream inputStream,List<DataSetColumnMeta> columns,int recordlength,int pos){
        int startPos=recordlength*pos;
        Assert.isTrue(inputStream.capacity()>startPos,"");
        try {
            byte[] bytes=new byte[recordlength];
            inputStream.read(bytes,startPos,recordlength);

        }catch (IOException ex){

        }
        return null;
    }
    public static int serialize(ByteBuffer byteBuffer, List<DataSetColumnMeta> columns, Iterator<Map<String,Object>> iterator, int recordlength, int maxlength
                                        , BiConsumer<Map<String,Object>,Integer> consumer){

        int recordpos=0;
        int bytepos=0;
        try(ByteBufferOutputStream outputStream=new ByteBufferOutputStream(byteBuffer)) {
            while(iterator.hasNext()){
                if(bytepos>=maxlength){
                    throw new OperationNotSupportException("serialize overflow max capacity "+maxlength);
                }
                Map<String,Object> valueMap=iterator.next();
                serialize(outputStream,columns,valueMap,recordlength,maxlength);
                recordpos++;
                bytepos+=recordlength;
                consumer.accept(valueMap,recordpos);
            }

        }catch (IOException ex){

        }
        return bytepos;
    }
    public static void serialize(ByteBufferOutputStream outputStream, List<DataSetColumnMeta> columns, Map<String,Object> valueMap, int recordlength, int maxlength) throws IOException{
        int curpos=0;
        for(DataSetColumnMeta meta:columns){
            if(!ObjectUtils.isEmpty(valueMap.get(meta.getColumnName()))){
                curpos=outputStream.writePrimitive(valueMap.get(meta.getColumnName()),curpos, Charset.forName("utf8"));
            }else{
                throw new OperationNotSupportException(" column "+meta.getColumnName()+" is null,can not process");
            }
        }
        if(curpos<recordlength){
            outputStream.writeLeft(recordlength-curpos,0);
        }else {
            throw new OperationNotSupportException("record length over record limit "+recordlength);
        }
    }
}
