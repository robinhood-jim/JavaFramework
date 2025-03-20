package com.robin.basis.controller.region;

import com.robin.basis.mapper.RegionMapper;
import com.robin.basis.model.region.Region;
import com.robin.basis.service.region.IRegionService;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.util.Const;
import com.robin.core.web.controller.AbstractMyBatisController;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequestMapping("/region")
@RestController
public class RegionController extends AbstractMyBatisController<IRegionService, RegionMapper, Region,Long> {
    @GetMapping("provinces")
    public Map<String,Object> listProvince(){
        List<Region> regionList=service.queryByField(Region::getLevel, Const.OPERATOR.EQ,1);
        return wrapObject(regionList.stream().map(f->{
            Map<String,Object> map=new HashMap<>();
            map.put("label",f.getName());
            map.put("value",f.getName());
            return map;
        }).collect(Collectors.toList()));
    }
    @GetMapping("cities")
    public Map<String,Object> listCities(@RequestParam String province){
        Region region=service.getByField(Region::getName, Const.OPERATOR.EQ,province);
        if(ObjectUtils.isEmpty(region)){
            throw new ServiceException(" province not exists");
        }
        List<Region> regionList=service.queryByField(Region::getPCode, Const.OPERATOR.EQ,region.getCode());
        return wrapObject(regionList.stream().map(f->{
            Map<String,Object> map=new HashMap<>();
            map.put("label",f.getName());
            map.put("value",f.getName());
            return map;
        }).collect(Collectors.toList()));
    }
    @GetMapping("districts")
    public Map<String,Object> listDistricts(@RequestParam String city){
        Region region=service.getByField(Region::getName, Const.OPERATOR.EQ,city);
        if(ObjectUtils.isEmpty(region)){
            throw new ServiceException(" city not exists");
        }
        List<Region> regionList=service.queryByField(Region::getPCode, Const.OPERATOR.EQ,region.getCode());
        return wrapObject(regionList.stream().map(f->{
            Map<String,Object> map=new HashMap<>();
            map.put("label",f.getName());
            map.put("value",f.getName());
            return map;
        }).collect(Collectors.toList()));
    }
}
