package com.robin.etl.validator;

import com.robin.etl.context.StatefulJobContext;
import com.robin.etl.context.StepContext;

public class HdfsResourceValidator implements IresourceValidator {
    @Override
    public boolean checkResourceReady(StatefulJobContext jobContext, StepContext stepContext, String processDate) {
        return false;
    }
}
