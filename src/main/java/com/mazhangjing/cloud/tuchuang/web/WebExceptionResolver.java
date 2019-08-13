package com.mazhangjing.cloud.tuchuang.web;

import org.omg.SendingContext.RunTime;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@ResponseBody
public class WebExceptionResolver {

    @ExceptionHandler({Exception.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> resolveAllException(Exception e) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("response","Exception");
        map.put("reason",e.getMessage());
        return map;
    }

}
