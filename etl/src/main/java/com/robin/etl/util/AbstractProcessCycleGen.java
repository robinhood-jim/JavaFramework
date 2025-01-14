package com.robin.etl.util;

import com.robin.etl.common.EtlConstant;
import org.apache.commons.lang3.tuple.Pair;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class AbstractProcessCycleGen {
    protected static DateTimeFormatter yearFormatter = DateTimeFormatter.ofPattern("yyyy");
    protected static DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyyMM");
    protected static DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    protected static DateTimeFormatter hourFormatter = DateTimeFormatter.ofPattern("yyyyMMddHH");

    public abstract Pair<Boolean,String> genRunCycleByTypeAndStartTime(Integer runCycle, LocalDateTime dateTime);
    public abstract Pair<String,LocalDateTime> getNextRunningCycle(Integer cycleType,LocalDateTime dateTime);
    public abstract void finishCycle(String runCycle);
    public abstract LocalDateTime parseTimeByType(String cycle,Integer cycleType);
}
