package com.dili.ia.controller;

import com.dili.ia.domain.TransactionDetails;
import com.dili.ia.domain.dto.TransactionDetailsListDto;
import com.dili.ia.service.DataAuthService;
import com.dili.ia.service.TransactionDetailsService;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.domain.EasyuiPageOutput;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.session.SessionContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
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
@Api("/transactionDetails")
@Controller
@RequestMapping("/transactionDetails")
public class TransactionDetailsController {
    @Autowired
    TransactionDetailsService transactionDetailsService;
    @Autowired
    DataAuthService dataAuthService;
    /**
     * 跳转到TransactionDetails页面
     * @param modelMap
     * @return String
     */
    @ApiOperation("跳转到TransactionDetails页面")
    @RequestMapping(value="/index.html", method = RequestMethod.GET)
    public String index(ModelMap modelMap) {
        //默认显示最近3天，结束时间默认为当前日期的23:59:59，开始时间为当前日期-2的00:00:00，选择到年月日时分秒
        Calendar c = Calendar.getInstance();
        c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        c.add(Calendar.DAY_OF_MONTH, -2);
        Date createdStart = c.getTime();

        Calendar ce = Calendar.getInstance();
        ce.set(ce.get(Calendar.YEAR), ce.get(Calendar.MONTH), ce.get(Calendar.DAY_OF_MONTH), 23, 59, 59);
        Date  createdEnd = ce.getTime();

        modelMap.put("createdStart", createdStart);
        modelMap.put("createdEnd", createdEnd);
        return "transactionDetails/index";
    }

    /**
     * 分页查询TransactionDetails，返回easyui分页信息
     * @param transactionDetails
     * @return String
     * @throws Exception
     */
    @ApiOperation(value="分页查询TransactionDetails", notes = "分页查询TransactionDetails，返回easyui分页信息")
    @ApiImplicitParams({
		@ApiImplicitParam(name="TransactionDetails", paramType="form", value = "TransactionDetails的form信息", required = false, dataType = "string")
	})
    @RequestMapping(value="/listPage.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody String listPage(TransactionDetailsListDto transactionDetails) throws Exception {
        List<Long> marketIdList = dataAuthService.getMarketDataAuth(SessionContext.getSessionContext().getUserTicket());
        if (CollectionUtils.isEmpty(marketIdList)){
            return new EasyuiPageOutput(0, Collections.emptyList()).toString();
        }
        transactionDetails.setMarketIds(marketIdList);
        return transactionDetailsService.listEasyuiPageByExample(transactionDetails, true).toString();
    }
}