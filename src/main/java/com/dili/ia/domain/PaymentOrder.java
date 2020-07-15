package com.dili.ia.domain;

import com.dili.ss.dto.IBaseDomain;
import com.dili.ss.metadata.FieldEditor;
import com.dili.ss.metadata.annotation.EditMode;
import com.dili.ss.metadata.annotation.FieldDef;
import tk.mybatis.mapper.annotation.Version;

import java.util.Date;
import javax.persistence.*;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * 由MyBatis Generator工具自动生成
 * 缴费单
 * This file was generated on 2020-02-28 21:11:27.
 */
@Table(name = "`payment_order`")
public interface PaymentOrder extends IBaseDomain {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`id`")
    @FieldDef(label="id")
    @EditMode(editor = FieldEditor.Number, required = true)
    Long getId();

    void setId(Long id);

    @Column(name = "`code`")
    @FieldDef(label="编号", maxLength = 30)
    @EditMode(editor = FieldEditor.Text, required = false)
    String getCode();

    void setCode(String code);

    @Column(name = "`create_time`")
    @FieldDef(label="创建时间")
    @EditMode(editor = FieldEditor.Datetime, required = false)
    Date getCreateTime();

    void setCreateTime(Date createTime);

    @Column(name = "`modify_time`")
    @FieldDef(label="修改时间")
    @EditMode(editor = FieldEditor.Datetime, required = false)
    Date getModifyTime();

    void setModifyTime(Date modifyTime);

    @Column(name = "`customer_id`")
    @FieldDef(label="客户ID")
    @EditMode(editor = FieldEditor.Number, required = false)
    Long getCustomerId();

    void setCustomerId(Long customerId);

    @Column(name = "`customer_name`")
    @FieldDef(label="客户名称")
    @EditMode(editor = FieldEditor.Text, required = false)
    String getCustomerName();

    void setCustomerName(String customerName);

    @Column(name = "`business_id`")
    @FieldDef(label="业务单ID")
    @EditMode(editor = FieldEditor.Number, required = false)
    Long getBusinessId();

    void setBusinessId(Long businessId);

    @Column(name = "`business_code`")
    @FieldDef(label="业务单编号", maxLength = 30)
    @EditMode(editor = FieldEditor.Text, required = false)
    String getBusinessCode();

    void setBusinessCode(String businessCode);

    @Column(name = "`biz_type`")
    @FieldDef(label="业务类型")
    @EditMode(editor = FieldEditor.Number, required = false)
    Integer getBizType();

    void setBizType(Integer bizType);

    @Column(name = "`state`")
    @FieldDef(label="状态")
    @EditMode(editor = FieldEditor.Number, required = false)
    Integer getState();

    void setState(Integer state);

    @Column(name = "`is_settle`")
    @FieldDef(label="是否结清")
    @EditMode(editor = FieldEditor.Number, required = false)
    Integer getIsSettle();

    void setIsSettle(Integer isSettle);

    @Column(name = "`amount`")
    @FieldDef(label="金额")
    @EditMode(editor = FieldEditor.Number, required = false)
    Long getAmount();

    void setAmount(Long amount);

    @Column(name = "`creator_id`")
    @FieldDef(label="创建人ID")
    @EditMode(editor = FieldEditor.Number, required = false)
    Long getCreatorId();

    void setCreatorId(Long creatorId);

    @Column(name = "`creator`")
    @FieldDef(label="创建人", maxLength = 20)
    @EditMode(editor = FieldEditor.Text, required = false)
    String getCreator();

    void setCreator(String creator);

    @Column(name = "`payed_time`")
    @FieldDef(label="支付时间")
    @EditMode(editor = FieldEditor.Datetime, required = false)
    Date getPayedTime();

    void setPayedTime(Date payedTime);

    @Column(name = "`settlement_code`")
    @FieldDef(label="结算编号", maxLength = 30)
    @EditMode(editor = FieldEditor.Text, required = false)
    String getSettlementCode();

    void setSettlementCode(String settlementCode);

    @Column(name = "`settlement_way`")
    @FieldDef(label="结算方式（冗余 来自结算）")
    @EditMode(editor = FieldEditor.Number, required = false)
    Integer getSettlementWay();

    void setSettlementWay(Integer settlementWay);

    @Column(name = "`settlement_operator`")
    @FieldDef(label="结算编号")
    @EditMode(editor = FieldEditor.Text, required = false)
    String getSettlementOperator();

    void setSettlementOperator(String settlementOperator);

    @Column(name = "`market_id`")
    @FieldDef(label="marketId")
    @EditMode(editor = FieldEditor.Number, required = false)
    Long getMarketId();

    void setMarketId(Long marketId);

    @Column(name = "`market_code`")
    @FieldDef(label="市场Code", maxLength = 20)
    @EditMode(editor = FieldEditor.Text, required = false)
    String getMarketCode();

    void setMarketCode(String marketCode);

    @Column(name = "`version`")
    @FieldDef(label="乐观锁，版本号")
    @EditMode(editor = FieldEditor.Number, required = false)
    @Version
    Integer getVersion();

    Integer setVersion(Integer version);
}