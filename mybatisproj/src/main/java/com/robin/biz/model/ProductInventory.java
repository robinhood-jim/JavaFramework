package com.robin.biz.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.robin.basis.model.AbstractMybatisModel;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("t_biz_product_inventory")
public class ProductInventory extends AbstractMybatisModel {
    private Long id;
    private String checkTag;
    private Long productId;
    private Long salesQuantity;
    private Long stockQuantity;
    private BigDecimal salesReceivable;
    private BigDecimal salesRealReceive;

}
