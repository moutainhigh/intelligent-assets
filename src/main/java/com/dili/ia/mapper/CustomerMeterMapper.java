package com.dili.ia.mapper;

import com.dili.ia.domain.CustomerMeter;
import com.dili.ia.domain.dto.CustomerMeterDto;
import com.dili.ss.base.MyMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CustomerMeterMapper extends MyMapper<CustomerMeter> {

    /**
     * 根据主键 id 查询表用户关系
     *
     * @param  id
     * @return CustomerMeterDto
     * @date   2020/6/29
     */
    CustomerMeterDto getMeterById(Long id);

    /**
     * meter、customerMeter 查询表用户关系的集合(分页)
     *
     * @param  customerMeterDto
     * @return CustomerMeterDtoList
     * @date   2020/6/17
     */
    List<CustomerMeterDto> listCustomerMeters(CustomerMeterDto customerMeterDto);


    /**
     * 根据表主键 meterId 获取表绑定的用户信息
     *
     * @param  meterId
     * @param  state
     * @return CustomerMeter
     * @date   2020/6/28
     */
    CustomerMeter getBindInfoByMeterId(@Param("meterId") Long meterId, @Param("state") Integer state);

    /**
     * 根据表编号模糊查询表客户信息列表
     *
     *
     * @param  customerMeterDto
     * @return CustomerMeterDtoList
     * @date   2020/7/10
     */
    List<CustomerMeterDto> listCustomerMetersByLikeName(CustomerMeterDto customerMeterDto);

}