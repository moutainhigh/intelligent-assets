package com.dili.ia.service;

import com.dili.assets.sdk.dto.BusinessChargeItemDto;
import com.dili.commons.glossary.YesOrNoEnum;
import com.dili.ia.domain.BusinessChargeItem;
import com.dili.ss.base.BaseService;

import java.util.List;
import java.util.Map;

/**
 * 由MyBatis Generator工具自动生成
 * This file was generated on 2020-05-29 16:13:04.
 */
public interface BusinessChargeItemService extends BaseService<BusinessChargeItem, Long> {
    /**
     * 查询业务产生的收费项（行转列）
     * @param bizType
     * @param businessIds
     * @param chargeItemDtos
     * @return
     */
    List<Map<String, String>> queryBusinessChargeItem(String bizType, List<Long> businessIds, List<BusinessChargeItemDto> chargeItemDtos);

    /**
     * 查询业务所配置的收费项
     * @param marketId
     * @param bizType
     * @param isEnable
     * @return
     */
    List<BusinessChargeItemDto> queryBusinessChargeItemConfig(Long marketId, String bizType, Integer isEnable);

    /**
     * 查询业务资产项的所产生的收费项元信息
     * @param businessIds
     * @return
     */
    List<BusinessChargeItemDto> queryBusinessChargeItemMeta(List<Long> businessIds);

    /**
     * 根据业务ID、Code、Type 删除旧有的动态收费项
     *
     * @param  businessId
     * @param  businessCode
     * @param  bizType
     * @return int
     * @date   2020/7/16
     */
    int deleteByBusinessIdAndCodeAndBizType(Long businessId, String businessCode, Integer bizType);

    /**
     * 通过业务ID修改支付中金额
     * @param businessId
     * @param bizType
     * @param paymentAmount (不传则按收费项全额分摊)
     */
    void unityUpdatePaymentAmountByBusinessId(Long businessId, String bizType , Long... paymentAmount);
}