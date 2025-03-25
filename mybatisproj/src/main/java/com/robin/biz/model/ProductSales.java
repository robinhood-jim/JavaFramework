package com.robin.biz.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.robin.basis.model.AbstractMybatisModel;
import lombok.Data;

@Data
@TableName("t_product_sales")
public class ProductSales  extends AbstractMybatisModel {
    private Long id;
    private Long productId;
    private Long orderId;
    private Integer quantity;

}
