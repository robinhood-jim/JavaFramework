package com.robin.biz.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
