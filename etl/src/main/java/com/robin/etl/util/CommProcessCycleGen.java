package com.robin.etl.util;

import com.robin.etl.common.EtlConstant;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;

public class CommProcessCycleGen extends AbstractProcessCycleGen {
    private LocalDateTime preTime;
    private String runCycle;
    private WeekFields weekFields = WeekFields.of(DayOfWeek.MONDAY, 1);

    private CommProcessCycleGen(){

    }
    private static CommProcessCycleGen instance=new CommProcessCycleGen();

    public static CommProcessCycleGen getInstance() {
        return instance;
    }

    @Override
    public Pair<Boolean, String> genRunCycleByTypeAndStartTime(@NonNull EtlConstant.CYCLE_TYPE cycleType,@NonNull LocalDateTime dateTime) {
        Assert.notNull(dateTime,"");
        boolean runTag = false;
        StringBuilder cycle = new StringBuilder();
        if (null == preTime && null != dateTime) {
            preTime = dateTime;
        }
        int month = dateTime.getMonthValue();
        int day = dateTime.getDayOfMonth();
        int dayOfYear = dateTime.getDayOfYear();
        int year = dateTime.getYear();
        int hour = dateTime.getHour();
        int minutes = dateTime.getMinute();

        if (cycleType.equals(EtlConstant.CYCLE_TYPE.YEAR)) {
            //年任务，读取指定的执行日期,执行周期为上一个年度
            cycle.append(year - 1);
            runTag = true;
        } else if (cycleType.equals(EtlConstant.CYCLE_TYPE.QUARTER)) {
            //季度,下一个季度第一个月开始判断
            int caculateYear = year;
            if (month % 3 == 1) {
                int quarter = month / 3;
                if (quarter == 0) {
                    quarter = 4;
                    caculateYear--;
                }
                cycle.append(caculateYear).append("Q").append(quarter);
            }
            runTag = true;
        } else if (cycleType.equals(EtlConstant.CYCLE_TYPE.MONTH)) {
            //每月的某一天
            LocalDateTime tmpTime = dateTime;
            //前一月
            tmpTime = tmpTime.minusMonths(1);
            cycle.append(caculateMonthStr(tmpTime));
            if (null == runCycle || !runCycle.equals(cycle)) {
                runTag = true;
            }
        } else if (cycleType.equals(EtlConstant.CYCLE_TYPE.XUN)) {
            LocalDateTime tmpTime = dateTime;
            //前一月
            tmpTime = tmpTime.minusMonths(1);
            cycle.append(caculateMonthStr(tmpTime));
            int xunNum=day/10;
            if(xunNum>2){
                xunNum=3;
            }else{
                xunNum++;
            }
            cycle.append("X"+xunNum);
        } else if (cycleType.equals(EtlConstant.CYCLE_TYPE.WEEK)) {
            //上一周范围
            LocalDateTime tmpDatetime = dateTime.minusWeeks(1);
            DayOfWeek dayOfWeek = tmpDatetime.getDayOfWeek();
            int value = dayOfWeek.getValue();
            cycle.append(year+"W"+value);
            runTag=true;

        } else if (cycleType.equals(EtlConstant.CYCLE_TYPE.DAY.getInt())) {
            LocalDateTime tmpTs=dateTime.minusDays(1);
            cycle.append(dayFormatter.format(tmpTs));
            runTag=true;
        }else if(cycleType.equals(EtlConstant.CYCLE_TYPE.HOUR)){
            LocalDateTime tmpTs=dateTime.minusHours(1);
            cycle.append(hourFormatter.format(tmpTs));
            runTag=true;
        }
        return Pair.of(runTag, cycle.toString());
    }

    @Override
    public Pair<String,LocalDateTime> getNextRunningCycle(EtlConstant.CYCLE_TYPE cycleType, LocalDateTime dateTime) {
        Assert.notNull(dateTime,"");
        String cycle=null;
        LocalDateTime tmpTs=null;
        if (EtlConstant.CYCLE_TYPE.YEAR.equals(cycleType)) {
            tmpTs= dateTime.plusYears(1);
            cycle=yearFormatter.format(tmpTs);
        }else if(EtlConstant.CYCLE_TYPE.QUARTER.equals(cycleType)){
            tmpTs=dateTime.plusMonths(3);
            int month = tmpTs.getMonthValue();
            int quarter = month / 3+1;
            cycle=yearFormatter.format(tmpTs)+"Q"+quarter;
        }else if(EtlConstant.CYCLE_TYPE.MONTH.equals(cycleType)){
            tmpTs=dateTime.minusMonths(1);
            cycle=monthFormatter.format(tmpTs);
        }
        else if(EtlConstant.CYCLE_TYPE.XUN.equals(cycleType)){
            tmpTs=dateTime.plusDays(10);
            int xun=tmpTs.getDayOfMonth()/10+1;
            if(xun==4){
                xun=3;
            }
            cycle=monthFormatter.format(tmpTs)+"X"+xun;
        }else if(EtlConstant.CYCLE_TYPE.DAY.equals(cycleType)){
            tmpTs=dateTime.plusDays(1);
            cycle=dayFormatter.format(tmpTs);
        }

        return Pair.of(cycle,tmpTs);
    }

    private String caculateMonthStr(LocalDateTime dateTime){
        int dateInt = 100 + dateTime.getMonthValue();
        return dateTime.getYear() + String.valueOf(dateInt).substring(2, 3);
    }

    @Override
    public LocalDateTime parseTimeByType(String cycle, Integer cycleType) {
        Assert.notNull(cycle, "");
        LocalDateTime dateTime = null;
        if (cycleType.equals(EtlConstant.CYCLE_TYPE.YEAR.getInt())) {
            Assert.isTrue(cycle.length() == 4, "");
            dateTime = LocalDateTime.parse(cycle, yearFormatter);
        } else if (cycleType.equals(EtlConstant.CYCLE_TYPE.QUARTER.getInt())) {
            Assert.isTrue(cycle.length() == 6, "");
            dateTime = LocalDateTime.parse(cycle.substring(0, 4), yearFormatter);
            int quartzNum = Integer.parseInt(cycle.substring(5));
            dateTime=dateTime.minusMonths((quartzNum - 1) * 3L);
        } else if (cycleType.equals(EtlConstant.CYCLE_TYPE.MONTH.getInt())) {
            Assert.isTrue(cycle.length() == 6, "");
            dateTime = LocalDateTime.parse(cycle, monthFormatter);
        } else if (cycleType.equals(EtlConstant.CYCLE_TYPE.XUN.getInt())) {
            Assert.isTrue(cycle.length() == 8, "");
            dateTime = LocalDateTime.parse(cycle.substring(0, 6), monthFormatter);
            int xunNum = Integer.parseInt(cycle.substring(7));
            dateTime=dateTime.minusDays((xunNum - 1) * 10L);
        } else if (cycleType.equals(EtlConstant.CYCLE_TYPE.DAY.getInt())) {
            Assert.isTrue(cycle.length() == 8, "");
            dateTime = LocalDateTime.parse(cycle, dayFormatter);
        }
        return dateTime;
    }

    @Override
    public void finishCycle(String runCycle) {
        this.runCycle = runCycle;
    }
}
