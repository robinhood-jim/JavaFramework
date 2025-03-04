package com.robin.basis.model.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.robin.basis.model.AbstractMybatisModel;
import lombok.Data;


@TableName("t_sys_code")
@Data
public class SysCode extends AbstractMybatisModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long csId;
    private String itemName;
    private String itemValue;
    private Integer orderNo;

}
