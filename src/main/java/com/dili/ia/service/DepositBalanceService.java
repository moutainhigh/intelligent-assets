package com.dili.ia.service;

import com.dili.ia.domain.DepositBalance;
import com.dili.ia.domain.dto.DepositBalanceQuery;
import com.dili.ss.base.BaseService;

/**
 * 由MyBatis Generator工具自动生成
 * This file was generated on 2020-06-29 15:15:26.
 */
public interface DepositBalanceService extends BaseService<DepositBalance, Long> {
    /**
     * 余额合计
     * @param depositBalanceQuery 余额
     * @return Long
     */
    Long sumBalance(DepositBalanceQuery depositBalanceQuery);
}