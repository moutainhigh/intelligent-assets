package com.dili.ia.service.impl;

import com.dili.ia.domain.RefundOrder;
import com.dili.ia.domain.TransactionDetails;
import com.dili.ia.domain.dto.RefundOrderPrintDto;
import com.dili.ia.glossary.BizTypeEnum;
import com.dili.ia.glossary.PrintTemplateEnum;
import com.dili.ia.glossary.TransactionItemTypeEnum;
import com.dili.ia.glossary.TransactionSceneTypeEnum;
import com.dili.ia.mapper.RefundOrderMapper;
import com.dili.ia.rpc.SettlementRpc;
import com.dili.ia.service.CustomerAccountService;
import com.dili.ia.service.RefundOrderDispatcherService;
import com.dili.ia.service.TransactionDetailsService;
import com.dili.settlement.domain.SettleOrder;
import com.dili.ss.base.BaseServiceImpl;
import com.dili.ss.domain.BaseOutput;
import com.dili.uap.sdk.rpc.DepartmentRpc;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 由MyBatis Generator工具自动生成
 * This file was generated on 2020-03-09 19:34:40.
 */
@Service
public class EarnestRefundOrderServiceImpl extends BaseServiceImpl<RefundOrder, Long> implements RefundOrderDispatcherService {

    public RefundOrderMapper getActualDao() {
        return (RefundOrderMapper)getDao();
    }
    @Autowired
    SettlementRpc settlementRpc;
    @Autowired
    DepartmentRpc departmentRpc;
    @Autowired
    CustomerAccountService customerAccountService;
    @Autowired
    TransactionDetailsService transactionDetailsService;

    @Override
    public Set<Integer> getBizType() {
        return Sets.newHashSet(BizTypeEnum.EARNEST.getCode());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseOutput submitHandler(RefundOrder refundOrder) {
        //冻结客户资金，写入冻结记录
        customerAccountService.frozenEarnest(refundOrder.getCustomerId(), refundOrder.getMarketId(), refundOrder.getPayeeAmount());
        Integer bizType = BizTypeEnum.EARNEST.getCode();
        Integer itemType = TransactionItemTypeEnum.EARNEST.getCode();
        Integer sceneType = TransactionSceneTypeEnum.FROZEN.getCode();
        TransactionDetails transactionDetails = transactionDetailsService.buildByConditions(sceneType, bizType, itemType, refundOrder.getPayeeAmount(), refundOrder.getOrderId(), refundOrder.getOrderCode(), refundOrder.getCustomerId(), refundOrder.getRefundReason(), refundOrder.getMarketId());
        transactionDetailsService.insertSelective(transactionDetails);
        return BaseOutput.success();
    }

    @Override
    public BaseOutput withdrawHandler(RefundOrder refundOrder) {
        //解冻客户资金，写入解冻记录
        customerAccountService.unfrozenEarnest(refundOrder.getCustomerId(), refundOrder.getMarketId(), refundOrder.getPayeeAmount());
        Integer bizType = BizTypeEnum.EARNEST.getCode();
        Integer itemType = TransactionItemTypeEnum.EARNEST.getCode();
        Integer sceneType = TransactionSceneTypeEnum.UNFROZEN.getCode();
        TransactionDetails transactionDetails = transactionDetailsService.buildByConditions(sceneType, bizType, itemType, refundOrder.getPayeeAmount(), refundOrder.getOrderId(), refundOrder.getOrderCode(), refundOrder.getCustomerId(), refundOrder.getRefundReason(), refundOrder.getMarketId());
        transactionDetailsService.insertSelective(transactionDetails);
        return BaseOutput.success();
    }

    @Override
    public BaseOutput refundSuccessHandler(SettleOrder settleOrder, RefundOrder refundOrder) {
        //解冻客户资金，扣除客户余额，写入解冻，扣除记录记录
        customerAccountService.refundSuccessEarnest(refundOrder.getCustomerId(), refundOrder.getMarketId(), refundOrder.getPayeeAmount());
        Integer bizType = BizTypeEnum.EARNEST.getCode();
        Integer itemType = TransactionItemTypeEnum.EARNEST.getCode();
        Integer sceneTypeUnfrozen = TransactionSceneTypeEnum.UNFROZEN.getCode();
        Integer sceneTypeRefund = TransactionSceneTypeEnum.REFUND.getCode();
        TransactionDetails unfrozenDetails = transactionDetailsService.buildByConditions(settleOrder, sceneTypeUnfrozen, bizType, itemType, refundOrder.getPayeeAmount(), refundOrder.getOrderId(), refundOrder.getOrderCode(), refundOrder.getCustomerId(), refundOrder.getRefundReason(), refundOrder.getMarketId());
        TransactionDetails refundDetails = transactionDetailsService.buildByConditions(settleOrder, sceneTypeRefund, bizType, itemType, refundOrder.getPayeeAmount(), refundOrder.getOrderId(), refundOrder.getOrderCode(), refundOrder.getCustomerId(), refundOrder.getRefundReason(), refundOrder.getMarketId());
        transactionDetailsService.insertSelective(unfrozenDetails);
        transactionDetailsService.insertSelective(refundDetails);
        return BaseOutput.success();
    }

    @Override
    public BaseOutput cancelHandler(RefundOrder refundOrder) {
        return BaseOutput.success();
    }

    @Override
    public BaseOutput<Map<String,Object>> buildBusinessPrintData(RefundOrder refundOrder) {
        Map<String,Object> resultMap = new HashMap<>();
        resultMap.put("printTemplateCode",PrintTemplateEnum.EARNEST_REFUND_ORDER.getCode());
        return BaseOutput.success().setData(resultMap);
    }
}