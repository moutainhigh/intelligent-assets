package com.dili.ia.controller;

import com.alibaba.fastjson.JSON;
import com.dili.ia.domain.LeaseOrder;
import com.dili.ia.domain.LeaseOrderItem;
import com.dili.ia.domain.PaymentOrder;
import com.dili.ia.domain.dto.LeaseOrderListDto;
import com.dili.ia.domain.dto.RefundOrderDto;
import com.dili.ia.glossary.*;
import com.dili.ia.service.LeaseOrderItemService;
import com.dili.ia.service.LeaseOrderService;
import com.dili.ia.service.PaymentOrderService;
import com.dili.logger.sdk.domain.BusinessLog;
import com.dili.logger.sdk.domain.input.BusinessLogQueryInput;
import com.dili.logger.sdk.rpc.BusinessLogRpc;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.dto.DTOUtils;
import com.dili.ss.exception.BusinessException;
import com.dili.ss.util.DateUtils;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.session.SessionContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import tk.mybatis.mapper.entity.Example;

/**
 * 由MyBatis Generator工具自动生成
 * This file was generated on 2020-02-11 15:54:49.
 */
@Api("/leaseOrder")
@Controller
@RequestMapping("/leaseOrder")
public class LeaseOrderController {
    private final static Logger LOG = LoggerFactory.getLogger(LeaseOrderController.class);

    @Autowired
    LeaseOrderService leaseOrderService;
    @Autowired
    LeaseOrderItemService leaseOrderItemService;
    @Autowired
    PaymentOrderService paymentOrderService;
  /*  @Autowired
    BusinessLogRpc businessLogRpc;*/

    /**
     * 跳转到LeaseOrder页面
     * @param modelMap
     * @return String
     */
    @ApiOperation("跳转到LeaseOrder页面")
    @RequestMapping(value="/index.html", method = RequestMethod.GET)
    public String index(ModelMap modelMap) {
        return "leaseOrder/index";
    }

    /**
     * 跳转到LeaseOrder查看页面
     * @param modelMap
     * @param businessCode 缴费单CODE
     * @return String
     */
    @ApiOperation("跳转到LeaseOrder查看页面")
    @RequestMapping(value="/view.html", method = RequestMethod.GET)
    public String view(ModelMap modelMap,Long id,String businessCode) {
        LeaseOrder leaseOrder = null;
        if(null != id) {
            leaseOrder = leaseOrderService.get(id);
        }else if(StringUtils.isNotBlank(businessCode)){
            PaymentOrder paymentOrder = DTOUtils.newInstance(PaymentOrder.class);
            paymentOrder.setCode(businessCode);
            leaseOrder = leaseOrderService.get(paymentOrderService.listByExample(paymentOrder).stream().findFirst().orElse(null).getBusinessId());
            id = leaseOrder.getId();
        }

        LeaseOrderItem condition = DTOUtils.newInstance(LeaseOrderItem.class);
        condition.setLeaseOrderId(id);
        List<LeaseOrderItem> leaseOrderItems = leaseOrderItemService.list(condition);
        modelMap.put("leaseOrder",leaseOrder);
        modelMap.put("leaseOrderItems", leaseOrderItems);

        //日志查询
//        BusinessLogQueryInput businessLogQueryInput = new BusinessLogQueryInput();
//        businessLogQueryInput.setBusinessId(id);
//        businessLogQueryInput.setBusinessType(LogBizTypeEnum.BOOTH_LEASE.getCode());
//        BaseOutput<List<BusinessLog>> businessLogOutput = businessLogRpc.list(businessLogQueryInput);
//        if(businessLogOutput.isSuccess()){
//            modelMap.put("logs",businessLogOutput.getData());
//        }
        return "leaseOrder/view";
    }

