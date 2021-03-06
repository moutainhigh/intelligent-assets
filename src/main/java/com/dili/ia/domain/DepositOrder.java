package com.dili.ia.domain;

import com.dili.ss.domain.BaseDomain;
import com.dili.ss.metadata.FieldEditor;
import com.dili.ss.metadata.annotation.EditMode;
import com.dili.ss.metadata.annotation.FieldDef;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import tk.mybatis.mapper.annotation.Version;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 由MyBatis Generator工具自动生成
 * 
 * This file was generated on 2020-06-29 15:11:22.
 */
@Table(name = "`deposit_order`")
public class DepositOrder extends BaseDomain {
    @Id
    @Column(name = "`id`")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "`create_time`")
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "`modify_time`")
    private LocalDateTime modifyTime;

    /**
     * 业务编号
     */
    @Column(name = "`code`")
    private String code;

    /**
     * 客户ID
     */
    @Column(name = "`customer_id`")
    private Long customerId;

    /**
     * 客户名称
     */
    @Column(name = "`customer_name`")
    private String customerName;

    /**
     * 客户证件号
     */
    @Column(name = "`certificate_number`")
    private String certificateNumber;

    /**
     * 客户电话
     */
    @Column(name = "`customer_cellphone`")
    private String customerCellphone;

    /**
     * 业务所属部门ID
     */
    @Column(name = "`department_id`")
    private Long departmentId;

    /**
     * 业务所属部门名称
     */
    @Column(name = "`department_name`")
    private String departmentName;

    /**
     * 保证金类型，来源数据字典
     */
    @Column(name = "`type_code`")
    private String typeCode;

    /**
     * 保证金类型名称
     */
    @Column(name = "`type_name`")
    private String typeName;

    /**
     * 资产类型
     */
    @Column(name = "`assets_type`")
    private Integer assetsType;

    /**
     * 资产ID
     */
    @Column(name = "`assets_id`")
    private Long assetsId;

    /**
     * 资产名称
     */
    @Column(name = "`assets_name`")
    private String assetsName;

    /**
     * 保证金金额= 已付金额 + 待付金额
     */
    @Column(name = "`amount`")
    private Long amount;

    /**
     * 已付金额
     */
    @Column(name = "`paid_amount`")
    private Long paidAmount;

    /**
     * 待付金额
     */
    @Column(name = "`wait_amount`")
    private Long waitAmount;

    /**
     * 退款金额，用于多次退款记录
     */
    @Column(name = "`refund_amount`")
    private Long refundAmount;

    /**
     * 备注信息
     */
    @Column(name = "`notes`")
    private String notes;

    /**
     * 是否关联订单1，是，0否
     */
    @Column(name = "`is_related`")
    private Integer isRelated;

    /**
     * 关联订单ID
     */
    @Column(name = "`business_id`")
    private Long businessId;

    /**
     * 关联订单业务类型
     */
    @Column(name = "`biz_type`")
    private String bizType;

    /**
     * （1：已创建 2：已取消 3：已提交 4：已交费5：已退款）
     */
    @Column(name = "`state`")
    private Integer state;

    /**
     * 支付状态（1：未交清 2：已交清）
     */
    @Column(name = "`pay_state`")
    private Integer payState;

    /**
     * 1:待申请 2：退款中 3：已退款
     */
    @Column(name = "`refund_state`")
    private Integer refundState;

    /**
     * 审批状态
     */
    @Column(name = "`approval_state`")
    private Integer approvalState;

    /**
     *  流程实例ID
     */
    @Column(name = "`process_instance_id`")
    private Long processInstanceId;

    /**
     * 创建操作员ID
     */
    @Column(name = "`creator_id`")
    private Long creatorId;

    /**
     * 创建人名称
     */
    @Column(name = "`creator`")
    private String creator;

    /**
     * 提交人ID
     */
    @Column(name = "`submitter_id`")
    private Long submitterId;

    /**
     * 提交人名称
     */
    @Column(name = "`submitter`")
    private String submitter;

    /**
     * 提交时间
     */
    @Column(name = "`submit_time`")
    private LocalDateTime submitTime;

    /**
     * 撤回人ID
     */
    @Column(name = "`withdraw_operator_id`")
    private Long withdrawOperatorId;

    /**
     * 撤回人名称
     */
    @Column(name = "`withdraw_operator`")
    private String withdrawOperator;

    /**
     * 取消人ID
     */
    @Column(name = "`canceler_id`")
    private Long cancelerId;

    /**
     * 取消人名称
     */
    @Column(name = "`canceler`")
    private String canceler;

    /**
     * 市场Id
     */
    @Column(name = "`market_id`")
    private Long marketId;

    /**
     * 市场CODE
     */
    @Column(name = "`market_code`")
    private String marketCode;

    /**
     * 版本控制,乐观锁
     */
    @Version
    @Column(name = "`version`")
    private Long version;

    /**
     * 是否老数据导入订单，1是，0否
     */
    @Column(name = "`is_import`")
    private Integer isImport;

    /**
     * @return id
     */
    @FieldDef(label="id")
    @EditMode(editor = FieldEditor.Number, required = true)
    public Long getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取创建时间
     *
     * @return create_time - 创建时间
     */
    @FieldDef(label="创建时间")
    @EditMode(editor = FieldEditor.Datetime, required = false)
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    /**
     * 设置创建时间
     *
     * @param createTime 创建时间
     */
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    /**
     * 获取修改时间
     *
     * @return modify_time - 修改时间
     */
    @FieldDef(label="修改时间")
    @EditMode(editor = FieldEditor.Datetime, required = false)
    public LocalDateTime getModifyTime() {
        return modifyTime;
    }

    /**
     * 设置修改时间
     *
     * @param modifyTime 修改时间
     */
    public void setModifyTime(LocalDateTime modifyTime) {
        this.modifyTime = modifyTime;
    }

    /**
     * 获取业务编号
     *
     * @return code - 业务编号
     */
    @FieldDef(label="业务编号", maxLength = 30)
    @EditMode(editor = FieldEditor.Text, required = false)
    public String getCode() {
        return code;
    }

    /**
     * 设置业务编号
     *
     * @param code 业务编号
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * 获取客户ID
     *
     * @return customer_id - 客户ID
     */
    @FieldDef(label="客户ID")
    @EditMode(editor = FieldEditor.Number, required = false)
    public Long getCustomerId() {
        return customerId;
    }

    /**
     * 设置客户ID
     *
     * @param customerId 客户ID
     */
    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    /**
     * 获取客户名称
     *
     * @return customer_name - 客户名称
     */
    @FieldDef(label="客户名称", maxLength = 40)
    @EditMode(editor = FieldEditor.Text, required = false)
    public String getCustomerName() {
        return customerName;
    }

    /**
     * 设置客户名称
     *
     * @param customerName 客户名称
     */
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    /**
     * 获取客户证件号
     *
     * @return certificate_number - 客户证件号
     */
    @FieldDef(label="客户证件号", maxLength = 40)
    @EditMode(editor = FieldEditor.Text, required = false)
    public String getCertificateNumber() {
        return certificateNumber;
    }

    /**
     * 设置客户证件号
     *
     * @param certificateNumber 客户证件号
     */
    public void setCertificateNumber(String certificateNumber) {
        this.certificateNumber = certificateNumber;
    }

    /**
     * 获取客户电话
     *
     * @return customer_cellphone - 客户电话
     */
    @FieldDef(label="客户电话", maxLength = 20)
    @EditMode(editor = FieldEditor.Text, required = false)
    public String getCustomerCellphone() {
        return customerCellphone;
    }

    /**
     * 设置客户电话
     *
     * @param customerCellphone 客户电话
     */
    public void setCustomerCellphone(String customerCellphone) {
        this.customerCellphone = customerCellphone;
    }

    /**
     * 获取业务所属部门ID
     *
     * @return department_id - 业务所属部门ID
     */
    @FieldDef(label="业务所属部门ID")
    @EditMode(editor = FieldEditor.Number, required = false)
    public Long getDepartmentId() {
        return departmentId;
    }

    /**
     * 设置业务所属部门ID
     *
     * @param departmentId 业务所属部门ID
     */
    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    /**
     * 获取业务所属部门名称
     *
     * @return department_name - 业务所属部门名称
     */
    @FieldDef(label="业务所属部门名称", maxLength = 50)
    @EditMode(editor = FieldEditor.Text, required = false)
    public String getDepartmentName() {
        return departmentName;
    }

    /**
     * 设置业务所属部门名称
     *
     * @param departmentName 业务所属部门名称
     */
    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    /**
     * 获取保证金类型，来源数据字典
     *
     * @return type_code - 保证金类型，来源数据字典
     */
    @FieldDef(label="保证金类型，来源数据字典", maxLength = 50)
    @EditMode(editor = FieldEditor.Text, required = false)
    public String getTypeCode() {
        return typeCode;
    }

    /**
     * 设置保证金类型，来源数据字典
     *
     * @param typeCode 保证金类型，来源数据字典
     */
    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    /**
     * 获取保证金类型名称
     *
     * @return type_name - 保证金类型名称
     */
    @FieldDef(label="保证金类型名称", maxLength = 120)
    @EditMode(editor = FieldEditor.Text, required = false)
    public String getTypeName() {
        return typeName;
    }

    /**
     * 设置保证金类型名称
     *
     * @param typeName 保证金类型名称
     */
    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    /**
     * 获取资产类型
     *
     * @return assets_type - 资产类型
     */
    @FieldDef(label="资产类型", maxLength = 50)
    @EditMode(editor = FieldEditor.Number, required = false)
    public Integer getAssetsType() {
        return assetsType;
    }

    /**
     * 设置资产类型
     *
     * @param assetsType 资产类型
     */
    public void setAssetsType(Integer assetsType) {
        this.assetsType = assetsType;
    }

    /**
     * 获取资产ID
     *
     * @return assets_id - 资产ID
     */
    @FieldDef(label="资产ID")
    @EditMode(editor = FieldEditor.Number, required = false)
    public Long getAssetsId() {
        return assetsId;
    }

    /**
     * 设置资产ID
     *
     * @param assetsId 资产ID
     */
    public void setAssetsId(Long assetsId) {
        this.assetsId = assetsId;
    }

    /**
     * 获取资产名称
     *
     * @return assets_name - 资产名称
     */
    @FieldDef(label="资产名称", maxLength = 200)
    @EditMode(editor = FieldEditor.Text, required = false)
    public String getAssetsName() {
        return assetsName;
    }

    /**
     * 设置资产名称
     *
     * @param assetsName 资产名称
     */
    public void setAssetsName(String assetsName) {
        this.assetsName = assetsName;
    }

    /**
     * 获取保证金金额= 已付金额 + 待付金额
     *
     * @return amount - 保证金金额= 已付金额 + 待付金额
     */
    @FieldDef(label="保证金金额= 已付金额 + 待付金额")
    @EditMode(editor = FieldEditor.Number, required = false)
    public Long getAmount() {
        return amount;
    }

    /**
     * 设置保证金金额= 已付金额 + 待付金额
     *
     * @param amount 保证金金额= 已付金额 + 待付金额
     */
    public void setAmount(Long amount) {
        this.amount = amount;
    }

    /**
     * 获取已付金额
     *
     * @return paid_amount - 已付金额
     */
    @FieldDef(label="已付金额")
    @EditMode(editor = FieldEditor.Number, required = false)
    public Long getPaidAmount() {
        return paidAmount;
    }

    /**
     * 设置已付金额
     *
     * @param paidAmount 已付金额
     */
    public void setPaidAmount(Long paidAmount) {
        this.paidAmount = paidAmount;
    }

    /**
     * 获取待付金额
     *
     * @return wait_amount - 待付金额
     */
    @FieldDef(label="待付金额")
    @EditMode(editor = FieldEditor.Number, required = false)
    public Long getWaitAmount() {
        return waitAmount;
    }

    /**
     * 设置待付金额
     *
     * @param waitAmount 待付金额
     */
    public void setWaitAmount(Long waitAmount) {
        this.waitAmount = waitAmount;
    }

    /**
     * 获取退款金额，用于多次退款记录
     *
     * @return refund_amount - 退款金额，用于多次退款记录
     */
    @FieldDef(label="退款金额，用于多次退款记录")
    @EditMode(editor = FieldEditor.Number, required = false)
    public Long getRefundAmount() {
        return refundAmount;
    }

    /**
     * 设置退款金额，用于多次退款记录
     *
     * @param refundAmount 退款金额，用于多次退款记录
     */
    public void setRefundAmount(Long refundAmount) {
        this.refundAmount = refundAmount;
    }

    /**
     * 获取备注信息
     *
     * @return notes - 备注信息
     */
    @FieldDef(label="备注信息", maxLength = 250)
    @EditMode(editor = FieldEditor.Text, required = false)
    public String getNotes() {
        return notes;
    }

    /**
     * 设置备注信息
     *
     * @param notes 备注信息
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * 获取是否关联订单1，是，0否
     *
     * @return is_related - 是否关联订单1，是，0否
     */
    @FieldDef(label="是否关联订单1，是，0否")
    @EditMode(editor = FieldEditor.Number, required = false)
    public Integer getIsRelated() {
        return isRelated;
    }

    /**
     * 设置是否关联订单1，是，0否
     *
     * @param isRelated 是否关联订单1，是，0否
     */
    public void setIsRelated(Integer isRelated) {
        this.isRelated = isRelated;
    }

    /**
     * 获取关联订单ID
     *
     * @return business_id - 关联订单ID
     */
    @FieldDef(label="关联订单ID")
    @EditMode(editor = FieldEditor.Number, required = false)
    public Long getBusinessId() {
        return businessId;
    }

    /**
     * 设置关联订单ID
     *
     * @param businessId 关联订单ID
     */
    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    /**
     * 获取关联订单业务类型
     *
     * @return biz_type - 关联订单业务类型
     */
    @FieldDef(label="关联订单业务类型")
    @EditMode(editor = FieldEditor.Number, required = false)
    public String getBizType() {
        return bizType;
    }

    /**
     * 设置关联订单业务类型
     *
     * @param bizType 关联订单业务类型
     */
    public void setBizType(String bizType) {
        this.bizType = bizType;
    }

    /**
     * 获取（1：已创建 2：已取消 3：已提交 4：已交费5：已退款）
     *
     * @return state - （1：已创建 2：已取消 3：已提交 4：已交费5：已退款）
     */
    @FieldDef(label="（1：已创建 2：已取消 3：已提交 4：已交费5：已退款）")
    @EditMode(editor = FieldEditor.Number, required = false)
    public Integer getState() {
        return state;
    }

    /**
     * 设置（1：已创建 2：已取消 3：已提交 4：已交费5：已退款）
     *
     * @param state （1：已创建 2：已取消 3：已提交 4：已交费5：已退款）
     */
    public void setState(Integer state) {
        this.state = state;
    }

    /**
     * 获取支付状态（1：未交清 2：已交清）
     *
     * @return pay_state - 支付状态（1：未交清 2：已交清）
     */
    @FieldDef(label="支付状态（1：未交清 2：已交清）")
    @EditMode(editor = FieldEditor.Number, required = false)
    public Integer getPayState() {
        return payState;
    }

    /**
     * 设置支付状态（1：未交清 2：已交清）
     *
     * @param payState 支付状态（1：未交清 2：已交清）
     */
    public void setPayState(Integer payState) {
        this.payState = payState;
    }

    /**
     * 获取1:待申请 2：退款中 3：已退款
     *
     * @return refund_state - 1:待申请 2：退款中 3：已退款
     */
    @FieldDef(label="1:待申请 2：退款中 3：已退款")
    @EditMode(editor = FieldEditor.Number, required = true)
    public Integer getRefundState() {
        return refundState;
    }

    /**
     * 设置1:待申请 2：退款中 3：已退款
     *
     * @param refundState 1:待申请 2：退款中 3：已退款
     */
    public void setRefundState(Integer refundState) {
        this.refundState = refundState;
    }

    /**
     * 获取审批状态
     *
     * @return approval_state - 审批状态
     */
    @FieldDef(label="审批状态")
    @EditMode(editor = FieldEditor.Number, required = false)
    public Integer getApprovalState() {
        return approvalState;
    }

    /**
     * 设置审批状态
     *
     * @param approvalState 审批状态
     */
    public void setApprovalState(Integer approvalState) {
        this.approvalState = approvalState;
    }

    /**
     * 获取 流程实例ID
     *
     * @return process_instance_id -  流程实例ID
     */
    @FieldDef(label=" 流程实例ID")
    @EditMode(editor = FieldEditor.Number, required = false)
    public Long getProcessInstanceId() {
        return processInstanceId;
    }

    /**
     * 设置 流程实例ID
     *
     * @param processInstanceId  流程实例ID
     */
    public void setProcessInstanceId(Long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    /**
     * 获取创建操作员ID
     *
     * @return creator_id - 创建操作员ID
     */
    @FieldDef(label="创建操作员ID")
    @EditMode(editor = FieldEditor.Number, required = false)
    public Long getCreatorId() {
        return creatorId;
    }

    /**
     * 设置创建操作员ID
     *
     * @param creatorId 创建操作员ID
     */
    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    /**
     * 获取创建人名称
     *
     * @return creator - 创建人名称
     */
    @FieldDef(label="创建人名称", maxLength = 20)
    @EditMode(editor = FieldEditor.Text, required = false)
    public String getCreator() {
        return creator;
    }

    /**
     * 设置创建人名称
     *
     * @param creator 创建人名称
     */
    public void setCreator(String creator) {
        this.creator = creator;
    }

    /**
     * 获取提交人ID
     *
     * @return submitter_id - 提交人ID
     */
    @FieldDef(label="提交人ID")
    @EditMode(editor = FieldEditor.Number, required = false)
    public Long getSubmitterId() {
        return submitterId;
    }

    /**
     * 设置提交人ID
     *
     * @param submitterId 提交人ID
     */
    public void setSubmitterId(Long submitterId) {
        this.submitterId = submitterId;
    }

    /**
     * 获取提交人名称
     *
     * @return submitter - 提交人名称
     */
    @FieldDef(label="提交人名称", maxLength = 20)
    @EditMode(editor = FieldEditor.Text, required = false)
    public String getSubmitter() {
        return submitter;
    }

    /**
     * 设置提交人名称
     *
     * @param submitter 提交人名称
     */
    public void setSubmitter(String submitter) {
        this.submitter = submitter;
    }

    /**
     * 获取提交时间
     *
     * @return submit_time - 提交时间
     */
    @FieldDef(label="提交时间")
    @EditMode(editor = FieldEditor.Datetime, required = false)
    public LocalDateTime getSubmitTime() {
        return submitTime;
    }

    /**
     * 设置提交时间
     *
     * @param submitTime 提交时间
     */
    public void setSubmitTime(LocalDateTime submitTime) {
        this.submitTime = submitTime;
    }

    /**
     * 获取撤回人ID
     *
     * @return withdraw_operator_id - 撤回人ID
     */
    @FieldDef(label="撤回人ID")
    @EditMode(editor = FieldEditor.Number, required = false)
    public Long getWithdrawOperatorId() {
        return withdrawOperatorId;
    }

    /**
     * 设置撤回人ID
     *
     * @param withdrawOperatorId 撤回人ID
     */
    public void setWithdrawOperatorId(Long withdrawOperatorId) {
        this.withdrawOperatorId = withdrawOperatorId;
    }

    /**
     * 获取撤回人名称
     *
     * @return withdraw_operator - 撤回人名称
     */
    @FieldDef(label="撤回人名称", maxLength = 20)
    @EditMode(editor = FieldEditor.Text, required = false)
    public String getWithdrawOperator() {
        return withdrawOperator;
    }

    /**
     * 设置撤回人名称
     *
     * @param withdrawOperator 撤回人名称
     */
    public void setWithdrawOperator(String withdrawOperator) {
        this.withdrawOperator = withdrawOperator;
    }

    /**
     * 获取取消人ID
     *
     * @return canceler_id - 取消人ID
     */
    @FieldDef(label="取消人ID")
    @EditMode(editor = FieldEditor.Number, required = false)
    public Long getCancelerId() {
        return cancelerId;
    }

    /**
     * 设置取消人ID
     *
     * @param cancelerId 取消人ID
     */
    public void setCancelerId(Long cancelerId) {
        this.cancelerId = cancelerId;
    }

    /**
     * 获取取消人名称
     *
     * @return canceler - 取消人名称
     */
    @FieldDef(label="取消人名称", maxLength = 20)
    @EditMode(editor = FieldEditor.Text, required = false)
    public String getCanceler() {
        return canceler;
    }

    /**
     * 设置取消人名称
     *
     * @param canceler 取消人名称
     */
    public void setCanceler(String canceler) {
        this.canceler = canceler;
    }

    /**
     * 获取市场Id
     *
     * @return market_id - 市场Id
     */
    @FieldDef(label="市场Id")
    @EditMode(editor = FieldEditor.Number, required = false)
    public Long getMarketId() {
        return marketId;
    }

    /**
     * 设置市场Id
     *
     * @param marketId 市场Id
     */
    public void setMarketId(Long marketId) {
        this.marketId = marketId;
    }

    /**
     * 获取市场CODE
     *
     * @return market_code - 市场CODE
     */
    @FieldDef(label="市场CODE", maxLength = 20)
    @EditMode(editor = FieldEditor.Text, required = false)
    public String getMarketCode() {
        return marketCode;
    }

    /**
     * 设置市场CODE
     *
     * @param marketCode 市场CODE
     */
    public void setMarketCode(String marketCode) {
        this.marketCode = marketCode;
    }

    /**
     * 获取版本控制,乐观锁
     *
     * @return version - 版本控制,乐观锁
     */
    @FieldDef(label="版本控制,乐观锁")
    @EditMode(editor = FieldEditor.Number, required = false)
    public Long getVersion() {
        return version;
    }

    /**
     * 设置版本控制,乐观锁
     *
     * @param version 版本控制,乐观锁
     */
    public void setVersion(Long version) {
        this.version = version;
    }

    /**
     * 获取是否老数据导入订单，1是，0否
     *
     * @return is_import - 是否老数据导入订单，1是，0否
     */
    @FieldDef(label="是否老数据导入订单，1是，0否")
    @EditMode(editor = FieldEditor.Number, required = false)
    public Integer getIsImport() {
        return isImport;
    }

    /**
     * 设置是否老数据导入订单，1是，0否
     *
     * @param isImport 是否老数据导入订单，1是，0否
     */
    public void setIsImport(Integer isImport) {
        this.isImport = isImport;
    }
}