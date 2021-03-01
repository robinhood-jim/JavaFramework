package com.robin.etl.validator;

import com.robin.etl.context.StatefulJobContext;
import com.robin.etl.context.StepContext;

import java.util.Map;

public interface IresourceValidator {
    boolean checkResourceReady(StatefulJobContext jobContext, StepContext stepContext, String processDate);
}
