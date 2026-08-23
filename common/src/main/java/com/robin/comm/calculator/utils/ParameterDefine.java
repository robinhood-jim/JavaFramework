package com.robin.comm.calculator.utils;

import lombok.Data;

@Data
public class ParameterDefine{
    private String parameterName;
    private boolean ifFunction;
    private FunctionDefine subFunction;
}