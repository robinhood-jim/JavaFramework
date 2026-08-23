package com.robin.comm.calculator.time;

import com.robin.comm.calculator.AbstractCalculator;
import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.ObjectUtils;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

public class AddMonths extends AbstractCalculator {
    public AddMonths(){
        functionName="ADDMONTHS";
    }
    public AddMonths(DataCollectionMeta collectionMeta){
        super(collectionMeta);
        functionName="ADDMONTHS";
    }

    @Override
    public Pair<Const.CLASSTYPE, Object> doCalculator(Map<String, Object> inputObj,String[] parameters) {
        setInput(inputObj);
        LocalDateTime dateTime=null;
        Object originTime=getValue(parameters[0]);
        long ts=-1L;
        if(!ObjectUtils.isEmpty(originTime)) {
            if (LocalDateTime.class.isAssignableFrom(originTime.getClass())) {
                dateTime=(LocalDateTime) originTime;
            }else if(java.util.Date.class.isAssignableFrom(originTime.getClass())){
                java.util.Date date=(java.util.Date)originTime;
                dateTime=date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            }else if(Timestamp.class.isAssignableFrom(originTime.getClass())){
                ts=((Timestamp)originTime).getTime();
            }else if(Long.class.isAssignableFrom(originTime.getClass())){
                ts=Long.parseLong(originTime.toString());
            }
        }
        if(ts!=-1 && dateTime==null){
            dateTime= Instant.ofEpochMilli(ts).atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
        if(dateTime!=null && !ObjectUtils.isEmpty(getValue(parameters[1]))){
            dateTime.plusMonths(Long.parseLong(getValue(parameters[1]).toString()));
        }
        return Pair.of(Const.CLASSTYPE.TIMESTAMP,dateTime);
    }
}
