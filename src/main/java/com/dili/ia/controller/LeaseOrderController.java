package com.dili.ia.controller;

import com.alibaba.fastjson.JSON;
import com.dili.ia.domain.LeaseOrder;
import com.dili.ia.domain.LeaseOrderItem;
import com.dili.ia.domain.dto.LeaseOrderListDto;
import com.dili.ia.service.LeaseOrderItemService;
import com.dili.ia.service.LeaseOrderService;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.dto.DTOUtils;
import com.dili.ss.exception.BusinessException;
import com.dili.ss.util.DateUtils;
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
import org.springframework.web.bind.annotation.ResponseBody;

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
     * @return String
     */
    @ApiOperation("跳转到LeaseOrder查看页面")
    @RequestMapping(value="/view.html", method = RequestMethod.GET)
    public String view(ModelMap modelMap,Long id) {
        if(null != id){
            LeaseOrder leaseOrder = leaseOrderService.get(id);
            LeaseOrderItem condition = DTOUtils.newInstance(LeaseOrderItem.class);
            condition.setLeaseOrderId(id);
            List<LeaseOrderItem> leaseOrderItems = leaseOrderItemService.list(condition);
            modelMap.put("leaseOrder",leaseOrder);
            modelMap.put("leaseOrderItems", JSON.toJSONString(leaseOrderItems));
        }
        return "leaseOrder/view";
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
        }
        return "leaseOrder/preSave";
    }

    /**
     * 跳转到LeaseOrder新增页面
     * @param modelMap
     * @return String
     */
    @ApiOperation("跳转到LeaseOrder退款申请页面")
    @RequestMapping(value="/refundApply.html", method = RequestMethod.GET)
    public String refundApply(ModelMap modelMap) {
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
        if (StringUtils.isNotBlank(leaseOrder.getBoothName())) {
            LeaseOrderItem leaseOrderItemCondition = DTOUtils.newDTO(LeaseOrderItem.class);
            leaseOrderItemCondition.setBoothName(leaseOrder.getBoothName());
            leaseOrder.setIds(leaseOrderItemService.list(leaseOrderItemCondition).stream().map(LeaseOrderItem::getLeaseOrderId).collect(Collectors.toList()));
        }
        return leaseOrderService.listEasyuiPageByExample(leaseOrder, true).toString();
    }

    /**
     * 摊位租赁订单保存
     * @param leaseOrder
     * @return
     * @throws Exception
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

}