package com.dili.ia.domain.dto.printDto;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * <B>Description</B>
 * 本软件源代码版权归农丰时代及其团队所有,未经许可不得任意复制与传播
 * <B>农丰时代科技有限公司</B>
 *
 * @author qinkelan
 * @createTime 2020-03-13 10:32
 */
public class DepositOrderPrintDto {
    //打印时间
    private String printTime;
    //开始日期
    private String startTime;
    //结束日期
    private String endTime;

    //补打标记
    private String reprint;
    //订单编号
    private String code;
    //业务类型
    private String bizType;
    //客户名称
    private String customerName;
    //客户电话
    private String customerCellphone;
    //备注
    private String notes;
    //合计总金额
    private String totalAmount;
    //付款金额
    private String amount;
    //待付金额
    private String waitAmount;
    //结算方式
    private String settlementWay;
    //结算员
    private String settlementOperator;
    //结算详情
    private String settleWayDetails;
    //提交人
    private String submitter;
    //保证金业务类型
    private String typeName;
    //资产类型
    private String assetsType;
    //资产编号
    private String assetsName;

    public String getReprint() {
        return reprint;
    }

    public void setReprint(String reprint) {
        this.reprint = reprint;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getBizType() {
        return bizType;
    }

    public void setBizType(String bizType) {
        this.bizType = bizType;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getAssetsType() {
        return assetsType;
    }

    public void setAssetsType(String assetsType) {
        this.assetsType = assetsType;
    }

    public String getAssetsName() {
        return assetsName;
    }

    public void setAssetsName(String assetsName) {
        this.assetsName = assetsName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerCellphone() {
        return customerCellphone;
    }

    public void setCustomerCellphone(String customerCellphone) {
        this.customerCellphone = customerCellphone;
    }

    public String getPrintTime() {
        return printTime;
    }

    public void setPrintTime(String printTime) {
        this.printTime = printTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getSettlementWay() {
        return settlementWay;
    }

    public void setSettlementWay(String settlementWay) {
        this.settlementWay = settlementWay;
    }

    public String getSettlementOperator() {
        return settlementOperator;
    }

    public void setSettlementOperator(String settlementOperator) {
        this.settlementOperator = settlementOperator;
    }

    public String getSettleWayDetails() {
        return settleWayDetails;
    }

    public void setSettleWayDetails(String settleWayDetails) {
        this.settleWayDetails = settleWayDetails;
    }

    public String getSubmitter() {
        return submitter;
    }

    public void setSubmitter(String submitter) {
        this.submitter = submitter;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getWaitAmount() {
        return waitAmount;
    }

    public void setWaitAmount(String waitAmount) {
        this.waitAmount = waitAmount;
    }
}
