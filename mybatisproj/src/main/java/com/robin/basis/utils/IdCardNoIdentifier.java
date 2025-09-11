package com.robin.basis.utils;

import cn.hutool.core.util.StrUtil;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

public class IdCardNoIdentifier {
    private static final DateTimeFormatter formatter=DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static Map<String,Object> identifyByCode(String code){
        Assert.isTrue(StrUtil.isNotBlank(code) && (code.length()==15 || code.length()==18),"");
        String year;
        String month;
        String day;
        Map<String,Object> retMap=new HashMap<>();
        boolean ifSuccess=false;
        long diffYear=0L;
        retMap.put("districtId",code.substring(0,6));
        if(code.length()==18){
            year=code.substring(6,10);
            month=code.substring(10,12);
            day=code.substring(12,14);
            if(Integer.valueOf(month)>11 || Integer.valueOf(day)>31){
                ifSuccess=false;
            }else{
                ifSuccess=true;
                diffYear=getAge(year+"-"+month+"-"+day) ;
                retMap.put("age",diffYear);
                retMap.put("birthDay",year+"-"+month+"-"+day);
            }
            if(ifSuccess && Integer.parseInt(code.substring(16,17)) %2!=0){
                retMap.put("gender","1");
            }else{
                retMap.put("gender","2");
            }
        }else{
            year="19"+code.substring(6,8);
            month=code.substring(8,10);
            day=code.substring(10,12);
            if(Integer.valueOf(month)>11 || Integer.valueOf(day)>31){
                ifSuccess=false;
            }else {
                ifSuccess = true;
                diffYear=getAge(year+"-"+month+"-"+day) ;
                retMap.put("age",diffYear);
                retMap.put("birthDay",year+"-"+month+"-"+day);
            }
            if(ifSuccess && Integer.parseInt(code.substring(14,15)) %2!=0){
                retMap.put("gender","1");
            }else{
                retMap.put("gender","2");
            }
        }
        retMap.put("success",ifSuccess);
        return retMap;
    }
    private static Long getAge(String brithDay){
        LocalDateTime birthTime= LocalDate.parse(brithDay,formatter).atStartOfDay();
        return ChronoUnit.YEARS.between(birthTime,LocalDateTime.now());
    }
}