    /**
     * 跳转到LeaseOrder续租页面
     * @param modelMap
     * @param id 老单子ID
     * @return String
     */
    @ApiOperation("跳转到LeaseOrder续租页面")
    @RequestMapping(value="/renew.html", method = RequestMethod.GET)
    public String renew(ModelMap modelMap,Long id) {
        if(null != id){
            LeaseOrder leaseOrder = leaseOrderService.get(id);
            LeaseOrderItem condition = DTOUtils.newInstance(LeaseOrderItem.class);
            condition.setLeaseOrderId(id);
            List<LeaseOrderItem> leaseOrderItems = leaseOrderItemService.list(condition);
            modelMap.put("leaseOrder",leaseOrder);
            modelMap.put("isRenew", IsRenewEnum.YES.getCode());
            modelMap.put("leaseOrderItems", JSON.toJSONString(leaseOrderItems));
        }
        return "leaseOrder/preSave";
    }

    /**
     * 跳转到LeaseOrder新增页面
     * @param modelMap
     * @return String
     */
    @ApiOperation("跳转到LeaseOrder新增页面")
    @RequestMapping(value="/preSave.html", method = RequestMethod.GET)
    public String add(ModelMap modelMap,Long id) {
        if(null != id){
            LeaseOrder leaseOrder = leaseOrderService.get(id);
            LeaseOrderItem condition = DTOUtils.newInstance(LeaseOrderItem.class);
            condition.setLeaseOrderId(id);
            List<LeaseOrderItem> leaseOrderItems = leaseOrderItemService.list(condition);
            modelMap.put("leaseOrder",leaseOrder);
            modelMap.put("leaseOrderItems", JSON.toJSONString(leaseOrderItems));
        }else{
            modelMap.put("isRenew", IsRenewEnum.NO.getCode());
        }
        return "leaseOrder/preSave";
    }

    /**
     *
     * @param modelMap
     * @param id 对应租赁单ID 或 订单项ID
     * @param type 1：租赁单退款 2： 子单退款
     * @return
     */
    @ApiOperation("跳转到LeaseOrder退款申请页面")
    @RequestMapping(value="/refundApply.html", method = RequestMethod.GET)
    public String refundApply(ModelMap modelMap,Long id,Integer type) {
        if(LeaseOrderRefundTypeEnum.LEASE_ORDER_REFUND.getCode().equals(type)){
            modelMap.put("leaseOrder",leaseOrderService.get(id));
        }else if(LeaseOrderRefundTypeEnum.LEASE_ORDER_ITEM_REFUND.getCode().equals(type)){
            LeaseOrderItem leaseOrderItem = leaseOrderItemService.get(id);
            modelMap.put("leaseOrderItem",leaseOrderItem);
            modelMap.put("leaseOrder",leaseOrderService.get(leaseOrderItem.getLeaseOrderId()));
        }
        return "leaseOrder/refundApply";
    }

    /**
     * 分页查询LeaseOrder，返回easyui分页信息
     * @param leaseOrder
     * @return String
     * @throws Exception
     */
    @ApiOperation(value="分页查询LeaseOrder", notes = "分页查询LeaseOrder，返回easyui分页信息")
    @ApiImplicitParams({
		@ApiImplicitParam(name="LeaseOrder", paramType="form", value = "LeaseOrder的form信息", required = false, dataType = "string")
	})
    @RequestMapping(value="/listPage.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody String listPage(LeaseOrderListDto leaseOrder) throws Exception {
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        if (userTicket == null) {
            throw new RuntimeException("未登录");
        }
        leaseOrder.setMarketId(userTicket.getFirmId());

        if (StringUtils.isNotBlank(leaseOrder.getBoothName())) {
            LeaseOrderItem leaseOrderItemCondition = DTOUtils.newDTO(LeaseOrderItem.class);
            leaseOrderItemCondition.setBoothName(leaseOrder.getBoothName());
            leaseOrder.setIds(leaseOrderItemService.list(leaseOrderItemCondition).stream().map(LeaseOrderItem::getLeaseOrderId).collect(Collectors.toList()));
        }
        return leaseOrderService.listEasyuiPageByExample(leaseOrder, true).toString();
    }

