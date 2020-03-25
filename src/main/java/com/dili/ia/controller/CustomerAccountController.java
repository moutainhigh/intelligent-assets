package com.dili.ia.controller;

import com.dili.ia.domain.CustomerAccount;
import com.dili.ia.domain.EarnestTransferOrder;
import com.dili.ia.domain.RefundOrder;
import com.dili.ia.domain.dto.CustomerAccountListDto;
import com.dili.ia.domain.dto.EarnestTransferDto;
import com.dili.ia.service.CustomerAccountService;
import com.dili.ia.service.DataAuthService;
import com.dili.ia.util.LogBizTypeConst;
import com.dili.ia.util.LoggerUtil;
import com.dili.logger.sdk.annotation.BusinessLogger;
import com.dili.logger.sdk.base.LoggerContext;
import com.dili.logger.sdk.domain.BusinessLog;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.domain.EasyuiPageOutput;
import com.dili.ss.exception.BusinessException;
import com.dili.ss.util.MoneyUtils;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.session.SessionContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.List;

/**
 * 由MyBatis Generator工具自动生成
 * This file was generated on 2020-02-14 10:18:23.
 */
@Api("/customerAccount")
@Controller
@RequestMapping("/customerAccount")
public class CustomerAccountController {
    @Autowired
    CustomerAccountService customerAccountService;
    @Autowired
    DataAuthService dataAuthService;

    /**
     * 跳转到CustomerAccount页面
     * @param modelMap
     * @return String
     */
    @RequestMapping(value="/index.html", method = RequestMethod.GET)
    public String index(ModelMap modelMap) {
        return "customerAccount/index";
    }

    /**
     * 分页查询CustomerAccount，返回easyui分页信息
     * @param customerAccount
     * @return String
     * @throws Exception
     */
    @RequestMapping(value="/listPage.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody String listPage(CustomerAccountListDto customerAccount) throws Exception {
        List<Long> marketIdList = dataAuthService.getMarketDataAuth(SessionContext.getSessionContext().getUserTicket());
        if (CollectionUtils.isEmpty(marketIdList)){
            return new EasyuiPageOutput(0, Collections.emptyList()).toString();
        }
        customerAccount.setMarketIds(marketIdList);
        return customerAccountService.listEasyuiPageByExample(customerAccount, true).toString();
    }

    /**
     * 跳转到CustomerAccount页面---定金退款
     * @param modelMap
     * @param id 客户账户Id
     * @return String
     */
    @ApiOperation("跳转到CustomerAccount页面---定金退款")
    @RequestMapping(value="/earnestRefund.html", method = RequestMethod.GET)
    public String earnestRefund(ModelMap modelMap, Long id) {
        if(null != id){
            CustomerAccount customerAccount = customerAccountService.get(id);
            modelMap.put("customerAccount",customerAccount);
        }
        return "customerAccount/earnestRefund";
    }
    /**
     * 跳转到CustomerAccount页面--定金转移
     * @param modelMap
     * @param id 客户账户Id
     * @return String
     */
    @ApiOperation("跳转到CustomerAccount页面--定金转移")
    @RequestMapping(value="/earnestTransfer.html", method = RequestMethod.GET)
    public String earnestTransfer(ModelMap modelMap, Long id) {
        if(null != id){
            CustomerAccount customerAccount = customerAccountService.get(id);
            modelMap.put("customerAccount",customerAccount);
        }
        return "customerAccount/earnestTransfer";
    }

