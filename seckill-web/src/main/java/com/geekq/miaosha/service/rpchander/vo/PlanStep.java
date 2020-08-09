package com.geekq.miaosha.service.rpchander.vo;

import lombok.Data;

import java.util.Date;

@Data
public class PlanStep {
    private Long id;

    private Date createTime;

    private Date updateTime;

    private Integer version;

    private Long orderId;

    private String status;

    private String type;

    private Integer priority;

    private Integer retryCount;

    private String remark;

}