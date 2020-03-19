package com.dili.ia.service;

import com.dili.ia.domain.LeaseOrder;
import com.dili.ia.domain.RefundOrder;
import com.dili.ia.domain.dto.LeaseOrderListDto;
import com.dili.ia.domain.dto.PrintDataDto;
import com.dili.ia.domain.dto.RefundOrderDto;
import com.dili.settlement.domain.SettleOrder;
import com.dili.ss.base.BaseService;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.exception.BusinessException;

/**
 * 由MyBatis Generator工具自动生成
 * This file was generated on 2020-02-11 15:54:49.
 */
public interface LeaseOrderService extends BaseService<LeaseOrder, Long> {
    /**
     * 摊位租赁订单保存
     * @param dto
     * @return
     */
    BaseOutput saveLeaseOrder(LeaseOrderListDto dto);

    /**
     * 提交付款
     * @param id 租赁单ID
     * @param amount 交费金额
     * @param waitAmount 待缴费金额
     * @return
     */
    BaseOutput submitPayment(Long id,Long amount,Long waitAmount);

    /**
     * 摊位租赁订单取消
     * @param id
     * @return
     */
    BaseOutput cancelOrder(Long id);

    /**
     * 摊位租赁订单撤回
     * @param id
     * @return
     */
    BaseOutput withdrawOrder(Long id);

    /**
     * 根据结算信息修改摊位租赁相关信息
     * @param settleOrder
     * @return
     */
    BaseOutput<Boolean> updateLeaseOrderBySettleInfo(SettleOrder settleOrder);

    /**
     * 扫描已生效但状态未变更的单子，更改其状态
     * @return
     */
    BaseOutput<Boolean> scanNotActiveLeaseOrder();

    /**
     * 扫描已到期但状态未变更的单子，更改其状态
     * @return
     */
    BaseOutput<Boolean> scanExpiredLeaseOrder();

    /**
     * 查询打印数据
     * @param businessCode
     * @param reprint
     * @return
     */
    BaseOutput<PrintDataDto> queryPrintData(String businessCode, Integer reprint);

    /**
     * 退款申请
     * @param refundOrderDto
     * @return
     */
    BaseOutput createRefundOrder(RefundOrderDto refundOrderDto);

    /**
     * 取消退款单回调处理
     * @param leaseOrderId
     * @param leaseOrderItemId
     * @return
     */
    BaseOutput cancelRefundOrderHandler(Long leaseOrderId,Long leaseOrderItemId);

    /**
     * 结算退款单成功回调处理
     * @param refundOrder
     * @return
     */
    BaseOutput settleSuccessRefundOrderHandler(RefundOrder refundOrder);

}