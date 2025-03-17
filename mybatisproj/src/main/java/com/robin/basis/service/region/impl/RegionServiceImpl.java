package com.robin.basis.service.region.impl;

import cn.hutool.core.util.StrUtil;
import com.robin.basis.cache.CommonCache;
import com.robin.basis.mapper.RegionMapper;
import com.robin.basis.model.region.Region;
import com.robin.basis.service.region.IRegionService;
import com.robin.basis.vo.RegionVO;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.service.AbstractMybatisService;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RegionServiceImpl extends AbstractMybatisService<RegionMapper, Region,Long> implements IRegionService {
    @Resource
    private CommonCache cache;

    public List<String> getRegionLevel(String districtCode){
        Assert.isTrue(StrUtil.isNotBlank(districtCode) && districtCode.length()==6,"");
        List<String> displayList=new ArrayList<>();
        Map<String, RegionVO> regionMap = getProvinceMap(districtCode.substring(0,2));
        if(regionMap.containsKey(districtCode.substring(0,2))){
            displayList.add(regionMap.get(districtCode.substring(0,2)).getName());
        }
        if(regionMap.containsKey(districtCode.substring(0,4))){
            displayList.add(regionMap.get(districtCode.substring(0,4)).getName());
        }
        if(regionMap.containsKey(districtCode)){
            displayList.add(regionMap.get(districtCode).getName());
        }
        return displayList;
    }
    public Map<String, RegionVO> getProvinceMap(String provinceId) throws ServiceException {
        return cache.getProvinceMap(provinceId);
    }
}
