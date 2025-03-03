package com.robin.basis.model.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.robin.basis.model.AbstractMybatisModel;
import com.robin.core.base.model.AbstractBaseModel;
import lombok.Data;

@TableName("t_sys_codeset")
@Data
public class SysCodeSet extends AbstractMybatisModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String enName;
    private String cnName;
    private String csStatus;

}
