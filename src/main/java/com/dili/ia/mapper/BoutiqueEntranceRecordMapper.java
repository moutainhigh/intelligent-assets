package com.dili.ia.mapper;

import com.dili.ia.domain.BoutiqueEntranceRecord;
import com.dili.ia.domain.dto.BoutiqueEntranceRecordDto;
import com.dili.ia.domain.dto.BoutiqueFeeOrderDto;
import com.dili.ss.base.MyMapper;

import java.util.List;

public interface BoutiqueEntranceRecordMapper extends MyMapper<BoutiqueEntranceRecord> {


    /**
     * 根据code获取相关信息
     *
     * @param  code
     * @return BoutiqueFeeOrderDto
     * @date   2020/7/23
     */
    BoutiqueFeeOrderDto getBoutiqueAndOrderByCode(String code);

    /**
     * 列表查询
     * 
     * @param  boutiqueDto
     * @return list
     * @date   2020/8/17
     */
    List<BoutiqueEntranceRecordDto> listBoutiques(BoutiqueEntranceRecordDto boutiqueDto);
}