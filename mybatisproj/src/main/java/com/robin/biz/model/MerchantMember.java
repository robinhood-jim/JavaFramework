package com.robin.biz.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.robin.basis.model.AbstractMybatisModel;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_biz_merchant_member")
public class MerchantMember extends AbstractMybatisModel {
    private Long id;
    private String code;
    private Long custId;

    private String type;
    private Integer level;
    private Long scores;

    //抵用券
    private BigDecimal coupon;
    //总消费金额
    private BigDecimal consumeAmount;
    //消费抵用券
    private BigDecimal consumeCoupon;
    //消费次数
    private Integer consumeTimes;
    //取消次数
    private Integer cancelTimes;

    private String remark;
    //开卡时间
    private LocalDateTime openTime;

}
