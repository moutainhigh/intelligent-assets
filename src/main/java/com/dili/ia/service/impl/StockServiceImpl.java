package com.dili.ia.service.impl;

import com.dili.ia.domain.Stock;
import com.dili.ia.domain.StockIn;
import com.dili.ia.domain.StockInDetail;
import com.dili.ia.domain.StockOut;
import com.dili.ia.domain.StockRecord;
import com.dili.ia.domain.dto.StockDto;
import com.dili.ia.domain.dto.StockQueryDto;
import com.dili.ia.glossary.BizNumberTypeEnum;
import com.dili.ia.glossary.StockRecordTypeEnum;
import com.dili.ia.mapper.StockMapper;
import com.dili.ia.rpc.UidRpcResolver;
import com.dili.ia.service.StockOutService;
import com.dili.ia.service.StockRecordService;
import com.dili.ia.service.StockService;
import com.dili.ss.base.BaseServiceImpl;
import com.dili.ss.constant.ResultCode;
import com.dili.ss.exception.BusinessException;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.session.SessionContext;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import cn.hutool.core.bean.BeanUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 由MyBatis Generator工具自动生成
 * This file was generated on 2020-06-12 11:14:28.
 */
@Service
public class StockServiceImpl extends BaseServiceImpl<Stock, Long> implements StockService {
    private final static Logger LOG = LoggerFactory.getLogger(StockServiceImpl.class);

	@Autowired
	private StockRecordService stockRecordService;
	
	@Autowired
	private StockOutService stockOutService;
	
	@Autowired
	private UidRpcResolver uidRpcResolver;
	
    public StockMapper getActualDao() {
        return (StockMapper)getDao();
    }

	@Override
	@Transactional
	public void inStock(List<StockInDetail> details,StockIn stockIn) {
		if(CollectionUtils.isEmpty(details)) {
			throw new BusinessException(ResultCode.DATA_ERROR, "入库单详情为空");
		}
		for (StockInDetail stockInDetail : details) {
			//根据customerId categoryId assetsId 获取唯一库存信息
			Stock stock = getStock(stockInDetail.getCategoryId(), stockInDetail.getAssetsId(), stockIn.getCustomerId());
			if(stock == null) {
				//初始库存
				buildStock(stockInDetail, stockIn);
				//重试获取库存
				stock = getStock(stockInDetail.getCategoryId(), stockInDetail.getAssetsId(), stockIn.getCustomerId());
			}
			if (stock == null) {
				throw new BusinessException(ResultCode.DATA_ERROR, "数据异常");
			}
			//库存入库
			Stock domain = new Stock();
			domain.setWeight(stock.getWeight()+stockInDetail.getWeight());
			domain.setQuantity(stock.getQuantity()+stockInDetail.getQuantity());
			domain.setVersion(stock.getVersion()+1);
			Stock condition = new Stock();
			condition.setCategoryId(stock.getCategoryId());
			condition.setAssetsId(stock.getAssetsId());
			condition.setCustomerId(stock.getCustomerId());
			condition.setVersion(stock.getVersion());
			int row = updateSelectiveByExample(domain, condition);
			if(row != 1) {
				throw new BusinessException(ResultCode.DATA_ERROR, "入库失败");
			}
			//入库流水记录
			buildStockRecord(stockInDetail.getWeight(), stockInDetail.getQuantity(), stockInDetail.getCode(), stock,StockRecordTypeEnum.STOCK_IN);
		}
		
	}
	
	@Override
	public Stock getStock(Long categoryId,Long assetsId,Long customerId) {
		Stock example = new Stock();
		example.setCategoryId(categoryId);
		example.setAssetsId(assetsId);
		example.setCustomerId(customerId);
		List<Stock> stocks = listByExample(example);
		if(CollectionUtils.isEmpty(stocks)) {
			return null;		
		}
		if(stocks.size() != 1) {
			throw new BusinessException(ResultCode.DATA_ERROR, "数据异常");	
		}
		return stocks.get(0);
	}
	
