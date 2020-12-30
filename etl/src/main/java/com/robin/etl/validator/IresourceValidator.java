package com.robin.etl.validator;

import java.util.Map;

public interface IresourceValidator {
    boolean checkResourceReady(Map<String,Object> configMap, String processDate);
}
