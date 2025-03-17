package com.robin.basis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.robin.basis.model.region.Region;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RegionMapper extends BaseMapper<Region> {
}
