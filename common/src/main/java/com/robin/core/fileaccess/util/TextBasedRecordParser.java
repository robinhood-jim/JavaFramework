package com.robin.core.fileaccess.util;


import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.robin.comm.util.json.GsonUtil;
import com.robin.core.base.datameta.DataBaseColumnMeta;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextBasedRecordParser {
    private static final Gson gson= GsonUtil.getGson();
    private TextBasedRecordParser(){

    }
    public static Map<String,Object> parseTextStream(DataCollectionMeta collectionMeta, String inputLine){
        Map<String,Object> retMap=new HashMap<>();
        if(Const.FILESUFFIX_CSV.equalsIgnoreCase(collectionMeta.getFileFormat())){
            String[] valArr=inputLine.split(collectionMeta.getSplit(),-1);

            for(int i=0;i<collectionMeta.getColumnList().size();i++){
                DataSetColumnMeta setColumnMeta=collectionMeta.getColumnList().get(i);

                if(i<valArr.length) {
                    retMap.put(setColumnMeta.getColumnName(), ConvertUtil.parseParameter(setColumnMeta, valArr[i]));
                }
            }
        }else if(Const.FILESUFFIX_JSON.equalsIgnoreCase(collectionMeta.getFileFormat())){
            retMap=gson.fromJson(inputLine,new TypeToken<Map<String,Object>>(){}.getType());
        }else if(Const.FILESUFFIX_XML.toString().equalsIgnoreCase(collectionMeta.getFileFormat())){
            //TODO
        }
        return retMap;
    }
}
