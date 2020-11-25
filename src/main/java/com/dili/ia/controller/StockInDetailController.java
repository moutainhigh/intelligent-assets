package com.dili.ia.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dili.ia.domain.dto.StockInDetailQueryDto;
import com.dili.ia.service.DataAuthService;
import com.dili.ia.service.StockInDetailService;
import com.dili.ss.domain.EasyuiPageOutput;
import com.dili.ss.metadata.ValueProviderUtils;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.session.SessionContext;
import com.github.pagehelper.Page;

/**
 * 由MyBatis Generator工具自动生成
 * This file was generated on 2020-06-12 11:14:28.
 */
@Controller
@RequestMapping("/stock/stockInDetail")
public class StockInDetailController {
    @Autowired
    private StockInDetailService stockInDetailService;
    
    @Autowired
    private DataAuthService dataAuthService;

    /**
     * 跳转到StockInDetail页面
     * @param modelMap
     * @return String
     */
    @RequestMapping(value="/index.html", method = RequestMethod.GET)
    public String index(ModelMap modelMap) {
        return "stock/stockInDetail/index";
    }
    
    /**
     * 跳转到StockInDetail详情页面
     * @param modelMap
     * @return String
     */
    @RequestMapping(value="/view.html", method = RequestMethod.GET)
    public String view(ModelMap modelMap,String code) {
    	modelMap.putAll(stockInDetailService.viewStockInDetail(code));
        return "stock/stockInDetail/view";
    }
    
    /**
     * 分页查询StockInDetail，返回easyui分页信息
     * @param stockInDetail
     * @return String
     * @throws Exception
     */
    @RequestMapping(value="/listPage.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody String listPage(@ModelAttribute StockInDetailQueryDto stockInDetailQueryDto) throws Exception {
    	UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
    	stockInDetailQueryDto.setMarketId(userTicket.getFirmId());
    	List<Long> departmentIdList = dataAuthService.getDepartmentDataAuth(userTicket);
		if (CollectionUtils.isEmpty(departmentIdList)) {
			return new EasyuiPageOutput(0L, Collections.emptyList()).toString();
		}
		stockInDetailQueryDto.setDepIds(departmentIdList);
    	Page<Map<String, String>> page = stockInDetailService.selectByContion(stockInDetailQueryDto);
    	Map<String, String> map = stockInDetailQueryDto.getMetadata();
    	List<Map> result = ValueProviderUtils.buildDataByProvider(map, page.getResult());
    	return new EasyuiPageOutput(page.getTotal(), result ).toString();
    }

   
}