    /**
     * 租赁订单信息补录
     * @param leaseOrder
     * @return
     */
    @RequestMapping(value="/supplement.action", method = {RequestMethod.POST})
    public @ResponseBody BaseOutput supplement(LeaseOrder leaseOrder){
        try {
            LeaseOrder oldLeaseOrder = leaseOrderService.get(leaseOrder.getId());
            leaseOrder.setVersion(oldLeaseOrder.getVersion());
            leaseOrderService.updateSelective(leaseOrder);
            return BaseOutput.success();
        }catch (Exception e){
            LOG.error("补录异常！", e);
            return BaseOutput.failure("补录异常");
        }


    }

    /**
     * 租赁订单取消
     * @param id 订单ID
     * @return
     */
    @RequestMapping(value="/cancelOrder.action", method = {RequestMethod.POST})
    public @ResponseBody BaseOutput cancelOrder(Long id){
        try {
            return leaseOrderService.cancelOrder(id);
        }catch (Exception e){
            LOG.error("租赁订单取消异常！", e);
            return BaseOutput.failure("租赁订单取消异常");
        }


    }

    /**
     * 租赁订单撤回
     * @param id 订单ID
     * @return
     */
    @RequestMapping(value="/withdrawOrder.action", method = {RequestMethod.POST})
    public @ResponseBody BaseOutput withdrawOrder(Long id){
        try {
            return leaseOrderService.withdrawOrder(id);
        }catch (Exception e){
            LOG.error("租赁订单撤回异常！", e);
            return BaseOutput.failure("租赁订单撤回异常");
        }


    }

    /**
     * 摊位租赁订单保存
     * @param leaseOrder
     * @return
     */
    @RequestMapping(value="/saveLeaseOrder.action", method = {RequestMethod.POST})
    public @ResponseBody BaseOutput saveLeaseOrder(LeaseOrderListDto leaseOrder){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(leaseOrder.getEndTime());
        calendar.add(Calendar.HOUR_OF_DAY,23);
        calendar.add(Calendar.MINUTE,59);
        calendar.add(Calendar.SECOND,59);
        leaseOrder.setEndTime(calendar.getTime());
        try{
            return leaseOrderService.saveLeaseOrder(leaseOrder);
        }catch (BusinessException e){
            LOG.error("摊位租赁订单保存异常！", e);
            return BaseOutput.failure(e.getMessage());
        }catch (Exception e){
            LOG.error("摊位租赁订单保存异常！", e);
            return BaseOutput.failure(e.getMessage());
        }
    }


    /**
     * 提交付款
     * @param id
     * @param amount
     * @return
     */
    @RequestMapping(value="/submitPayment.action", method = {RequestMethod.POST})
    public @ResponseBody BaseOutput submitPayment(@RequestParam Long id,@RequestParam Long amount,@RequestParam Long waitAmount){
        try{
            if(amount <= 0L){
                return BaseOutput.failure("支付金额必须大于0");
            }
            return leaseOrderService.submitPayment(id,amount,waitAmount);
        }catch (BusinessException e){
            LOG.error("摊位租赁订单提交付款异常！", e);
            return BaseOutput.failure(e.getMessage());
        }catch (Exception e){
            LOG.error("摊位租赁订单提交付款异常！", e);
            return BaseOutput.failure(e.getMessage());
        }
    }

    /**
     * 摊位租赁退款申请
     * @param refundOrderDto
     * @return BaseOutput
     */
    @ApiOperation("摊位租赁退款申请")
    @RequestMapping(value="/createRefundOrder.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody BaseOutput createRefundOrder(RefundOrderDto refundOrderDto) {
        try{
            return leaseOrderService.createRefundOrder(refundOrderDto);
        }catch (BusinessException e){
            LOG.error("摊位租赁退款申请异常！", e);
            return BaseOutput.failure(e.getMessage());
        }catch (Exception e){
            LOG.error("摊位租赁退款申请异常！", e);
            return BaseOutput.failure(e.getMessage());
        }
    }

}