package com.dili.ia.domain;

import com.alibaba.fastjson.annotation.JSONField;
import com.dili.ss.domain.BaseDomain;
import com.dili.ss.metadata.FieldEditor;
import com.dili.ss.metadata.annotation.EditMode;
import com.dili.ss.metadata.annotation.FieldDef;

import java.time.LocalDateTime;
import java.util.Date;
import javax.persistence.*;

/**
 * 由MyBatis Generator工具自动生成
 * 租赁收费项目
 * This file was generated on 2020-05-29 16:13:04.
 */
@Table(name = "`business_charge_item`")
public class BusinessChargeItem extends BaseDomain {
    /**
     * id
     */
    @Id
    @Column(name = "`id`")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 业务ID
     */
    @Column(name = "`business_id`")
    private Long businessId;

    /**
     * 业务code
     */
    @Column(name = "`business_code`")
    private String businessCode;

    /**
     * 业务类型
     */
    @Column(name = "`biz_type`")
    private Integer bizType;

    /**
     * 收费项ID
     */
    @Column(name = "`charge_item_id`")
    private Long chargeItemId;

    /**
     * 收费项名称
     */
    @Column(name = "`charge_item_name`")
    private String chargeItemName;

    /**
     * 金额
     */
    @Column(name = "`amount`")
    private Long amount;


    //创建时间
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "`create_time`")
    private LocalDateTime createTime;

    //修改时间
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "`modify_time`")
    private LocalDateTime modifyTime;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    public String getBusinessCode() {
        return businessCode;
    }

    public void setBusinessCode(String businessCode) {
        this.businessCode = businessCode;
    }

    public Integer getBizType() {
        return bizType;
    }

    public void setBizType(Integer bizType) {
        this.bizType = bizType;
    }

    public Long getChargeItemId() {
        return chargeItemId;
    }

    public void setChargeItemId(Long chargeItemId) {
        this.chargeItemId = chargeItemId;
    }

    public String getChargeItemName() {
        return chargeItemName;
    }

    public void setChargeItemName(String chargeItemName) {
        this.chargeItemName = chargeItemName;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(LocalDateTime modifyTime) {
        this.modifyTime = modifyTime;
    }
}