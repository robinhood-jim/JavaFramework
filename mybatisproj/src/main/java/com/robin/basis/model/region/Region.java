package com.robin.basis.model.region;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.robin.core.base.model.BaseObject;
import lombok.Data;

@TableName("t_region")
@Data
public class Region extends BaseObject {
    @TableId(type = IdType.ASSIGN_ID)
    private String code;
    private String name;
    private String pCode;
    private Short level;


}
