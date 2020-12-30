package com.robin.etl.util;

import com.robin.core.base.util.Const;
import org.apache.commons.lang3.tuple.Pair;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.Map;

public class CommProcessCycleGen implements IprocessCycleGen {
    private LocalDateTime preTime;
    private String runCycle;
    private String startFrom;
    private String endTo;
    private WeekFields weekFields = WeekFields.of(DayOfWeek.MONDAY,1);
    @Override
    public Pair<Boolean,String> genRunCycleByTypeAndStartTime(Integer cycleType, Map<String,Object> configMap, LocalDateTime dateTime) {
        boolean runTag=false;
        String cycle=null;
        if(null== preTime && null!=dateTime){
            preTime=dateTime;
        }
        int month=dateTime.getMonthValue();
        int day=dateTime.getDayOfMonth();
        int dayOfYear=dateTime.getDayOfYear();
        int year=dateTime.getYear();
        int hour=dateTime.getHour();
        int minutes=dateTime.getMinute();
        int triggerSpan=(null!=configMap.get(Const.TRIGGER_TIMESPAN))?Integer.valueOf(configMap.get(Const.TRIGGER_TIMESPAN).toString()):0;
        if(cycleType.equals(Const.CYCLE_TYPE.YEAR.getInt())){
            //年任务，读取指定的执行日期,执行周期为上一个年度
            if(configMap.containsKey(Const.TRIGGER_TIMESPAN)){
                if(dayOfYear==triggerSpan) {
                    cycle = String.valueOf(year - 1);
                    runTag = true;
                }
            }else{
                //没有具体触发日期,以1月1号为准
                if(dayOfYear==1) {
                    cycle = String.valueOf(year - 1);
                    runTag = true;
                }
            }
        }else if(cycleType.equals(Const.CYCLE_TYPE.QUARTER.getInt())){
            //季度,下一个季度第一个月开始判断
            int caculateYear=year;
            if(month % 3==1){
                int quarter=month / 3;
                if(quarter==0){
                    quarter=4;
                    caculateYear--;
                }
                StringBuilder builder=new StringBuilder();
                builder.append(caculateYear).append("Q").append(quarter);
                cycle=builder.toString();
            }
            if(day==triggerSpan){
                runTag=true;
            }
        }
        else if(cycleType.equals(Const.CYCLE_TYPE.MONTH.getInt())){
            if(configMap.containsKey(Const.TRIGGER_TIMESPAN)){
                //每月的某一天
                String dayStr=String.valueOf(100+day).substring(2,3);
                //String triggerSpan=configMap.get(Const.TRIGGER_TIMESPAN).toString();
                if(dayStr.equals(triggerSpan)){
                    LocalDateTime tmpTime=dateTime;
                    //前一月
                    tmpTime=tmpTime.minusMonths(1);
                    int dateInt=100+tmpTime.getMonthValue();
                    cycle=String.valueOf(tmpTime.getYear())+String.valueOf(dateInt).substring(2,3);
                    if(null==runCycle || !runCycle.equals(cycle)) {
                        runTag = true;
                    }
                }
            }
        }else if(cycleType.equals(Const.CYCLE_TYPE.XUN.getInt())){
            if(day!=31){

            }
        }
        else if(cycleType.equals(Const.CYCLE_TYPE.WEEK.getInt())){
            if(configMap.containsKey(Const.TRIGGER_TIMESPAN)){
                //上一周范围
                LocalDateTime tmpDatetime=dateTime.minusWeeks(1);
                DayOfWeek dayOfWeek=tmpDatetime.getDayOfWeek();
                int weeknum=tmpDatetime.get(weekFields.weekOfYear());
                int value=dayOfWeek.getValue();


            }
        }else if(cycleType.equals(Const.CYCLE_TYPE.DAY.getInt())){

        }
        return Pair.of(runTag,cycle);
    }

    @Override
    public void finishCycle(String runCycle) {
        this.runCycle=runCycle;
    }
}