	private void buildStock(StockInDetail stockInDetail,StockIn stockIn) {
		Stock domain = new Stock();
		domain.setWeight(0L);
		domain.setQuantity(0L);
		/*BeanUtils.copyProperties(stockInDetail, domain);
		BeanUtils.copyProperties(stockIn, domain);*/
		domain.setCategoryId(stockInDetail.getCategoryId());
		domain.setAssetsId(stockInDetail.getAssetsId());
		domain.setCustomerId(stockIn.getCustomerId());
		domain.setFirstDistrictId(stockInDetail.getParentDistrictId());
		domain.setFirstDistrictName(stockInDetail.getParentDistrictName());
		domain.setDistrictId(stockInDetail.getDistrictId());
		domain.setAssetsName(stockInDetail.getAssetsCode());
		domain.setCategoryName(stockInDetail.getCategoryName());
		domain.setCustomerName(stockIn.getCustomerName());	
		domain.setCustomerCellphone(stockIn.getCustomerCellphone());
		domain.setDistrictName(stockInDetail.getDistrictName());
		domain.setDepartmentId(stockIn.getDepartmentId());
		domain.setDepartmentName(stockIn.getDepartmentName());
		domain.setMarketId(stockIn.getMarketId());
		domain.setMarketCode(stockIn.getMarketCode());
		domain.setVersion(1);
		int row = insertSelective(domain);
		if(row != 1) {
			throw new BusinessException(ResultCode.DATA_ERROR, "数据异常");
		}
	}
	
	private void buildStockRecord(Long weight,Long quantity,String businessCode,Stock stock,StockRecordTypeEnum type) {
		StockRecord record = new StockRecord();
		record.setType(type.getCode());
		record.setWeight(weight);
		record.setQuantity(quantity);
		record.setBusinessCode(businessCode);
		record.setStockId(stock.getId());
		record.setMarketId(stock.getMarketId());
		record.setMarketCode(stock.getMarketCode());
		record.setCreateTime(LocalDateTime.now());
		record.setOperationDay(LocalDate.now());
		//计算期末库存
		if(StockRecordTypeEnum.STOCK_IN == type) {
			record.setStockQuantity(stock.getQuantity()+quantity);
			record.setStockWeight(stock.getWeight()+weight);
		}else {
			record.setStockQuantity(stock.getQuantity()-quantity);
			record.setStockWeight(stock.getWeight()-weight);
		}
		stockRecordService.insertSelective(record);		
	}

	@Override
	@Transactional
	public StockOut stockOut(Long stockId, Long weight, Long quantity,String notes) {
		UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
		Stock stock = this.get(stockId);
		stockDeduction(stock, 0L, quantity);
		// 出库单
		return buildStockOut(stock, userTicket, notes, 0L, quantity);
	}
	
	private void stockDeduction(Stock stock,Long weight, Long quantity){
		Stock domain = new Stock();
		domain.setVersion(stock.getVersion()+1);
		if((stock.getQuantity()-quantity) < 0 || (stock.getWeight() - weight) < 0) {
			throw new BusinessException(ResultCode.DATA_ERROR, "库存不足!");
		}
		if(quantity != null && quantity > 0) {
			domain.setQuantity(stock.getQuantity()-quantity);
		}
		if(weight != null && weight > 0) {
			domain.setWeight(stock.getWeight() - weight);
		}
		Stock condition = new Stock();
		condition.setId(stock.getId());
		condition.setVersion(stock.getVersion());
		int row = updateSelectiveByExample(domain, condition);
		if(row != 1) {
			throw new BusinessException(ResultCode.DATA_ERROR, "数据状态已改变,请刷新页面重试");
		}
	}
	
	public StockOut buildStockOut(Stock stock,UserTicket userTicket,String notes,Long weight, Long quantity) {
		StockOut stockOut = new StockOut();
		String code = uidRpcResolver.bizNumber(stock.getMarketCode()+"_"+BizNumberTypeEnum.STOCK_OUT.getCode());
		stockOut.setCode(code);
		BeanUtil.copyProperties(stock, stockOut,"id");
		stockOut.setQuantity(quantity);
		stockOut.setCreator(userTicket.getRealName());
		stockOut.setCreatorId(userTicket.getId());
		stockOut.setCreateTime(LocalDateTime.now());
		stockOut.setStockOutDate(LocalDateTime.now());
		stockOut.setNotes(notes);
		stockOutService.insertSelective(stockOut);
		//出库流水记录
		buildStockRecord(weight, quantity, code, stock,StockRecordTypeEnum.STOCK_OUT);
		return stockOut;
	}

	@Override
	@Transactional
	public void stockDeduction(StockInDetail detail,Long customerId,String businessCode) {
		Stock stock = getStock(detail.getCategoryId(), detail.getAssetsId(), customerId);
		stockDeduction(stock, detail.getWeight(), detail.getQuantity());
		buildStockRecord(detail.getWeight(), detail.getQuantity(), businessCode, stock, StockRecordTypeEnum.STOCK_CANCEL);
	}
	
	
	@Override
	public Page<StockDto> countCustomerStock(StockQueryDto stockQueryDto) {
		Page<StockDto> result = PageHelper.startPage(stockQueryDto.getPage(), stockQueryDto.getRows());
		getActualDao().countCustomerStock(stockQueryDto);
		return result;
	}
		
}