    /**
     * CustomerAccount--- 定金退款
     * @param order
     * @return BaseOutput
     */
    @BusinessLogger(businessType = LogBizTypeConst.REFUND_ORDER, content="${businessCode!}客户【${customerName!}】申请退款${amountYuan!}元", operationType="refundApply", systemCode = "INTELLIGENT_ASSETS")
    @RequestMapping(value="/doAddEarnestRefund.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody BaseOutput doEarnestRefund(RefundOrder order) {
        try {
            BaseOutput<RefundOrder> out = customerAccountService.addEarnestRefund(order);
            if (out.isSuccess()){
                RefundOrder refundOrder = out.getData();
                UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
                LoggerContext.put("amountYuan", MoneyUtils.centToYuan(order.getPayeeAmount()));
                //记录业务日志
                BusinessLog businessLog = new BusinessLog();
                businessLog.setBusinessId(refundOrder.getId());
                businessLog.setBusinessCode(refundOrder.getCode());
                businessLog.setOperatorId(userTicket.getId());
                businessLog.setOperatorName(userTicket.getRealName());
                businessLog.setMarketId(userTicket.getFirmId());
                businessLog.setNotes(order.getRefundReason());
                LoggerUtil.buildLoggerContext(businessLog);
            }
            return out;
        } catch (BusinessException e) {
            return BaseOutput.failure(e.getErrorMsg());
        } catch (Exception e) {
            return BaseOutput.failure("创建退款出错！");
        }
    }

    /**
     * CustomerAccount --- 定金转移
     * @param efDto
     * @return BaseOutput
     */
    @BusinessLogger(businessType = LogBizTypeConst.CUSTOMER_ACCOUNT, content="${businessCode}客户【${payerName}】转移给客户【${customerName}】${amountYuan}元", operationType="transfer", systemCode = "INTELLIGENT_ASSETS")
    @RequestMapping(value="/doEarnestTransfer.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody BaseOutput doEarnestTransfer(EarnestTransferDto efDto) {
        try {
            UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
            //判断转入方客户账户是否存在,不存在先创建客户账户
            if (!customerAccountService.checkCustomerAccountExist(efDto.getCustomerId(), userTicket.getFirmId())){
                BaseOutput<CustomerAccount> cusOut = customerAccountService.addCustomerAccountByCustomerInfo(efDto.getCustomerId(), efDto.getCustomerName(), efDto.getCustomerCellphone(), efDto.getCertificateNumber());
                if (!cusOut.isSuccess()){
                    return cusOut;
                }
            }
            BaseOutput<EarnestTransferOrder> output = customerAccountService.addEarnestTransferOrder(efDto);
            if (!output.isSuccess()){
                return output;
            }
            BaseOutput<EarnestTransferOrder> transOutput = customerAccountService.earnestTransfer(output.getData());
            if (!transOutput.isSuccess()){
                return transOutput;
            }
            if (transOutput.isSuccess()){
                EarnestTransferOrder earnestTransferOrder = transOutput.getData();
                LoggerContext.put("amountYuan", MoneyUtils.centToYuan(efDto.getAmount()));
                //记录业务日志
                BusinessLog businessLog = new BusinessLog();
                businessLog.setBusinessId(earnestTransferOrder.getId());
                businessLog.setBusinessCode(earnestTransferOrder.getCode());
                businessLog.setOperatorId(userTicket.getId());
                businessLog.setOperatorName(userTicket.getRealName());
                businessLog.setMarketId(userTicket.getFirmId());
                businessLog.setNotes(efDto.getTransferReason());
                LoggerUtil.buildLoggerContext(businessLog);

            }

            return BaseOutput.success("转移成功！");
        } catch (BusinessException e) {
            return BaseOutput.failure(e.getErrorMsg());
        } catch (Exception e) {
            return BaseOutput.failure("转移出错！");
        }
    }

    /**
     * 账户余额查询
     * @param customerId
     * @return
     */
    @RequestMapping(value="/getCustomerAccountByCustomerId.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody BaseOutput getCustomerAccountByCustomerId(Long customerId) {
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        if (userTicket == null) {
            return BaseOutput.failure("未登陆");
        }
        return BaseOutput.success().setData(customerAccountService.getCustomerAccountByCustomerId(customerId,userTicket.getFirmId()));
    }


}