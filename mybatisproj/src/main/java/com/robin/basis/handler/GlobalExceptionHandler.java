package com.robin.basis.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public Map<String,Object> exceptionHandler(Exception e){
        log.error("{}",e.getMessage());
        Map<String,Object> retmap=new HashMap<>();
        retmap.put("success", false);
        retmap.put("message",e.getMessage());
        return retmap;
    }
}
