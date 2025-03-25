package com.robin.biz.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.robin.basis.model.AbstractMybatisModel;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_biz_product_purchase")
public class ProductPurchase extends AbstractMybatisModel {
    private Long id;
    private Long productId;
    private Double price;
    private BigDecimal total;
    private LocalDateTime purchaseTm;
    private String operator;
    private Long receiver;
    private Short type;
}
