package com.robin.basis.service.system;

import com.robin.basis.dto.SysCodeSetDTO;
import com.robin.basis.model.system.SysCodeSet;
import com.robin.core.base.service.IMybatisBaseService;

import java.util.Map;

public interface ISysCodeSetService extends IMybatisBaseService<SysCodeSet,Long> {
    Map<String,Object> list(SysCodeSetDTO dto);
}
