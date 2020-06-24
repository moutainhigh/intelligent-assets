package com.dili.ia.controller;

import com.dili.ia.domain.StockRecord;
import com.dili.ia.service.StockRecordService;
import com.dili.ss.domain.BaseOutput;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 由MyBatis Generator工具自动生成
 * This file was generated on 2020-06-12 11:14:28.
 */
@Controller
@RequestMapping("/stockRecord")
public class StockRecordController {
    @Autowired
    StockRecordService stockRecordService;

    /**
     * 跳转到StockRecord页面
     * @param modelMap
     * @return String
     */
    @RequestMapping(value="/index.html", method = RequestMethod.GET)
    public String index(ModelMap modelMap) {
        return "stockRecord/index";
    }

    /**
     * 分页查询StockRecord，返回easyui分页信息
     * @param stockRecord
     * @return String
     * @throws Exception
     */
    @RequestMapping(value="/listPage.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody String listPage(@ModelAttribute StockRecord stockRecord) throws Exception {
        return stockRecordService.listEasyuiPageByExample(stockRecord, true).toString();
    }

    /**
     * 新增StockRecord
     * @param stockRecord
     * @return BaseOutput
     */
    @RequestMapping(value="/insert.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody BaseOutput insert(@ModelAttribute StockRecord stockRecord) {
        stockRecordService.insertSelective(stockRecord);
        return BaseOutput.success("新增成功");
    }

    /**
     * 修改StockRecord
     * @param stockRecord
     * @return BaseOutput
     */
    @RequestMapping(value="/update.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody BaseOutput update(@ModelAttribute StockRecord stockRecord) {
        stockRecordService.updateSelective(stockRecord);
        return BaseOutput.success("修改成功");
    }

    /**
     * 删除StockRecord
     * @param id
     * @return BaseOutput
     */
    @RequestMapping(value="/delete.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody BaseOutput delete(Long id) {
        stockRecordService.delete(id);
        return BaseOutput.success("删除成功");
    }
}