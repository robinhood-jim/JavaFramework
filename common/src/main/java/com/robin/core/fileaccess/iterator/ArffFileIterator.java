package com.robin.core.fileaccess.iterator;

import com.google.common.collect.Lists;
import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;


public class ArffFileIterator extends PlainTextFileIterator{
    public ArffFileIterator(){
        identifier= Const.FILEFORMATSTR.ARFF.getValue();
    }

    public ArffFileIterator(DataCollectionMeta colmeta) {
        super(colmeta);
        identifier= Const.FILEFORMATSTR.ARFF.getValue();
    }

    @Override
    public void beforeProcess() {
        super.beforeProcess();
        if(CollectionUtils.isEmpty(colmeta.getColumnList())){
            if(!ObjectUtils.isEmpty(reader)){
                try {
                    while (!(readLineStr = reader.readLine()).equalsIgnoreCase("@data")) {
                        if(StringUtils.startsWithIgnoreCase(readLineStr,"@RELATION ")){
                            String relationName=readLineStr.substring(10).replace("'","");
                            colmeta.getResourceCfgMap().put("relationName",relationName);
                        }else if(StringUtils.startsWithIgnoreCase(readLineStr,"@attribute ")){
                            colmeta.addColumnMeta(parseDefine(readLineStr.substring(11)));
                        }
                    }
                }catch (IOException ex){
                    logger.info("{}",ex.getMessage());
                }
            }
        }
    }
    private DataSetColumnMeta parseDefine(String content){
        String[] arr=content.trim().split("\\||\\t");
        String type=arr[1].trim();
        String columnName=arr[0].trim();
        DataSetColumnMeta columnMeta=null;
        if("REAL".equalsIgnoreCase(arr[1]) || "numeric".equalsIgnoreCase(arr[1])){
            columnMeta=new DataSetColumnMeta(columnName,Const.META_TYPE_DOUBLE,null);
        }else if("string".equalsIgnoreCase(arr[1])){
            columnMeta=new DataSetColumnMeta(columnName,Const.META_TYPE_STRING,null);
        }else if("date".equalsIgnoreCase(type)){
            columnMeta=new DataSetColumnMeta(columnName,Const.META_TYPE_TIMESTAMP,null);
        }else if(type.startsWith("{")){
            List<String> nominalValues= Lists.newArrayList(type.substring(1,type.length()-1).split(","));
            columnMeta=new DataSetColumnMeta(columnName,Const.META_TYPE_STRING,null);
            columnMeta.setNominalValues(nominalValues);
        }
        return columnMeta;
    }

    @Override
    public boolean hasNext() {
         boolean hasRecord=super.hasNext();
         if(readLineStr.contains(",") || readLineStr.contains("@")){
             return hasRecord;
         }else{
             return false;
         }
    }
}
