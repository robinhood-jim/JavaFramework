package com.robin.basis.service.region;

import com.robin.basis.model.region.Region;
import com.robin.basis.vo.RegionVO;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.service.IMybatisBaseService;

import java.util.List;
import java.util.Map;

public interface IRegionService extends IMybatisBaseService<Region,Long> {
    List<String> getRegionLevel(String districtCode);
    Map<String, RegionVO> getProvinceMap(String provinceId) throws ServiceException;
}
