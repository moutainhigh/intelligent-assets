package com.dili.ia.mapper;

import com.dili.ia.domain.CustomerMeter;
import com.dili.ia.domain.Meter;
import com.dili.ia.domain.dto.CustomerMeterDto;
import com.dili.ss.base.MyMapper;

import java.util.List;

public interface CustomerMeterMapper extends MyMapper<CustomerMeter> {

    /**
     * @author:      xiaosa
     * @date:        2020/6/16
     * @param:       customerMeterDto
     * @return:      BaseOutput
     * @description：根据主键 id 查询
     */
    CustomerMeterDto getMeterById(Long id);

    /**
     * @author:      xiaosa
     * @date:        2020/6/16
     * @param:       customerMeterDto
     * @return:      BaseOutput
     * @description：查询列表
     */
    List<CustomerMeterDto> listCustomerMeters(CustomerMeterDto customerMeterDto);

    /**
     * @author:      xiaosa
     * @date:        2020/6/16
     * @param        type
     * @return       BaseOutput
     * @description：根据表类型获取未绑定的表编号
     */
    List<Meter> getUnbindMeterByType(Long type);
}