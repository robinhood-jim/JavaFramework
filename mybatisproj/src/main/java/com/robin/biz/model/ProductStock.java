package com.robin.biz.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.robin.basis.model.AbstractMybatisModel;
import lombok.Data;

@Data
@TableName("t_biz_product_stock")
public class ProductStock extends AbstractMybatisModel {
    private Long id;
    private Long productId;
    private Long quantity;

}
