package com.dili.ia.controller;

import com.dili.ia.domain.AssetsLeaseOrderItem;
import com.dili.ia.glossary.StopWayEnum;
import com.dili.ia.service.AssetsLeaseOrderItemService;
import com.dili.ia.util.LogBizTypeConst;
import com.dili.logger.sdk.annotation.BusinessLogger;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.exception.BusinessException;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.session.SessionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 由MyBatis Generator工具自动生成
 * This file was generated on 2020-05-29 14:40:05.
 */
@Controller
@RequestMapping("/assetsLeaseOrderItem")
public class AssetsLeaseOrderItemController {
    private final static Logger LOG = LoggerFactory.getLogger(AssetsLeaseOrderItemController.class);
    @Autowired
    AssetsLeaseOrderItemService assetsLeaseOrderItemService;


    /**
     * 分页查询LeaseOrderItem，返回easyui分页信息
     * @param leaseOrderItem
     * @return String
     * @throws Exception
     */
    @RequestMapping(value="/listPage.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody String listPage(AssetsLeaseOrderItem leaseOrderItem) throws Exception {
        return assetsLeaseOrderItemService.listEasyuiPageByExample(leaseOrderItem, true).toString();
    }

    /**
     * 租赁
     * @param leaseOrderItem
     * @return
     */
    @BusinessLogger(businessType = LogBizTypeConst.BOOTH_LEASE,content = "${boothName} ${isNotEmpty(stopTime)?'停租时间:'+stopTime : ''}",operationType="stopLease",systemCode = "INTELLIGENT_ASSETS")
    @RequestMapping(value="/stopRent.action", method = {RequestMethod.POST})
    public @ResponseBody BaseOutput stopRent(AssetsLeaseOrderItem leaseOrderItem){
        try {
            UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
            if (userTicket == null) {
                throw new RuntimeException("未登录");
            }
            if(null == leaseOrderItem.getStopWay()|| null == leaseOrderItem.getId()){
                return BaseOutput.failure("参数错误");
            }
            if(StopWayEnum.TIMING.getCode().equals(leaseOrderItem.getStopWay()) && null == leaseOrderItem.getStopTime()){
                return BaseOutput.failure("参数错误");
            }
            BaseOutput output = assetsLeaseOrderItemService.stopRent(leaseOrderItem);
            return output;
        }catch (BusinessException e){
            LOG.info("摊位停租异常！", e);
            return BaseOutput.failure(e.getErrorMsg());
        }catch (Exception e){
            LOG.error("摊位停租异常！", e);
            return BaseOutput.failure(e.getMessage());
        }


    }
}