package com.robin.comm.util.regex;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternMatcherUtils {
    public static final Pattern PATTERN_TEMPLATE_CALENDAR = Pattern.compile("\\$\\[(yy)?(YY)?(yyyy)?(YYYY)?(/)?(-)?(MM)?(/)?(-)?(dd)?(DD)?(\\s)?(HH)?(mm)?(HH:mm:ss)?(HHmmss)?(HH:MM:SS)?(HHMMSS)?[+-]?[0-9]*(D|d|M|m|H|S)?\\]");
    public static final Pattern PATTERN_TEMPLATE_PARAM = Pattern.compile("\\$\\[.*?\\]");
    public static final Pattern PATTERN_TEMPLATE_FUNCTION=Pattern.compile("\\w+\\(.*?\\)");
    public static final Pattern PATTERN_DATE_SUFFIX=Pattern.compile("[+-]?\\d+[YMdDHms]");
    public static String parseRegexParams(String regexStr, Map<String,Object> paramMap){
        Matcher matcher=PATTERN_TEMPLATE_CALENDAR.matcher(regexStr);
        StringBuffer buffer=new StringBuffer();
        while(matcher.find()){
            String keyword=matcher.group();
            String v_word = keyword.replaceFirst("\\$\\[", "").replaceFirst("\\]", "");
            if(v_word.contains("+") || v_word.contains("-")){
                int pos=v_word.indexOf("+")==-1?v_word.indexOf("-"):v_word.indexOf("+");
                String parseDate=parseDateFormat(v_word.substring(0,pos),v_word.substring(pos));
                matcher.appendReplacement(buffer,parseDate);
            }
        }
        matcher.appendTail(buffer);
        Matcher paramMatcher=PATTERN_TEMPLATE_PARAM.matcher(buffer.toString());
        buffer.delete(0,buffer.length());
        while(paramMatcher.find()){
            String keyword=matcher.group();
            String v_word = keyword.replaceFirst("\\$\\[", "").replaceFirst("\\]", "");
            if(null!=paramMap.get(v_word)){
                paramMatcher.appendReplacement(buffer,paramMap.get(v_word).toString());
            }
        }
        paramMatcher.appendTail(buffer);
        return buffer.toString();
    }
    public static String parseDateFormat(String dateFormatStr,String suffix){
        Integer plusNum=Integer.valueOf(suffix.substring(1,suffix.length()-1));
        String addType=suffix.substring(suffix.length()-1);
        if(suffix.startsWith("-")){
            plusNum=plusNum*-1;
        }
        return returnDateStrWithType(dateFormatStr,null,addType,plusNum);
    }
    public static String returnDateStrWithType(String formatstr,LocalDateTime time,String addType,int opernum){
        String retStr="";
        DateTimeFormatter format=DateTimeFormatter.ofPattern(formatstr);
        LocalDateTime dateTime=null==time?LocalDateTime.now():time;
        if(addType!=null){
            if("D".equalsIgnoreCase(addType)){
                dateTime=dateTime.plus(opernum, ChronoUnit.DAYS);
            }else if("M".equalsIgnoreCase(addType)){
                dateTime=dateTime.plus(opernum,ChronoUnit.MONTHS);
            }else if("H".equalsIgnoreCase(addType)){
                dateTime=dateTime.plus(opernum,ChronoUnit.HOURS);
            }else if("m".equalsIgnoreCase(addType)){
                dateTime=dateTime.plus(opernum,ChronoUnit.MINUTES);
            }else if("S".equalsIgnoreCase(addType)){
                dateTime=dateTime.plus(opernum,ChronoUnit.SECONDS);
            }
            else if("Y".equalsIgnoreCase(addType)){
                dateTime=dateTime.plus(opernum,ChronoUnit.YEARS);
            }
        }
        retStr=format.format(dateTime);
        return retStr;
    }

}
