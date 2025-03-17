package com.robin.basis.vo;

import com.robin.basis.model.region.Region;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;

@Data
public class RegionVO implements Serializable {
    private String code;
    private String name;
    private String pCode;
    private Short level;
    public static RegionVO fromVO(Region region){
        RegionVO regionVO=new RegionVO();
        BeanUtils.copyProperties(region,regionVO);
        return regionVO;
    }
}
