package com.robin.basis.vo;

import com.robin.basis.model.system.SysOrg;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.List;

@Data
public class SysOrgVO implements Serializable {
    private Long id;
    private String orgName;
    private String orgCode;
    private String status;
    private String orgAbbr;
    private Long pid;
    List<SysOrgVO> children;
    public static SysOrgVO fromVO(SysOrg sysOrg){
        SysOrgVO vo=new SysOrgVO();
        BeanUtils.copyProperties(sysOrg,vo);
        return vo;
    }

}
