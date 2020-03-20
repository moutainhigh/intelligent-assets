package com.dili.ia.controller;

import com.dili.ia.domain.RefundOrder;
import com.dili.ia.domain.TransferDeductionItem;
import com.dili.ia.domain.dto.RefundOrderDto;
import com.dili.ia.glossary.BizTypeEnum;
import com.dili.ia.glossary.LogBizTypeEnum;
import com.dili.ia.glossary.RefundOrderStateEnum;
import com.dili.ia.service.LeaseOrderItemService;
import com.dili.ia.service.RefundOrderService;
import com.dili.ia.service.TransferDeductionItemService;
import com.dili.logger.sdk.domain.BusinessLog;
import com.dili.logger.sdk.domain.input.BusinessLogQueryInput;
import com.dili.logger.sdk.rpc.BusinessLogRpc;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.dto.DTOUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 由MyBatis Generator工具自动生成
 * This file was generated on 2020-03-09 19:34:40.
 */
@Api("/refundOrder")
@Controller
@RequestMapping("/refundOrder")
public class RefundOrderController {
    private final static Logger LOG = LoggerFactory.getLogger(RefundOrderController.class);
    @Autowired
    RefundOrderService refundOrderService;
    @Autowired
    LeaseOrderItemService leaseOrderItemService;
    @Autowired
    TransferDeductionItemService transferDeductionItemService;
    @Autowired
    BusinessLogRpc businessLogRpc;

    /**
     * 跳转到RefundOrder页面
     * @param modelMap
     * @return String
     */
    @ApiOperation("跳转到RefundOrder页面")
    @RequestMapping(value="/index.html", method = RequestMethod.GET)
    public String index(ModelMap modelMap) {
        return "refundOrder/index";
    }

    /**
     * 分页查询RefundOrder，返回easyui分页信息
     * @param refundOrder
     * @return String
     * @throws Exception
     */
    @ApiOperation(value="分页查询RefundOrder", notes = "分页查询RefundOrder，返回easyui分页信息")
    @ApiImplicitParams({
		@ApiImplicitParam(name="RefundOrder", paramType="form", value = "RefundOrder的form信息", required = false, dataType = "string")
	})
    @RequestMapping(value="/listPage.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody String listPage(RefundOrderDto refundOrder) throws Exception {
        return refundOrderService.listEasyuiPageByExample(refundOrder, true).toString();
    }

    /**
     * 跳转到退款单-查看页面
     * @param modelMap
     * @return String
     */
    @ApiOperation("跳转到退款单-查看页面")
    @RequestMapping(value="/view.html", method = RequestMethod.GET)
    public String view(ModelMap modelMap, Long id) {
        RefundOrder refundOrder = refundOrderService.get(id);
        if(null != id && refundOrder != null){
            modelMap.put("refundOrder",refundOrder);
            if (refundOrder.getBizType().equals(BizTypeEnum.EARNEST.getCode())){
                return "refundOrder/earnestRefundOrderView";
            }else if (refundOrder.getBizType().equals(BizTypeEnum.BOOTH_LEASE.getCode())){
                TransferDeductionItem transferDeductionItemCondition = DTOUtils.newInstance(TransferDeductionItem.class);
                transferDeductionItemCondition.setRefundOrderId(id);
                modelMap.put("transferDeductionItems",transferDeductionItemService.list(transferDeductionItemCondition));
                if(null != refundOrder.getOrderItemId()){
                    modelMap.put("leaseOrderItem",leaseOrderItemService.get(refundOrder.getOrderItemId()));
                }
                return "refundOrder/leaseRefundOrderView";
            }
        }

        try{
            //日志查询
            BusinessLogQueryInput businessLogQueryInput = new BusinessLogQueryInput();
            businessLogQueryInput.setBusinessId(id);
            businessLogQueryInput.setBusinessType(LogBizTypeEnum.REFUND_ORDER.getCode());
            BaseOutput<List<BusinessLog>> businessLogOutput = businessLogRpc.list(businessLogQueryInput);
            if(businessLogOutput.isSuccess()){
                modelMap.put("logs",businessLogOutput.getData());
            }
        }catch (Exception e){
            LOG.error("日志服务查询异常",e);
        }
        return "refundOrder/leaseRefundOrderView";
    }
    /**
     * 跳转到退款单-修改页面
     * @param modelMap
     * @return String
     */
    @ApiOperation("跳转到退款单-修改页面")
    @RequestMapping(value="/update.html", method = RequestMethod.GET)
    public String update(ModelMap modelMap, Long id) {
        RefundOrder refundOrder = refundOrderService.get(id);
        if(null != id && refundOrder != null){
            modelMap.put("refundOrder",refundOrder);
            if (refundOrder.getBizType().equals(BizTypeEnum.EARNEST.getCode())){
                return "refundOrder/earnestRefundOrderUpdate";
            }else if (refundOrder.getBizType().equals(BizTypeEnum.BOOTH_LEASE.getCode())){
                return "refundOrder/leaseRefundOrderUpdate";
            }
        }
        return "refundOrder/leaseRefundOrderUpdate";
    }

    /**
     * 退款单--提交
     * @param id
     * @return BaseOutput
     */
    @ApiOperation("退款单--提交")
    @ApiImplicitParams({
            @ApiImplicitParam(name="id", paramType="form", value = "RefundOrder的主键", required = true, dataType = "long")
    })
    @RequestMapping(value="/submit.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody BaseOutput submit(Long id) {
        try {
            RefundOrder refundOrder = refundOrderService.get(id);
            if (refundOrder == null){
                return BaseOutput.failure("未查询到退款单！");
            }
            BaseOutput out = refundOrderService.doSubmitDispatcher(refundOrder);
            return out;
        } catch (RuntimeException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            return BaseOutput.failure("提交出错！");
        }
    }

    /**
     * 退款单--撤回
     * @param id
     * @return BaseOutput
     */
    @ApiOperation("退款单--撤回")
    @ApiImplicitParams({
            @ApiImplicitParam(name="id", paramType="form", value = "RefundOrder的主键", required = true, dataType = "long")
    })
    @RequestMapping(value="/withdraw.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody BaseOutput withdraw(Long id) {
        try {
            RefundOrder refundOrder = refundOrderService.get(id);
            if (refundOrder == null){
                return BaseOutput.failure("未查询到退款单！");
            }
            BaseOutput out = refundOrderService.doWithdrawDispatcher(refundOrder);
            return out;
        } catch (RuntimeException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            return BaseOutput.failure("撤回出错！");
        }
    }

    /**
     * 退款单--取消
     * @param id
     * @return BaseOutput
     */
    @ApiOperation("定退款单--取消")
    @ApiImplicitParams({
            @ApiImplicitParam(name="id", paramType="form", value = "RefundOrder的主键", required = true, dataType = "long")
    })
    @RequestMapping(value="/cancel.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody BaseOutput cancel(Long id) {
        RefundOrder refundOrder = refundOrderService.get(id);
        if (!refundOrder.getState().equals(RefundOrderStateEnum.CREATED.getCode())){
            return BaseOutput.failure("取消失败，定金单状态已变更！");
        }
        refundOrderService.doCancelDispatcher(refundOrder);
        return BaseOutput.success("取消成功");
    }
}