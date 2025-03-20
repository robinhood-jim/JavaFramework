package com.robin.basis.cache;

import com.robin.basis.model.region.Region;
import com.robin.basis.service.region.IRegionService;
import com.robin.basis.vo.RegionVO;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.util.Const;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CommonCache {
    @Resource
    private IRegionService regionService;

    @Cacheable(value = "regionCache", key = "#provinceId")
    public Map<String, RegionVO> getProvinceMap(String provinceId) throws ServiceException {
        List<Region> regions=regionService.queryByField(Region::getCode, Const.OPERATOR.RLIKE,provinceId);
        if(!CollectionUtils.isEmpty(regions)){
            return regions.stream().map(RegionVO::fromVO).collect(Collectors.toMap(RegionVO::getCode, Function.identity()));
        }else{
            throw new ServiceException("");
        }
    }
    @CacheEvict(value = "regionCache",key = "#provinceId")
    public void refreshResource(String resourceId){

    }
}
