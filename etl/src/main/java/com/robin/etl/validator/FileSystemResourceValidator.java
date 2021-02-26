package com.robin.etl.validator;

import com.robin.etl.context.StatefulJobContext;
import com.robin.etl.context.StepContext;

import java.util.Map;

public class FileSystemResourceValidator implements IresourceValidator {

    @Override
    public boolean checkResourceReady(StatefulJobContext jobContext, StepContext stepContext, String processDate) {

        return false;
    }
}
