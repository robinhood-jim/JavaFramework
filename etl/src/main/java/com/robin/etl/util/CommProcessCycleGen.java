package com.robin.etl.util;

import com.robin.core.base.util.Const;
import com.robin.etl.common.EtlConstant;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.tomcat.jni.Local;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Map;

public class CommProcessCycleGen implements IprocessCycleGen {
    private LocalDateTime preTime;
    private String runCycle;
    private String startFrom;
    private String endTo;
    private WeekFields weekFields = WeekFields.of(DayOfWeek.MONDAY, 1);
    private DateTimeFormatter yearFormatter = DateTimeFormatter.ofPattern("yyyy");
    private DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyyMM");
    private DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public Pair<Boolean, String> genRunCycleByTypeAndStartTime(@NonNull Integer cycleType,@NonNull LocalDateTime dateTime) {
        Assert.notNull(dateTime,"");
        boolean runTag = false;
        String cycle = null;
        if (null == preTime && null != dateTime) {
            preTime = dateTime;
        }
        int month = dateTime.getMonthValue();
        int day = dateTime.getDayOfMonth();
        int dayOfYear = dateTime.getDayOfYear();
        int year = dateTime.getYear();
        int hour = dateTime.getHour();
        int minutes = dateTime.getMinute();

        if (cycleType.equals(EtlConstant.CYCLE_TYPE.YEAR.getInt())) {
            //年任务，读取指定的执行日期,执行周期为上一个年度
            cycle = String.valueOf(year - 1);
            runTag = true;
        } else if (cycleType.equals(EtlConstant.CYCLE_TYPE.QUARTER.getInt())) {
            //季度,下一个季度第一个月开始判断
            int caculateYear = year;
            if (month % 3 == 1) {
                int quarter = month / 3;
                if (quarter == 0) {
                    quarter = 4;
                    caculateYear--;
                }
                StringBuilder builder = new StringBuilder();
                builder.append(caculateYear).append("Q").append(quarter);
                cycle = builder.toString();
            }
            runTag = true;
        } else if (cycleType.equals(EtlConstant.CYCLE_TYPE.MONTH.getInt())) {
            //每月的某一天

            LocalDateTime tmpTime = dateTime;
            //前一月
            tmpTime = tmpTime.minusMonths(1);
            int dateInt = 100 + tmpTime.getMonthValue();
            cycle =caculateMonthStr(tmpTime);
            if (null == runCycle || !runCycle.equals(cycle)) {
                runTag = true;
            }

        } else if (cycleType.equals(EtlConstant.CYCLE_TYPE.XUN.getInt())) {
            if (day != 31) {
                int xunNum=day/10;

            }
        } else if (cycleType.equals(EtlConstant.CYCLE_TYPE.WEEK.getInt())) {
            //上一周范围
            LocalDateTime tmpDatetime = dateTime.minusWeeks(1);
            DayOfWeek dayOfWeek = tmpDatetime.getDayOfWeek();
            int weeknum = tmpDatetime.get(weekFields.weekOfYear());
            int value = dayOfWeek.getValue();
            cycle=caculateMonthStr(tmpDatetime)+"W"+value;
            runTag=true;

        } else if (cycleType.equals(EtlConstant.CYCLE_TYPE.DAY.getInt())) {
            LocalDateTime tmpTs=dateTime.minusDays(1);
            cycle=dayFormatter.format(tmpTs);
            runTag=true;
        }
        return Pair.of(runTag, cycle);
    }

    @Override
    public Pair<String,LocalDateTime> getNextRunningCycle(Integer cycleType, LocalDateTime dateTime) {
        Assert.notNull(dateTime,"");
        String cycle=null;
        LocalDateTime tmpTs=null;
        if (cycleType.equals(EtlConstant.CYCLE_TYPE.YEAR.getInt())) {
            tmpTs= dateTime.plusYears(1);
            cycle=yearFormatter.format(tmpTs);
        }else if(cycleType.equals(EtlConstant.CYCLE_TYPE.QUARTER.getInt())){
            tmpTs=dateTime.plusMonths(3);
            int month = tmpTs.getMonthValue();
            int quarter = month / 3+1;
            cycle=yearFormatter.format(tmpTs)+"Q"+quarter;
        }else if(cycleType.equals(EtlConstant.CYCLE_TYPE.MONTH.getInt())){
            tmpTs=dateTime.minusMonths(1);
            cycle=monthFormatter.format(tmpTs);
        }
        else if(cycleType.equals(EtlConstant.CYCLE_TYPE.XUN.getInt())){
            tmpTs=dateTime.plusDays(10);
            int xun=tmpTs.getDayOfMonth()/10+1;
            if(xun==4){
                xun=3;
            }
            cycle=monthFormatter.format(tmpTs)+"X"+xun;
        }else if(cycleType.equals(EtlConstant.CYCLE_TYPE.DAY.getInt())){
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
