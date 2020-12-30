package com.robin.etl.util;

import org.apache.commons.lang3.tuple.Pair;

import java.time.LocalDateTime;
import java.util.Map;

public interface IprocessCycleGen {
    Pair<Boolean,String> genRunCycleByTypeAndStartTime(Integer runCycle, Map<String,Object> configMap, LocalDateTime dateTime);
    void finishCycle(String runCycle);
}
