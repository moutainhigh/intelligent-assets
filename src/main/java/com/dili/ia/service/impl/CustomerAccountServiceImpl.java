package com.dili.ia.service.impl;

import com.dili.ia.domain.CustomerAccount;
import com.dili.ia.mapper.CustomerAccountMapper;
import com.dili.ia.service.CustomerAccountService;
import com.dili.ss.base.BaseServiceImpl;
import com.dili.ss.dto.DTOUtils;
import com.dili.ss.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

/**
 * 由MyBatis Generator工具自动生成
 * This file was generated on 2020-02-14 10:18:23.
 */
@Service
public class CustomerAccountServiceImpl extends BaseServiceImpl<CustomerAccount, Long> implements CustomerAccountService {

    public CustomerAccountMapper getActualDao() {
        return (CustomerAccountMapper)getDao();
    }

    @Override
    public Boolean checkCustomerAccountExist(Long customerId, Long marketId) {
        CustomerAccount customerAccount = DTOUtils.newDTO(CustomerAccount.class);
        customerAccount.setCustomerId(customerId);
        customerAccount.setMarketId(marketId);
        List<CustomerAccount> list = getActualDao().select(customerAccount);
        if (CollectionUtils.isEmpty(list)){
            return false;
        }
        return true;
    }

    @Override
    public CustomerAccount getCustomerAccountByCustomerId(Long customerId, Long marketId) {
        CustomerAccount customerAccount = DTOUtils.newDTO(CustomerAccount.class);
        customerAccount.setCustomerId(customerId);
        customerAccount.setMarketId(marketId);
        List<CustomerAccount> list = getActualDao().select(customerAccount);
        if (CollectionUtils.isEmpty(list) || list.size() > 1){
            throw new BusinessException("2","当前客户账户存在异常，不存在或者同一市场存在多个相同客户账户！");
        }
       return list.get(0);
    }

    @Override
    public void subtractEarnestAvailableAndBalance(Long customerId, Long marketId, Long availableAmount, Long balanceAmount) {
        CustomerAccount customerAccount = this.getCustomerAccountByCustomerId(customerId, marketId);
        customerAccount.setEarnestAvailableBalance(customerAccount.getEarnestAvailableBalance() - availableAmount);
        customerAccount.setEarnestBalance(customerAccount.getEarnestBalance() - balanceAmount);

        Integer count = this.getActualDao().updateEarnestAccountByVersion(customerAccount);
        if (count < 1){
            throw new BusinessException("2","当前数据正已被其他用户操作，更新失败！");
        }
    }

    @Override
    public void addEarnestAvailableAndBalance(Long customerId, Long marketId, Long availableAmount, Long balanceAmount) {
        CustomerAccount customerAccount = this.getCustomerAccountByCustomerId(customerId, marketId);
        customerAccount.setEarnestAvailableBalance(customerAccount.getEarnestAvailableBalance() + availableAmount);
        customerAccount.setEarnestBalance(customerAccount.getEarnestBalance() + balanceAmount);

        Integer count = this.getActualDao().updateEarnestAccountByVersion(customerAccount);
        if (count < 1){
            throw new BusinessException("2","当前数据正已被其他用户操作，更新失败！");
        }
    }
}