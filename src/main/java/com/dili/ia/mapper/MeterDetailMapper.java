package com.dili.ia.mapper;

import com.dili.ia.domain.MeterDetail;
import com.dili.ia.domain.dto.MeterDetailDto;
import com.dili.ss.base.MyMapper;

import java.util.List;

/**
 * @author:      xiaosa
 * @date:        2020/6/23
 * @version:     农批业务系统重构
 * @description: 水电费 dao 层
 */
public interface MeterDetailMapper extends MyMapper<MeterDetail> {

    /**
     * meter、meterDetail 两表查询水电费单集合(分页)
     *
     * @param  meterDetailDto
     * @return MeterDetailDtoList
     * @date   2020/6/28
     */
    List<MeterDetailDto> listMeterDetails(MeterDetailDto meterDetailDto);

    /**
     * 根据表 meterId、用户 customerId 查询未缴费记录的数量
     *
     * @param  meterDetailDto
     * @return 未缴费单的数量
     * @date   2020/6/29
     */
    List<Long> countUnPayByMeterAndCustomer(MeterDetailDto meterDetailDto);

    /**
     * 根据 meterId 查询最近的一次已交费的记录的实际值/本期指数值
     *
     * @param  meterDetailDto
     * @return 实际值/本期指数值
     * @date   2020/6/30
     */
    Long getLastAmountByMeterId(MeterDetailDto meterDetailDto);

    /**
     * 根据 meterId 查询是否有未缴费的缴费单记录(某月份)
     *
     * @param  meterDetailDto
     * @return 缴费集合
     * @date   2020/6/30
     */
    List<MeterDetailDto> listUnPayUnSubmitByMeter(MeterDetailDto meterDetailDto);

    /**
     * 根据主键 id 查询到水电费单详情以及联表查询表信息
     *
     * @param  id
     * @return MeterDetailDto
     * @date   2020/7/6
     */
    MeterDetailDto getMeterDetailById(Long id);

    /**
     * 根据 code 查询实体
     * 
     * @param  code
     * @return MeterDetailDto
     * @date   2020/7/10
     */
    MeterDetailDto getMeterDetailByCode(String code);

    /**
     * 根据 code 查询实体
     *
     * @param  meterDetailDto
     * @return MeterDetailDto
     * @date   2020/7/10
     */
    List<MeterDetailDto> listMeterDetailByUnPayBusiness(MeterDetailDto meterDetailDto);
}