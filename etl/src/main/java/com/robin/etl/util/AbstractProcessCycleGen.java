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

    abstract Pair<Boolean,String> genRunCycleByTypeAndStartTime(EtlConstant.CYCLE_TYPE runCycle, LocalDateTime dateTime);
    abstract Pair<String,LocalDateTime> getNextRunningCycle(EtlConstant.CYCLE_TYPE cycleType,LocalDateTime dateTime);
    abstract void finishCycle(String runCycle);
    abstract LocalDateTime parseTimeByType(String cycle,Integer cycleType);
}
