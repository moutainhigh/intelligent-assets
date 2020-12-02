package com.dili.ia.service;

import com.dili.ia.domain.AssetsRental;
import com.dili.ia.domain.dto.AssetsRentalDto;
import com.dili.ss.base.BaseService;
import com.dili.uap.sdk.domain.UserTicket;

import java.util.List;

/**
 * @author:       xiaosa
 * @date:         2020/11/25
 * @version:      农批业务系统重构
 * @description:  资产出租预设
 */
public interface AssetsRentalService extends BaseService<AssetsRental, Long> {

    /**
     * 新增资产出租预设
     *
     * @param  assetsRentalDto
     * @param userTicket
     * @return AssetsRental
     * @date   2020/11/26
     */
    AssetsRental addAssetsRental(AssetsRentalDto assetsRentalDto, UserTicket userTicket) throws Exception;

    /**
     * 修改资产出租预设
     *
     * @param  assetsRentalDto
     * @return AssetsRental
     * @date   2020/11/26
     */
    AssetsRental updateAssetsRental(AssetsRentalDto assetsRentalDto) throws Exception;

    /**
     * 启用或者禁用
     *
     * @param  id
     * @return BaseOutput
     * @date   2020/11/26
     */
    void enableOrDisable(Long id);

    /**
     * 根据摊位 id 查询相关的预设信息
     *
     * @param  assetsId
     * @return BaseOutput
     * @date   2020/12/2
     */
    AssetsRentalDto getRentalByAssetsId(Long assetsId);

    /**
     * 根据摊位 ids 查询是否属于一个批次
     *
     * @param  assetsIds
     * @return BaseOutput
     * @date   2020/12/2
     */
    boolean belongsBatchByAssetsIds(List<Long> assetsIds);
}