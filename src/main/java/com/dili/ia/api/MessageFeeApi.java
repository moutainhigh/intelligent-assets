package com.dili.ia.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.dili.ia.domain.dto.printDto.LaborPayPrintDto;
import com.dili.ia.domain.dto.printDto.PrintDataDto;
import com.dili.ia.service.MessageFeeService;
import com.dili.ia.util.LogBizTypeConst;
import com.dili.logger.sdk.annotation.BusinessLogger;
import com.dili.settlement.domain.SettleOrder;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.exception.BusinessException;

/**
 * <B>Description</B>
 * 本软件源代码版权归农丰时代及其团队所有,未经许可不得任意复制与传播
 * <B>农丰时代科技有限公司</B>
 *
 * @Description 信息费接口
 * @author yangfan
 * @date 2020年8月25日
 */
@RestController
@RequestMapping("/api/fee/message")
public class MessageFeeApi {
	
	private final static Logger LOG = LoggerFactory.getLogger(MessageFeeApi.class);
	
	@Autowired
	private MessageFeeService messageFeeService;
	
	/**
     * 信息费结算成功回调
     * @param settleOrder
     * @return
     */
    @BusinessLogger(businessType=LogBizTypeConst.MESSAGE_FEE, content="${code!}", operationType="pay", systemCode = "IA")
    @RequestMapping(value="/settlementDealHandler", method = {RequestMethod.POST})
    public @ResponseBody BaseOutput<Boolean> settlementDealHandler(@RequestBody SettleOrder settleOrder){
        try{
        	messageFeeService.settlementDealHandler(settleOrder);
            return BaseOutput.success();
        }catch (BusinessException e){
            LOG.error("信息费结算回调异常！结算单{},入库单{}",settleOrder.getCode(),settleOrder.getBusinessCode(), e);
            return BaseOutput.failure(e.getMessage()).setData(false);
        }catch (Exception e){
            LOG.error("信息费结算回调异常！", e);
            return BaseOutput.failure("信息费结算回调异常！").setData(false);
        }
    }
    
    /**
	 * 
	 * @Title scanEffective
	 * @Description 定时任务扫描失效,待生效马甲单
	 * @throws
	 */
	@RequestMapping(value = "/scanEffective", method = { RequestMethod.GET, RequestMethod.POST })
	public void scanEffective() {
		LOG.info("--信息费单定时任务开始--");
		messageFeeService.scanEffective();
		LOG.info("--信息费单定时任务结束--");
	}
    
	/**
     * 信息费结算票据打印
     * @param settleOrder
     * @return
     */
    @RequestMapping(value="/queryPrintData/payment", method = {RequestMethod.POST,RequestMethod.GET})
    public @ResponseBody BaseOutput<PrintDataDto<LaborPayPrintDto>> queryPaymentPrintData(String orderCode, String reprint){
        try{
            return BaseOutput.success().setData(messageFeeService.receiptPaymentData(orderCode, reprint));
        }catch (BusinessException e){
            LOG.error("信息费结算票据打印异常！", e);
            return BaseOutput.failure(e.getMessage()).setData(false);
        }catch (Exception e){
            LOG.error("信息费结算票据打印异常！", e);
            return BaseOutput.failure("信息费票据打印异常！").setData(false);
        }
    }
    
    /**
     * 信息费退款票据打印
     * @param settleOrder
     * @return
     */
	/*@RequestMapping(value="/queryPrintData/refund", method = {RequestMethod.POST})
	public @ResponseBody BaseOutput<PrintDataDto<MessageFeeRefundPrintDto>> queryRefundPrintData(String orderCode, String reprint){
	    try{
	        return BaseOutput.success().setData(messageFeeService.receiptRefundPrintData(orderCode, reprint));
	    }catch (BusinessException e){
	        LOG.error("信息费结算票据打印异常！", e);
	        return BaseOutput.failure(e.getMessage()).setData(false);
	    }catch (Exception e){
	        LOG.error("信息费结算票据打印异常！", e);
	        return BaseOutput.failure("信息费票据打印异常！").setData(false);
	    }
	}*/
	
}
