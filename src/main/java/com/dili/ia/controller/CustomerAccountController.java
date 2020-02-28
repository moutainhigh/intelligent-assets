package com.dili.ia.controller;

import com.dili.ia.domain.CustomerAccount;
import com.dili.ia.service.CustomerAccountService;
import com.dili.ss.domain.BaseOutput;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.session.SessionContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

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

    /**
     * 跳转到CustomerAccount页面
     * @param modelMap
     * @return String
     */
    @ApiOperation("跳转到CustomerAccount页面")
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
    @ApiOperation(value="分页查询CustomerAccount", notes = "分页查询CustomerAccount，返回easyui分页信息")
    @ApiImplicitParams({
		@ApiImplicitParam(name="CustomerAccount", paramType="form", value = "CustomerAccount的form信息", required = false, dataType = "string")
	})
    @RequestMapping(value="/listPage.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody String listPage(CustomerAccount customerAccount) throws Exception {
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
     * @param customerAccount
     * @return BaseOutput
     */
    @ApiOperation("定金退款CustomerAccount")
    @ApiImplicitParams({
		@ApiImplicitParam(name="CustomerAccount", paramType="form", value = "CustomerAccount的form信息", required = true, dataType = "string")
	})
    @RequestMapping(value="/doEarnestRefund.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody BaseOutput doEarnestRefund(CustomerAccount customerAccount) {
        customerAccountService.updateSelective(customerAccount);
        return BaseOutput.success("修改成功");
    }

    /**
     * CustomerAccount --- 定金转移
     * @param id
     * @return BaseOutput
     */
    @ApiOperation("定金转移CustomerAccount")
    @ApiImplicitParams({
		@ApiImplicitParam(name="id", paramType="form", value = "CustomerAccount的主键", required = true, dataType = "long")
	})
    @RequestMapping(value="/doEarnestTransfer.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody BaseOutput doEarnestTransfer(Long id) {
        customerAccountService.delete(id);
        return BaseOutput.success("删除成功");
    }

    /**
     * 账户余额查询
     * @param customerId
     * @return
     */
    @RequestMapping(value="/listByCustomerId.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody BaseOutput listByCustomerId(Long customerId) {
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        if (userTicket == null) {
            return BaseOutput.failure("未登陆");
        }
        return BaseOutput.success().setData(customerAccountService.getCustomerAccountByCustomerId(customerId,/**userTicket.getFirmId()**/1L));
    }


}