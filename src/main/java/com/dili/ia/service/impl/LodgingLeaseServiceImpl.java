package com.dili.ia.service.impl;

import com.dili.assets.sdk.dto.AssetsDTO;
import com.dili.assets.sdk.dto.AssetsQuery;
import com.dili.assets.sdk.dto.AssetsRentDTO;
import com.dili.assets.sdk.rpc.AssetsRpc;
import com.dili.commons.glossary.EnabledStateEnum;
import com.dili.commons.glossary.YesOrNoEnum;
import com.dili.ia.domain.AssetsLeaseOrder;
import com.dili.ia.domain.AssetsLeaseOrderItem;
import com.dili.ia.glossary.AssetsTypeEnum;
import com.dili.ia.service.AssetsLeaseService;
import com.dili.ss.constant.ResultCode;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.exception.BusinessException;
import com.dili.ss.util.DateUtils;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.session.SessionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 公寓租赁相关实现
 */
@Service
public class LodgingLeaseServiceImpl implements AssetsLeaseService {
    private final static Logger LOG = LoggerFactory.getLogger(LodgingLeaseServiceImpl.class);
    @Autowired
    private AssetsRpc assetsRpc;

    @Override
    public Integer getAssetsType() {
        return AssetsTypeEnum.LODGING.getCode();
    }

    @Override
    public Long checkAssets(List<Long> assetsIds, Long mchId, String batchId) {
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        if (userTicket == null) {
            throw new BusinessException(ResultCode.DATA_ERROR, "登录过期或未登录");
        }
        AssetsQuery assetsQuery = new AssetsQuery();
        assetsQuery.setMarketId(userTicket.getFirmId());
        assetsQuery.setIds(assetsIds.stream().map(o -> o.toString()).collect(Collectors.toList()));
        BaseOutput<List<AssetsDTO>> output = assetsRpc.searchAssets(assetsQuery);
        if (!output.isSuccess()) {
            throw new BusinessException(ResultCode.DATA_ERROR, "公寓接口调用异常 " + output.getMessage());
        }

        List<AssetsDTO> assetsDTOS = output.getData();
        if (assetsDTOS.size() != assetsIds.size()) {
            throw new BusinessException(ResultCode.DATA_ERROR, "公寓不存在，请重新修改后保存");
        }

        //检查是否有不同商户的公寓
        if (assetsDTOS.stream().collect(Collectors.groupingBy(AssetsDTO::getMarketId, Collectors.counting())).size() > 1) {
            throw new BusinessException(ResultCode.DATA_ERROR, "合同中公寓分属不同组织，请修改后再操作");
        }

        return assetsDTOS.get(0).getMchId();
    }


    @Override
    public void frozenAsset(AssetsLeaseOrder leaseOrder, List<AssetsLeaseOrderItem> leaseOrderItems) {
        leaseOrderItems.forEach(o->{
            AssetsRentDTO assetsRentDTO = new AssetsRentDTO();
            assetsRentDTO.setAssetsId(o.getAssetsId());
            assetsRentDTO.setType(AssetsTypeEnum.LODGING.getCode());
            assetsRentDTO.setStart(DateUtils.localDateTimeToUdate(leaseOrder.getStartTime()));
            assetsRentDTO.setEnd(DateUtils.localDateTimeToUdate(leaseOrder.getEndTime()));
            assetsRentDTO.setOrderId(leaseOrder.getId().toString());
            assetsRentDTO.setUser(leaseOrder.getCustomerName());
            BaseOutput assetsOutput = assetsRpc.addAssetsRent(assetsRentDTO);
            if(!assetsOutput.isSuccess()){
                LOG.info("冻结公寓异常【编号：{}】", leaseOrder.getCode());
                if(assetsOutput.getCode().equals("2500")){
                    throw new BusinessException(ResultCode.DATA_ERROR,o.getAssetsName()+"选择的时间期限重复，请修改后重新保存");
                }else{
                    throw new BusinessException(ResultCode.DATA_ERROR,assetsOutput.getMessage());
                }
            }
        });
    }

    /**
     * 释放租赁订单所有公寓
     * @param leaseOrderId
     */
    @Override
    public void unFrozenAllAsset(Long leaseOrderId) {
        AssetsRentDTO assetsRentDTO = new AssetsRentDTO();
        assetsRentDTO.setOrderId(leaseOrderId.toString());
        BaseOutput assetsOutput = assetsRpc.deleteAssetsRent(assetsRentDTO);
        if(!assetsOutput.isSuccess()){
            LOG.info("解冻租赁订单【leaseOrderId:{}】所有公寓异常{}", leaseOrderId, assetsOutput.getMessage());
            throw new BusinessException(ResultCode.DATA_ERROR,assetsOutput.getMessage());
        }
    }

    /**
     * 解冻消费公寓
     * @param leaseOrder
     */
    @Override
    public void leaseAsset(AssetsLeaseOrder leaseOrder) {
        AssetsRentDTO assetsRentDTO = new AssetsRentDTO();
        assetsRentDTO.setOrderId(leaseOrder.getId().toString());
        BaseOutput assetsOutput = assetsRpc.rentAssetsRent(assetsRentDTO);
        if(!assetsOutput.isSuccess()){
            LOG.info("公寓解冻出租异常{}",assetsOutput.getMessage());
            throw new BusinessException(ResultCode.DATA_ERROR,assetsOutput.getMessage());
        }
    }
}
