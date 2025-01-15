package com.robin.core.fileaccess.util;

import com.robin.core.base.exception.GenericException;
import com.robin.core.base.exception.OperationNotSupportException;
import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.BiConsumer;

@Slf4j
public class ByteBufferMapSerializer {
    private ByteBufferMapSerializer(){

    }

    public static List<Map<String,Object>> deSerialize(ByteBuffer byteBuffer, List<DataSetColumnMeta> colmeta, int recordlength){
        int pos=0;
        List<Map<String,Object>> retList=new ArrayList<>();
        try(ByteBufferInputStream inputStream=new ByteBufferInputStream(byteBuffer,byteBuffer.position())){
            while(inputStream.available()>0 && (pos+1)*recordlength<inputStream.capacity()){
                Map<String,Object> valueMap=new HashMap<>();
                inputStream.seek(pos*recordlength);
                deSerialize(inputStream,colmeta,valueMap,recordlength,pos);
                retList.add(valueMap);
                pos++;
            }
        }catch (IOException ex){
            throw new GenericException(ex);
        }
        return retList;
    }
    public static void deSerialize(ByteBufferInputStream inputStream, List<DataSetColumnMeta> columns, Map<String,Object> valueMap, int recordlength, int pos){
        int startPos=recordlength*(pos-1);
        Assert.isTrue(inputStream.capacity()>startPos,"");
        try {
            valueMap.clear();

            for(int i=0;i<columns.size();i++){
                DataSetColumnMeta meta=columns.get(i);
                if(i>0){
                    int gap=inputStream.read();
                    //null column
                    if(gap==0xff){
                        valueMap.put(meta.getColumnName(),null);
                        continue;
                    }else if(gap!=0){
                        throw new OperationNotSupportException("read unexpected byte "+gap);
                    }
                }
                if(Const.META_TYPE_BIGINT.equals(meta.getColumnType())){
                    valueMap.put(meta.getColumnName(),inputStream.readLong());
                }else if(Const.META_TYPE_INTEGER.equals(meta.getColumnType())){
                    valueMap.put(meta.getColumnName(),inputStream.readInt());
                }else if(Const.META_TYPE_SHORT.equals(meta.getColumnType())){
                    valueMap.put(meta.getColumnName(),inputStream.readShort());
                }else if(Const.META_TYPE_DOUBLE.equals(meta.getColumnType())){
                    valueMap.put(meta.getColumnName(),inputStream.readDouble());
                }else if(Const.META_TYPE_TIMESTAMP.equals(meta.getColumnType())){
                    valueMap.put(meta.getColumnName(),inputStream.readTimeStamp());
                }else if(Const.META_TYPE_STRING.equals(meta.getColumnType())){
                    valueMap.put(meta.getColumnName(),new String(inputStream.readString(64),"utf8"));
                }
                else{
                    throw new OperationNotSupportException("type "+meta.getColumnType()+" can not supported");
                }
            }

        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
    public static int serialize(ByteBuffer byteBuffer, List<DataSetColumnMeta> columns, Iterator<Map<String,Object>> iterator, int recordlength, int maxlength
                                        , BiConsumer<Map<String,Object>,Integer> consumer){

        int recordpos=0;
        int bytepos=0;
        try(ByteBufferOutputStream outputStream=new ByteBufferOutputStream(byteBuffer)) {
            int pos=0;
            while(iterator.hasNext()){
                if(bytepos>=maxlength){
                    throw new OperationNotSupportException("serialize overflow max capacity "+maxlength);
                }
                Map<String,Object> valueMap=iterator.next();
                outputStream.seek(pos*maxlength);
                serialize(outputStream,columns,valueMap,recordlength,maxlength);
                recordpos++;
                bytepos+=recordlength;
                if(consumer!=null) {
                    consumer.accept(valueMap, recordpos);
                }
                pos++;
            }

        }catch (IOException ex){

        }
        return bytepos;
    }
    public static void serialize(ByteBufferOutputStream outputStream, List<DataSetColumnMeta> columns, Map<String,Object> valueMap, int recordlength, int maxlength) throws IOException{
        int fromPos=outputStream.getPosition();
        for(int i=0;i<columns.size();i++){
            DataSetColumnMeta meta=columns.get(i);
            if(!ObjectUtils.isEmpty(valueMap.get(meta.getColumnName()))){
                outputStream.writePrimitive(valueMap.get(meta.getColumnName()),i==columns.size()-1,Charset.forName("utf8"));
            }else{
                //value is null,separator be 0xff
               outputStream.writeNullTag();
            }
        }
        if(outputStream.getPosition()-fromPos<recordlength){
            //outputStream.writeLeft(recordlength-outputStream.getPosition()+fromPos,0);
        }else {
            throw new OperationNotSupportException("record length "+String.valueOf(outputStream.getPosition()-fromPos)+" over record limit "+recordlength);
        }
    }


}
