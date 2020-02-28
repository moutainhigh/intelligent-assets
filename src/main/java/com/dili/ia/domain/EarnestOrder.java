package com.dili.ia.domain;

import com.dili.ss.dto.IBaseDomain;
import com.dili.ss.metadata.FieldEditor;
import com.dili.ss.metadata.annotation.EditMode;
import com.dili.ss.metadata.annotation.FieldDef;
import java.util.Date;
import javax.persistence.*;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * 由MyBatis Generator工具自动生成
 * 定金的业务单
 * This file was generated on 2020-02-28 18:25:12.
 */
@Table(name = "`earnest_order`")
public interface EarnestOrder extends IBaseDomain {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`id`")
    @FieldDef(label="id")
    @EditMode(editor = FieldEditor.Number, required = true)
    Long getId();

    void setId(Long id);

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
    @FieldDef(label="客户名称", maxLength = 20)
    @EditMode(editor = FieldEditor.Text, required = false)
    String getCustomerName();

    void setCustomerName(String customerName);

    @Column(name = "`customer_cellphone`")
    @FieldDef(label="客户电话", maxLength = 20)
    @EditMode(editor = FieldEditor.Text, required = false)
    String getCustomerCellphone();

    void setCustomerCellphone(String customerCellphone);

    @Column(name = "`certificate_number`")
    @FieldDef(label="客户证件号码", maxLength = 40)
    @EditMode(editor = FieldEditor.Text, required = false)
    String getCertificateNumber();

    void setCertificateNumber(String certificateNumber);

    @Column(name = "`start_time`")
    @FieldDef(label="开始时间")
    @EditMode(editor = FieldEditor.Datetime, required = false)
    Date getStartTime();

    void setStartTime(Date startTime);

    @Column(name = "`end_time`")
    @FieldDef(label="截止时间")
    @EditMode(editor = FieldEditor.Datetime, required = false)
    Date getEndTime();

    void setEndTime(Date endTime);

    @Column(name = "`department_id`")
    @FieldDef(label="业务单业务部门ID")
    @EditMode(editor = FieldEditor.Number, required = false)
    Long getDepartmentId();

    void setDepartmentId(Long departmentId);

    @Column(name = "`department_name`")
    @FieldDef(label="业务单业务部门名称", maxLength = 50)
    @EditMode(editor = FieldEditor.Text, required = false)
    String getDepartmentName();

    void setDepartmentName(String departmentName);

    @Column(name = "`state`")
    @FieldDef(label="状态：1-已创建，2-已取消，3-已提交，4-已缴费")
    @EditMode(editor = FieldEditor.Number, required = false)
    Integer getState();

    void setState(Integer state);

    @Column(name = "`assets_type`")
    @FieldDef(label="资产类型，包含摊位，冷库，公寓等")
    @EditMode(editor = FieldEditor.Number, required = false)
    Integer getAssetsType();

    void setAssetsType(Integer assetsType);

    @Column(name = "`amount`")
    @FieldDef(label="金额")
    @EditMode(editor = FieldEditor.Number, required = false)
    Long getAmount();

    void setAmount(Long amount);

    @Column(name = "`code`")
    @FieldDef(label="定金业务单编号", maxLength = 50)
    @EditMode(editor = FieldEditor.Text, required = false)
    String getCode();

    void setCode(String code);

    @Column(name = "`creator_id`")
    @FieldDef(label="创建操作员ID")
    @EditMode(editor = FieldEditor.Number, required = false)
    Long getCreatorId();

    void setCreatorId(Long creatorId);

    @Column(name = "`creator`")
    @FieldDef(label="创建操作员名称", maxLength = 20)
    @EditMode(editor = FieldEditor.Text, required = false)
    String getCreator();

    void setCreator(String creator);

    @Column(name = "`submitter_id`")
    @FieldDef(label="提交人ID")
    @EditMode(editor = FieldEditor.Number, required = false)
    Long getSubmitterId();

    void setSubmitterId(Long submitterId);

    @Column(name = "`submitter`")
    @FieldDef(label="提交人名字", maxLength = 20)
    @EditMode(editor = FieldEditor.Text, required = false)
    String getSubmitter();

    void setSubmitter(String submitter);

    @Column(name = "`sub_date`")
    @FieldDef(label="提交时间")
    @EditMode(editor = FieldEditor.Datetime, required = false)
    Date getSubDate();

    void setSubDate(Date subDate);

    @Column(name = "`notes`")
    @FieldDef(label="备注", maxLength = 250)
    @EditMode(editor = FieldEditor.Text, required = false)
    String getNotes();

    void setNotes(String notes);

    @Column(name = "`withdraw_operator_id`")
    @FieldDef(label="撤回人ID")
    @EditMode(editor = FieldEditor.Number, required = false)
    Long getWithdrawOperatorId();

    void setWithdrawOperatorId(Long withdrawOperatorId);

    @Column(name = "`withdraw_operator`")
    @FieldDef(label="撤回人名字", maxLength = 20)
    @EditMode(editor = FieldEditor.Text, required = false)
    String getWithdrawOperator();

    void setWithdrawOperator(String withdrawOperator);

    @Column(name = "`canceler_id`")
    @FieldDef(label="取消人ID")
    @EditMode(editor = FieldEditor.Number, required = false)
    Long getCancelerId();

    void setCancelerId(Long cancelerId);

    @Column(name = "`market_id`")
    @FieldDef(label="市场Id")
    @EditMode(editor = FieldEditor.Number, required = false)
    Long getMarketId();

    void setMarketId(Long marketId);

    @Column(name = "`version`")
    @FieldDef(label="版本控制,乐观锁")
    @EditMode(editor = FieldEditor.Number, required = false)
    Long getVersion();

    void setVersion(Long version);

    @Column(name = "`canceler`")
    @FieldDef(label="取消人名字")
    @EditMode(editor = FieldEditor.Text, required = false)
    byte[] getCanceler();

    void setCanceler(byte[] canceler);
}