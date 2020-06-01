package com.dili.ia.controller;

import com.dili.ia.domain.RefundFeeItem;
import com.dili.ia.service.RefundFeeItemService;
import com.dili.ss.domain.BaseOutput;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
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
 * This file was generated on 2020-05-29 16:13:04.
 */
@Api("/refundFeeItem")
@Controller
@RequestMapping("/refundFeeItem")
public class RefundFeeItemController {
    @Autowired
    RefundFeeItemService refundFeeItemService;

    /**
     * 跳转到RefundFeeItem页面
     * @param modelMap
     * @return String
     */
    @ApiOperation("跳转到RefundFeeItem页面")
    @RequestMapping(value="/index.html", method = RequestMethod.GET)
    public String index(ModelMap modelMap) {
        return "refundFeeItem/index";
    }

    /**
     * 分页查询RefundFeeItem，返回easyui分页信息
     * @param refundFeeItem
     * @return String
     * @throws Exception
     */
    @ApiOperation(value="分页查询RefundFeeItem", notes = "分页查询RefundFeeItem，返回easyui分页信息")
    @ApiImplicitParams({
		@ApiImplicitParam(name="RefundFeeItem", paramType="form", value = "RefundFeeItem的form信息", required = false, dataType = "string")
	})
    @RequestMapping(value="/listPage.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody String listPage(@ModelAttribute RefundFeeItem refundFeeItem) throws Exception {
        return refundFeeItemService.listEasyuiPageByExample(refundFeeItem, true).toString();
    }

    /**
     * 新增RefundFeeItem
     * @param refundFeeItem
     * @return BaseOutput
     */
    @ApiOperation("新增RefundFeeItem")
    @ApiImplicitParams({
		@ApiImplicitParam(name="RefundFeeItem", paramType="form", value = "RefundFeeItem的form信息", required = true, dataType = "string")
	})
    @RequestMapping(value="/insert.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody BaseOutput insert(@ModelAttribute RefundFeeItem refundFeeItem) {
        refundFeeItemService.insertSelective(refundFeeItem);
        return BaseOutput.success("新增成功");
    }

    /**
     * 修改RefundFeeItem
     * @param refundFeeItem
     * @return BaseOutput
     */
    @ApiOperation("修改RefundFeeItem")
    @ApiImplicitParams({
		@ApiImplicitParam(name="RefundFeeItem", paramType="form", value = "RefundFeeItem的form信息", required = true, dataType = "string")
	})
    @RequestMapping(value="/update.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody BaseOutput update(@ModelAttribute RefundFeeItem refundFeeItem) {
        refundFeeItemService.updateSelective(refundFeeItem);
        return BaseOutput.success("修改成功");
    }

    /**
     * 删除RefundFeeItem
     * @param id
     * @return BaseOutput
     */
    @ApiOperation("删除RefundFeeItem")
    @ApiImplicitParams({
		@ApiImplicitParam(name="id", paramType="form", value = "RefundFeeItem的主键", required = true, dataType = "long")
	})
    @RequestMapping(value="/delete.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody BaseOutput delete(Long id) {
        refundFeeItemService.delete(id);
        return BaseOutput.success("删除成功");
    }
}