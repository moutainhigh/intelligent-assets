package com.dili.ia.service.impl;

import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dili.ia.domain.MessageFee;
import com.dili.ia.domain.RefundOrder;
import com.dili.ia.glossary.BizTypeEnum;
import com.dili.ia.service.MessageFeeService;
import com.dili.ia.service.RefundOrderDispatcherService;
import com.dili.settlement.domain.SettleOrder;
import com.dili.ss.base.BaseServiceImpl;
import com.dili.ss.constant.ResultCode;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.exception.BusinessException;
import com.dili.ss.util.MoneyUtils;
import com.google.common.collect.Sets;

/**
 * <B>Description</B> 本软件源代码版权归农丰时代及其团队所有,未经许可不得任意复制与传播 <B>农丰时代科技有限公司</B>
 *
 * @Description TODO(用一句话描述该文件做什么)
 * @author yangfan
 * @date 2020年8月25日
 */
@Service
public class MessageFeeRefundOrderServiceImpl extends BaseServiceImpl<RefundOrder, Long>
		implements RefundOrderDispatcherService {

	@Autowired
	private MessageFeeService messageFeeService;

	@Override
	public BaseOutput updateHandler(RefundOrder refundOrder) {
		MessageFee messageFee = messageFeeService.getMessageFeeByCode(refundOrder.getBusinessCode());
		if(messageFee == null) {
			throw new BusinessException(ResultCode.DATA_ERROR, "信息费单不存在!");
		}
		if(refundOrder.getTotalRefundAmount() > messageFee.getAmount()) {
			throw new BusinessException(ResultCode.DATA_ERROR, "金额不正确,最大可退款金额["+MoneyUtils.centToYuan(messageFee.getAmount())+"]!");
		}
		return BaseOutput.success();
	}

	@Override
	public BaseOutput submitHandler(RefundOrder refundOrder) {
		//冻结客户资金，写入冻结记录
		return BaseOutput.success();
	}

	@Override
	public BaseOutput withdrawHandler(RefundOrder refundOrder) {
		return BaseOutput.success();
	}

	@Override
	public BaseOutput refundSuccessHandler(SettleOrder settleOrder, RefundOrder refundOrder) {
		messageFeeService.refundSuccessHandler(settleOrder, refundOrder);
		return BaseOutput.success();
	}

	@Override
	public BaseOutput cancelHandler(RefundOrder refundOrder) {
		messageFeeService.cancleRefund(refundOrder);
		return BaseOutput.success();
	}

	@Override
	public BaseOutput<Map<String, Object>> buildBusinessPrintData(RefundOrder refundOrder) {
		// messageFeeService.receiptRefundPrintData(refundOrder.getCode(), "reprint")
		return BaseOutput.success().setData(null);
	}

	@Override
	public Set<String> getBizType() {
		return Sets.newHashSet(BizTypeEnum.MESSAGEFEE.getCode());
	}

}
