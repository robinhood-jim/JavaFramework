package com.robin.comm.util.lineprocess;

import java.io.Serializable;
import java.util.Map;


public interface MapResultProcess<T extends Serializable> {
    void doBefore(Map<String,T> record);
    void doAfter(Map<String,T> record);
    void doWithLine(Map<String,T> record);
}
