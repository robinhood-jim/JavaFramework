package com.robin.basis.controller.main;

import com.robin.core.web.controller.AbstractController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/home")
public class HomeController extends AbstractController {
    @GetMapping("/count")
    public Map<String,Object> statCount(){
        Map<String,Object> retMap=new HashMap<>();
        retMap.put("inventoryValue",1000.43);
        Map<String,Object> todayMap=new HashMap<>();
        todayMap.put("orderCount",20);
        todayMap.put("saleCount",1111.0);
        todayMap.put("costCount",1111.0);
        todayMap.put("profit",1111.0);
        retMap.put("today",todayMap);
        return wrapObject(retMap);
    }

}
