package com.robin.biz.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.robin.basis.model.AbstractMybatisModel;
import lombok.Data;

@TableName("t_biz_products")
@Data
public class Products extends AbstractMybatisModel {
    private Long id;
    private String name;
    private String code;
    private Double price;
    private String type;
    private Long brandId;

}
