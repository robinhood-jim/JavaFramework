package com.robin.basis.model.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.robin.basis.model.AbstractMybatisModel;
import lombok.Data;

@TableName("t_sys_params")
@Data
public class SysParams extends AbstractMybatisModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String paramName;
    private String paramValue;

}
