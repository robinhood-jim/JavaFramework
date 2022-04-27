package com.robin.comm.util.xls;

import javax.annotation.Nullable;
import java.util.Map;

@FunctionalInterface
public interface IExcelAfterProcessor {
    @Nullable
    void processLine(Map<String,String> rowMap);
}
