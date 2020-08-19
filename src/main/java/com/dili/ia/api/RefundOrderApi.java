package com.dili.ia.api;

import com.dili.ia.domain.RefundOrder;
import com.dili.ia.domain.dto.PrintDataDto;
import com.dili.ia.service.RefundOrderService;
import com.dili.ia.util.LoggerUtil;
import com.dili.logger.sdk.annotation.BusinessLogger;
import com.dili.settlement.domain.SettleOrder;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 退款单API
 *
 * <B>Description</B>
 * 本软件源代码版权归农丰时代及其团队所有,未经许可不得任意复制与传播
 * <B>农丰时代科技有限公司</B>
 *
 * @author qinkelan
 * @createTime 2020-03-13 11:29
 */
@RestController
@RequestMapping("/api/refundOrder")
public class RefundOrderApi {

    private final static Logger LOG = LoggerFactory.getLogger(RefundOrderApi.class);

    @Autowired
    RefundOrderService refundOrderService;

    /**
     * 退款单票据打印数据加载
     * @param orderCode 退款单订单号CODE
     * @param reprint 是否补打标记
     * @return BaseOutput<PrintDataDto>
     */
    @RequestMapping(value="/queryPrintData", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody
    BaseOutput<PrintDataDto> queryPrintData(String orderCode, Integer reprint){
        try{
            if(StringUtils.isBlank(orderCode) || null == reprint){
                return BaseOutput.failure("参数错误");
            }
            return refundOrderService.queryPrintData(orderCode,reprint);
        }catch (Exception e){
            LOG.error("获取打印数据异常！", e);
            return BaseOutput.failure(e.getMessage());
        }
    }

    /**
     * 退款单结算成功回调
     * @param settleOrder 结算单
     * @return BaseOutput<Boolean>
     */
    @BusinessLogger(businessType="refund_order", content="${code!}", operationType="refund", systemCode = "INTELLIGENT_ASSETS")
    @RequestMapping(value="/settlementDealHandler", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody BaseOutput<Boolean> settlementDealHandler(@RequestBody SettleOrder settleOrder){
        if (null == settleOrder){
            return BaseOutput.failure("回调参数为空！");
        }
        try{
            BaseOutput<RefundOrder> output = refundOrderService.doRefundSuccessHandler(settleOrder);
            if (output.isSuccess()){
                //记录业务日志
                LoggerUtil.buildLoggerContext(output.getData().getId(), output.getData().getCode(), settleOrder.getOperatorId(), settleOrder.getOperatorName(), output.getData().getMarketId(), null);
                return BaseOutput.success().setData(true);
            }
            return BaseOutput.failure(output.getMessage());
        }catch (BusinessException e){
            LOG.error("退款成功回调异常！" + e.getMessage(), e);
            return BaseOutput.failure(e.getMessage()).setData(false);
        }catch (Exception e){
            LOG.error("退款成功回调异常！", e);
            return BaseOutput.failure(e.getMessage()).setData(false);
        }
    }
}
