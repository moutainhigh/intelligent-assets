package com.dili.ia.service;

import com.dili.ia.domain.DepositOrder;
import com.dili.ia.domain.RefundOrder;
import com.dili.ia.domain.dto.DepositOrderQuery;
import com.dili.ia.domain.dto.PrintDataDto;
import com.dili.settlement.domain.SettleOrder;
import com.dili.ss.base.BaseService;
import com.dili.ss.domain.BaseOutput;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 由MyBatis Generator工具自动生成
 * This file was generated on 2020-05-22 17:54:56.
 */
public interface DepositOrderService extends BaseService<DepositOrder, Long> {
    /**
     * 新增保证金，需要先检查客户账户是否存在
     * */
    BaseOutput<DepositOrder> addDepositOrder(DepositOrderQuery depositOrderQuery);

    /**
     * 保证金 --修改
     * @param depositOrder 修改对象
     * @return BaseOutput
     * */
    BaseOutput<DepositOrder> updateDepositOrder(DepositOrder depositOrder);

    /**
     * 保证金 --提交
     * @param id 保证金单ID
     * @param amount  本次提交付款金额
     * @param waitAmount  提交付款时候的待付金额
     * @return BaseOutput
     * */
    BaseOutput<DepositOrder> submitDepositOrder(Long id, Long amount, Long waitAmount);
    /**
     * 保证金 --撤回
     * @param depositOrderId 保证金单ID
     * @return BaseOutput
     * */
    BaseOutput<DepositOrder> withdrawDepositOrder(Long depositOrderId);

    /**
     * 保证金 --缴费成功回调
     * @param settleOrder 结算单
     * @return BaseOutput
     * */
    BaseOutput<DepositOrder> paySuccessHandler(SettleOrder settleOrder);

    /**
     * 保证金 --创建退款单
     * @param refundOrder 退款单
     * @return BaseOutput
     * */
    BaseOutput<RefundOrder> addRefundOrder(RefundOrder refundOrder);
    /**
     * 保证金 --结算退款成功回调
     * @param refundOrder 退款单
     * @return BaseOutput
     * */
    BaseOutput refundSuccessHandler(RefundOrder refundOrder);

    /**
     * 保证金票据打印数据加载
     * @param orderCode 订单号
     * @param reprint 是否补打标记
     * @return BaseOutput<PrintDataDto>
     */
    BaseOutput<PrintDataDto> queryPrintData(String orderCode, Integer reprint);
    /**
     * 保证金余额列表，维度 【按照客户-保证金类型-编号统计】
     * @param depositOrder 订单
     * @return List<DepositOrder>
     */
    List<DepositOrder> selectBalanceList(DepositOrder depositOrder);
    /**
     * 保证金余额记录条数 ，维度 【按照客户-保证金类型-编号统计】
     * @param depositOrder 订单
     * @return Integer
     */
    Integer countBalanceList(DepositOrder depositOrder);
    /**
     * 余额合计
     * @param depositOrder 订单
     * @return Long
     */
    Long sumBalance(DepositOrder depositOrder);
}