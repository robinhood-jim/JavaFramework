package com.robin.biz.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.robin.basis.model.AbstractMybatisModel;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_biz_order")
public class Order extends AbstractMybatisModel {
    private Long id;
    private Long memberId;
    private Long merchatId;
    //总价
    private BigDecimal totalAmount;

    //"实付款")
    private BigDecimal payAmount;

    //"抵用券")
    private BigDecimal couponAmount;

    //"最终销售金额")
    private BigDecimal finalSalesAmount;

    //"备注")
    private String remark;
    private Integer payType;
    //对账标记
    private Short checking;

    //支付时间")
    private LocalDateTime paymentTime;

    //"完成时间")
    private LocalDateTime completionTime;
}
