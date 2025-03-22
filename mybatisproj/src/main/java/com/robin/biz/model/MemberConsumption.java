package com.robin.biz.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@TableName("t_biz_member_consumption")
public class MemberConsumption  {
    private Long id;
    private Long memberId;
    private Long tenantId;
    private Long orderId;
    private Long scores;
    private LocalDateTime scoreExpireTm;
    private BigDecimal consumption;
    private LocalDateTime consumeTime;
    private Short cancelTag;



}
