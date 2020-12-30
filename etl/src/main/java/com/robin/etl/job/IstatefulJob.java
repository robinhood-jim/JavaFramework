package com.robin.etl.job;

import java.util.Map;
import java.util.ResourceBundle;

@FunctionalInterface
public interface IstatefulJob {
    int doProcess(ResourceBundle bundle, Map<String,Object> configMap,String processDate);
}
