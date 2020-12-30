package com.robin.etl.validator;

import java.util.Map;

public class HdfsResourceValidator implements IresourceValidator {
    @Override
    public boolean checkResourceReady(Map<String, Object> configMap, String processDate) {

        return false;
    }
}
