package com.robin.core.web.serverless;

import java.util.Map;

public interface IUserDefineServerlessFunction {
    Object doFunction(Map<String,Object> map);
}
