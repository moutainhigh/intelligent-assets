package com.dili.ia.service.impl;

import com.dili.assets.sdk.dto.BoothDTO;
import com.dili.assets.sdk.dto.BoothRentDTO;
import com.dili.commons.glossary.EnabledStateEnum;
import com.dili.commons.glossary.YesOrNoEnum;
import com.dili.ia.domain.*;
import com.dili.ia.domain.dto.*;
import com.dili.ia.glossary.*;
import com.dili.ia.mapper.LeaseOrderMapper;
import com.dili.ia.rpc.AssetsRpc;
import com.dili.ia.rpc.CustomerRpc;
import com.dili.ia.rpc.SettlementRpc;
import com.dili.ia.rpc.UidFeignRpc;
import com.dili.ia.service.*;
import com.dili.ia.util.BeanMapUtil;
import com.dili.ia.util.ResultCodeConst;
import com.dili.settlement.domain.SettleOrder;
import com.dili.settlement.dto.SettleOrderDto;
import com.dili.settlement.enums.SettleStateEnum;
import com.dili.settlement.enums.SettleTypeEnum;
import com.dili.settlement.enums.SettleWayEnum;
import com.dili.ss.base.BaseServiceImpl;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.dto.DTOUtils;
import com.dili.ss.util.DateUtils;
import com.dili.ss.util.MoneyUtils;
import com.dili.uap.sdk.domain.Department;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.rpc.DepartmentRpc;
import com.dili.uap.sdk.session.SessionContext;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 由MyBatis Generator工具自动生成
 * This file was generated on 2020-02-11 15:54:49.
 */
@Service
public class LeaseOrderServiceImpl extends BaseServiceImpl<LeaseOrder, Long> implements LeaseOrderService {
    private final static Logger LOG = LoggerFactory.getLogger(LeaseOrderServiceImpl.class);

    public LeaseOrderMapper getActualDao() {
        return (LeaseOrderMapper) getDao();
    }

    @Autowired
    private DepartmentRpc departmentRpc;
    @Autowired
    private LeaseOrderItemService leaseOrderItemService;
    @Autowired
    private SettlementRpc settlementRpc;
    @Autowired
    private PaymentOrderService paymentOrderService;
    @Value("${settlement.app-id}")
    private Long settlementAppId;
    @Value("${settlement.handler.url}")
    private String settlerHandlerUrl;
    @Autowired
    private UidFeignRpc uidFeignRpc;
    @Autowired
    private CustomerAccountService customerAccountService;
    @Autowired
    private AssetsRpc assetsRpc;
    @Autowired
    private RefundOrderService refundOrderService;
    @Autowired
    private CustomerRpc customerRpc;

    /**
     * 摊位租赁单保存
     *
     * @param dto
     * @return
     */
    @Override
    @Transactional
    public BaseOutput saveLeaseOrder(LeaseOrderListDto dto) {
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        if (userTicket == null) {
            return BaseOutput.failure("未登录");
        }

        //检查客户状态
        checkCustomerState(dto.getCustomerId(),userTicket.getFirmId());
        dto.getLeaseOrderItems().forEach(o->{
            //检查摊位状态
            checkBoothState(o.getBoothId());
        });

        BaseOutput<Department> depOut = departmentRpc.get(userTicket.getDepartmentId());
        if (depOut.isSuccess()) {
            dto.setDepartmentName(depOut.getData().getName());
        }
        dto.setMarketId(userTicket.getFirmId());
        dto.setMarketCode(userTicket.getFirmCode());
        dto.setCreatorId(userTicket.getId());
        dto.setCreator(userTicket.getRealName());

        if (null == dto.getId()) {
            //租赁单新增
            BaseOutput<String> bizNumberOutput = uidFeignRpc.bizNumber(BizNumberTypeEnum.LEASE_ORDER.getCode());
            if (!bizNumberOutput.isSuccess()) {
                throw new RuntimeException("编号生成器微服务异常");
            }
            dto.setCode(userTicket.getFirmCode().toUpperCase() + bizNumberOutput.getData());
            dto.setState(LeaseOrderStateEnum.CREATED.getCode());
            dto.setPayState(PayStateEnum.NOT_PAID.getCode());
            dto.setWaitAmount(dto.getPayAmount());
            insertSelective(dto);
            insertLeaseOrderItems(dto);
        } else {
            //租赁单修改
            LeaseOrder oldLeaseOrder = get(dto.getId());
            dto.setVersion(oldLeaseOrder.getVersion());
            int rows = updateSelective(dto);
            if (rows == 0) {
                throw new RuntimeException("多人操作，请重试！");
            }

            LeaseOrderItem condition = DTOUtils.newInstance(LeaseOrderItem.class);
            condition.setLeaseOrderId(dto.getId());
            leaseOrderItemService.deleteByExample(condition);
            insertLeaseOrderItems(dto);
        }
        return BaseOutput.success().setData(dto);
    }

    /**
     * 批量插入租赁单项
     *
     * @param dto
     */
    private void insertLeaseOrderItems(LeaseOrderListDto dto) {
        dto.getLeaseOrderItems().forEach(o -> {
            o.setLeaseOrderId(dto.getId());
            o.setCustomerId(dto.getCustomerId());
            o.setCustomerName(dto.getCustomerName());
            o.setState(LeaseOrderStateEnum.CREATED.getCode());
            o.setDepositAmountFlag(DepositAmountFlagEnum.PRE_TRANSFER.getCode());
            o.setPayState(PayStateEnum.NOT_PAID.getCode());
            leaseOrderItemService.insertSelective(o);
        });
    }

    /**
     * 检查摊位状态
     * @param boothId
     */
    private void checkBoothState(Long boothId){
        BaseOutput<BoothDTO> output = assetsRpc.getBoothById(boothId);
        if(!output.isSuccess()){
            throw new RuntimeException("摊位接口调用异常 "+output.getMessage());
        }
        BoothDTO booth = output.getData();
        if(null == booth){
            throw new RuntimeException("摊位不存在，请核实和修改后再保存");
        }else if(EnabledStateEnum.DISABLED.getCode().equals(booth.getState())){
            throw new RuntimeException("摊位已禁用，请核实和修改后再保存");
        }else if(YesOrNoEnum.YES.getCode().equals(booth.getIsDelete())){
            throw new RuntimeException("摊位已删除，请核实和修改后再保存");
        }
    }

    /**
     * 检查客户状态
     * @param customerId
     * @param marketId
     */
    private void checkCustomerState(Long customerId,Long marketId){
        BaseOutput<Customer> output = customerRpc.get(customerId,marketId);
        if(!output.isSuccess()){
            throw new RuntimeException("客户接口调用异常 "+output.getMessage());
        }
        Customer customer = output.getData();
        if(null == customer){
            throw new RuntimeException("客户不存在，请核实和修改后再保存");
        }else if(EnabledStateEnum.DISABLED.getCode().equals(customer.getState())){
            throw new RuntimeException("客户已禁用，请核实和修改后再保存");
        }else if(YesOrNoEnum.YES.getCode().equals(customer.getIsDelete())){
            throw new RuntimeException("客户已删除，请核实和修改后再保存");
        }
    }

    /**
     * 结算成功，同步更新租赁单相关信息
     *
     * @param settleOrder
     * @return
     */
    @Override
    @Transactional
    @GlobalTransactional
    public BaseOutput<Boolean> updateLeaseOrderBySettleInfo(SettleOrder settleOrder) {
        PaymentOrder condition = DTOUtils.newInstance(PaymentOrder.class);
        condition.setCode(settleOrder.getBusinessCode());
        PaymentOrder paymentOrderPO = paymentOrderService.listByExample(condition).stream().findFirst().orElse(null);
        if (PaymentOrderStateEnum.PAID.getCode().equals(paymentOrderPO.getState())) {
            return BaseOutput.success().setData(true);
        }

        paymentOrderPO.setState(PaymentOrderStateEnum.PAID.getCode());
        paymentOrderPO.setSettlementWay(settleOrder.getWay());
        paymentOrderPO.setSettlementOperator(settleOrder.getOperatorName());
        paymentOrderPO.setPayedTime(DateUtils.localDateTimeToUdate(settleOrder.getOperateTime()));
        if (paymentOrderService.updateSelective(paymentOrderPO) == 0) {
            throw new RuntimeException("多人操作，请重试！");
        }

        //摊位租赁摊位租赁单及摊位租赁单项相关信息改动
        LeaseOrder leaseOrder = get(paymentOrderPO.getBusinessId());
        if (LeaseOrderStateEnum.SUBMITTED.getCode().equals(leaseOrder.getState())) {
            //第一笔消费抵扣保证金
            deductionLeaseOrderItemDepositAmount(leaseOrder.getId());
            //消费定金、转低
            BaseOutput customerAccountOutput = customerAccountService.paySuccessLeaseOrderCustomerAmountConsume(leaseOrder.getId(), leaseOrder.getCode(), leaseOrder.getCustomerId(), leaseOrder.getEarnestDeduction(), leaseOrder.getTransferDeduction(), leaseOrder.getDepositDeduction(), leaseOrder.getMarketId());
            if(!customerAccountOutput.isSuccess()){
                throw new RuntimeException(customerAccountOutput.getMessage());
            }
            //解冻出租摊位
            leaseBooth(leaseOrder);
        }
        if ((leaseOrder.getWaitAmount() - paymentOrderPO.getAmount()) == 0L) {
            leaseOrder.setPayState(PayStateEnum.PAID.getCode());
            Date now = new Date();
            if (now.getTime() >= leaseOrder.getStartTime().getTime() &&
                    now.getTime() <= leaseOrder.getEndTime().getTime()) {
                leaseOrder.setState(LeaseOrderStateEnum.EFFECTIVE.getCode());
            } else if (now.getTime() < leaseOrder.getStartTime().getTime()) {
                leaseOrder.setState(LeaseOrderStateEnum.NOT_ACTIVE.getCode());
            } else if (now.getTime() > leaseOrder.getEndTime().getTime()) {
                leaseOrder.setState(LeaseOrderStateEnum.EXPIRED.getCode());
            }

        }
        leaseOrder.setWaitAmount(leaseOrder.getWaitAmount() - paymentOrderPO.getAmount());
        leaseOrder.setPaidAmount(leaseOrder.getPaidAmount() + paymentOrderPO.getAmount());
        leaseOrder.setPaymentId(0L);

        //更新租赁单及子单相关状态
        if (updateSelective(leaseOrder) == 0) {
            LOG.info("摊位租赁单提交状态更新失败 乐观锁生效 【租赁单ID {}】", leaseOrder.getId());
            throw new RuntimeException("多人操作，请重试");
        }
        LeaseOrderItem leaseOrderItemCondition = DTOUtils.newInstance(LeaseOrderItem.class);
        leaseOrderItemCondition.setLeaseOrderId(leaseOrder.getId());
        List<LeaseOrderItem> leaseOrderItems = leaseOrderItemService.listByExample(leaseOrderItemCondition);
        leaseOrderItems.stream().forEach(o -> {
            o.setState(leaseOrder.getState());
            o.setPayState(leaseOrder.getPayState());
            if(PayStateEnum.PAID.getCode().equals(leaseOrder.getPayState())){
                o.setDepositAmountFlag(DepositAmountFlagEnum.TRANSFERRED.getCode());
            }
        });
        if (leaseOrderItemService.batchUpdateSelective(leaseOrderItems) != leaseOrderItems.size()) {
            throw new RuntimeException("多人操作，请重试！");
        }
        return BaseOutput.success().setData(true);
    }

    /**
     * 解冻消费摊位
     * @param leaseOrder
     */
    private void leaseBooth(LeaseOrder leaseOrder) {
        BoothRentDTO boothRentDTO = new BoothRentDTO();
        boothRentDTO.setOrderId(leaseOrder.getId().toString());
        BaseOutput assetsOutput = assetsRpc.rentBoothRent(boothRentDTO);
        if(!assetsOutput.isSuccess()){
            LOG.error("摊位解冻出租异常{}",assetsOutput.getMessage());
            throw new RuntimeException(assetsOutput.getMessage());
        }
    }

    /**
     * 提交付款
     *
     * @param id         租赁单ID
     * @param amount     交费金额
     * @param waitAmount 待缴费金额
     * @return
     */
    @Override
    @Transactional
    @GlobalTransactional
    public BaseOutput submitPayment(Long id, Long amount, Long waitAmount) {
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        if (userTicket == null) {
            throw new RuntimeException("未登录");
        }

        LeaseOrder leaseOrder = get(id);
        LeaseOrderItem condition = DTOUtils.newInstance(LeaseOrderItem.class);
        condition.setLeaseOrderId(leaseOrder.getId());
        List<LeaseOrderItem> leaseOrderItems = leaseOrderItemService.listByExample(condition);
        //检查客户状态
        checkCustomerState(leaseOrder.getCustomerId(),leaseOrder.getMarketId());
        leaseOrderItems.forEach(o->{
            //检查摊位状态
            checkBoothState(o.getBoothId());
        });
        //检查提交付款
        checkSubmitPayment(id, amount, waitAmount, leaseOrder);

        //新增缴费单
        PaymentOrder paymentOrder = buildPaymentOrder(leaseOrder);
        paymentOrder.setAmount(amount);
        paymentOrderService.insertSelective(paymentOrder);

        if (leaseOrder.getState().equals(LeaseOrderStateEnum.CREATED.getCode())) {
            //冻结保证金
            frozenLeaseOrderItemDepositAmount(id);
            //冻结定金和转低
            BaseOutput customerAccountOutput = customerAccountService.submitLeaseOrderCustomerAmountFrozen(leaseOrder.getId(), leaseOrder.getCode(), leaseOrder.getCustomerId(), leaseOrder.getEarnestDeduction(), leaseOrder.getTransferDeduction(), leaseOrder.getDepositDeduction(), leaseOrder.getMarketId());
            if(!customerAccountOutput.isSuccess()){
                if(ResultCodeConst.EARNEST_ERROR.equals(customerAccountOutput.getCode())){
                    throw new RuntimeException("客户定金可用金额不足，请核实修改后重新保存");
                }else if(ResultCodeConst.TRANSFER_ERROR.equals(customerAccountOutput.getCode())){
                    throw new RuntimeException("客户转低可用金额不足，请核实修改后重新保存");
                }else{
                    throw new RuntimeException(customerAccountOutput.getMessage());
                }
            }
            //冻结摊位
            frozenBooth(leaseOrder,leaseOrderItems);
            leaseOrder.setState(LeaseOrderStateEnum.SUBMITTED.getCode());
            leaseOrder.setPaymentId(paymentOrder.getId());
            //更新摊位租赁单状态
            cascadeUpdateLeaseOrderState(leaseOrder, true, LeaseOrderItemStateEnum.SUBMITTED);
        } else if (leaseOrder.getState().equals(LeaseOrderStateEnum.SUBMITTED.getCode())) {
            //判断缴费单是否需要撤回 需要撤回则撤回
            if (null != leaseOrder.getPaymentId() && 0 != leaseOrder.getPaymentId()) {
                withdrawPaymentOrder(leaseOrder.getPaymentId());
            }
            leaseOrder.setPaymentId(paymentOrder.getId());
            //更新摊位租赁单状态
            if (updateSelective(leaseOrder) == 0) {
                LOG.info("摊位租赁单提交状态更新失败 乐观锁生效 【租赁单ID {}】", id);
                throw new RuntimeException("摊位租赁单提交状态更新失败");
            }
        }

        //新增结算单
        SettleOrderDto settleOrder = buildSettleOrderDto(leaseOrder);
        settleOrder.setAmount(amount);
        settleOrder.setBusinessCode(paymentOrder.getCode());
        BaseOutput<SettleOrder> settlementOutput = settlementRpc.submit(settleOrder);
        if (settlementOutput.isSuccess()) {
            //冗余结算编号 另起事务使其不影响原有事务
            try {
                saveSettlementCode(paymentOrder.getId(), settlementOutput.getData().getCode());
            } catch (Exception e) {
                LOG.error("结算编号冗余异常 租赁单【code:{}】缴费单【code:{}】 异常信息{}", leaseOrder.getCode(), paymentOrder.getCode(), e.getMessage());
            }
        } else {
            throw new RuntimeException(settlementOutput.getMessage());
        }
        return settlementOutput;
    }

    /**
     * 冻结摊位
     * @param leaseOrderItems
     */
    private void frozenBooth(LeaseOrder leaseOrder,List<LeaseOrderItem> leaseOrderItems) {
        leaseOrderItems.forEach(o->{
            BoothRentDTO boothRentDTO = new BoothRentDTO();
            boothRentDTO.setBoothId(o.getBoothId());
            boothRentDTO.setStart(leaseOrder.getStartTime());
            boothRentDTO.setEnd(leaseOrder.getEndTime());
            boothRentDTO.setOrderId(leaseOrder.getId().toString());
            BaseOutput assetsOutput = assetsRpc.addBoothRent(boothRentDTO);
            if(!assetsOutput.isSuccess()){
                if(assetsOutput.getCode().equals("2500")){
                    throw new RuntimeException(o.getBoothName()+"选择的时间期限重复，请修改后重新保存");
                }else{
                    throw new RuntimeException(assetsOutput.getMessage());
                }
            }
        });
    }

    /**
     * 解冻摊位
     * @param leaseOrder
     */
    private void unFrozenBooth(LeaseOrder leaseOrder) {
        BoothRentDTO boothRentDTO = new BoothRentDTO();
        boothRentDTO.setOrderId(leaseOrder.getId().toString());
        BaseOutput assetsOutput = assetsRpc.deleteBoothRent(boothRentDTO);
        if(!assetsOutput.isSuccess()){
            LOG.error("摊位解冻异常{}",assetsOutput.getMessage());
            throw new RuntimeException(assetsOutput.getMessage());
        }
    }

    /**
     * 检查是否可以进行提交付款
     * @param id
     * @param amount
     * @param waitAmount
     * @param leaseOrder
     */
    private void checkSubmitPayment(Long id, Long amount, Long waitAmount, LeaseOrder leaseOrder) {

        //提交付款条件：已创建状态或已提交状态
        if (!LeaseOrderStateEnum.CREATED.getCode().equals(leaseOrder.getState()) &&
                !LeaseOrderStateEnum.SUBMITTED.getCode().equals(leaseOrder.getState())) {
            String stateName = LeaseOrderStateEnum.getLeaseOrderStateEnum(leaseOrder.getState()).getName();
            LOG.info("租赁单编号【{}】 状态为【{}】，不可以进行提交付款操作", leaseOrder.getCode(), stateName);
            throw new RuntimeException("租赁单状态为【" + stateName + "】，不可以进行提交付款操作");
        }
        if (leaseOrder.getWaitAmount().equals(0L)) {
            throw new RuntimeException("摊位租赁单费用已结清");
        }
        if (amount > leaseOrder.getWaitAmount()) {
            LOG.info("摊位租赁单【ID {}】 支付金额【{}】大于待付金额【{}】", id, amount, leaseOrder.getWaitAmount());
            throw new RuntimeException("支付金额大于待付金额");
        }
        if (!waitAmount.equals(leaseOrder.getWaitAmount())) {
            LOG.info("摊位租赁单待缴费金额已发生变更，请重试【ID {}】 旧金额【{}】新金额【{}】", id, waitAmount, leaseOrder.getWaitAmount());
            throw new RuntimeException("摊位租赁单待缴费金额已发生变更，请重试");
        }
    }

    /**
     * 摊位租赁单保证金抵扣
     *
     * @param id
     */
    private void deductionLeaseOrderItemDepositAmount(Long id) {
        LeaseOrderItem condition = DTOUtils.newInstance(LeaseOrderItem.class);
        condition.setLeaseOrderId(id);
        List<LeaseOrderItem> leaseOrderItems = leaseOrderItemService.listByExample(condition);
        List<Long> depositAmountSourceIds = leaseOrderItems.stream().map(LeaseOrderItem::getDepositAmountSourceId).collect(Collectors.toList());

        LeaseOrderItemListDto depositAmountSourceCondition = DTOUtils.newInstance(LeaseOrderItemListDto.class);
        depositAmountSourceCondition.setIds(depositAmountSourceIds);
        List<LeaseOrderItem> depositAmountSourceItems = leaseOrderItemService.listByExample(depositAmountSourceCondition);
        depositAmountSourceItems.stream().forEach(o -> {
            if (DepositAmountFlagEnum.FROZEN.getCode().equals(o.getDepositAmountFlag())) {
                o.setDepositAmountFlag(DepositAmountFlagEnum.DEDUCTION.getCode());
            } else {
                LOG.info("{}保证金已被抵扣,不可重复操作", o.getBoothName());
                throw new RuntimeException(o.getBoothName() + "保证金已被抵扣,不可重复操作");
            }
        });
        if (leaseOrderItemService.batchUpdateSelective(depositAmountSourceItems) != depositAmountSourceItems.size()) {
            LOG.info("源摊位租赁单项抵扣失败 【租赁单id={}】", id);
            throw new RuntimeException("多人操作，请重试！");
        }

    }

    /**
     * 摊位租赁单保证金冻结
     *
     * @param id
     */
    private void frozenLeaseOrderItemDepositAmount(Long id) {
        LeaseOrderItem condition = DTOUtils.newInstance(LeaseOrderItem.class);
        condition.setLeaseOrderId(id);
        List<LeaseOrderItem> leaseOrderItems = leaseOrderItemService.listByExample(condition);
        List<Long> depositAmountSourceIds = leaseOrderItems.stream().map(LeaseOrderItem::getDepositAmountSourceId).collect(Collectors.toList());

        LeaseOrderItemListDto depositAmountSourceCondition = DTOUtils.newInstance(LeaseOrderItemListDto.class);
        depositAmountSourceCondition.setIds(depositAmountSourceIds);
        List<LeaseOrderItem> depositAmountSourceItems = leaseOrderItemService.listByExample(depositAmountSourceCondition);
        depositAmountSourceItems.stream().forEach(o -> {
            //保证金为已转入且退款待申请且 费用已交清 方可冻结抵扣
            if (DepositAmountFlagEnum.TRANSFERRED.getCode().equals(o.getDepositAmountFlag())
                    && RefundStateEnum.WAIT_APPLY.getCode().equals(o.getRefundState())
                    && PayStateEnum.PAID.getCode().equals(o.getPayState())) {
                o.setDepositAmountFlag(DepositAmountFlagEnum.FROZEN.getCode());
            } else {
                String operateName = DepositAmountFlagEnum.getDepositAmountFlagEnum(o.getDepositAmountFlag()).getOperateName();
                LOG.info("{}保证金已被{},保证金总抵扣额已发生变化不可进行抵扣，请修改后重试", o.getBoothName(), operateName);
                throw new RuntimeException(o.getBoothName() + "保证金已被" + operateName + ",保证金总抵扣额已发生变化不可进行抵扣，请修改后重试");
            }
        });
        if (leaseOrderItemService.batchUpdateSelective(depositAmountSourceItems) != depositAmountSourceItems.size()) {
            LOG.info("源摊位租赁单项冻结失败 【租赁单id={}】", id);
            throw new RuntimeException("多人操作，请重试！");
        }

    }

    /**
     * 摊位租赁单保证金解冻
     *
     * @param id
     */
    private void unFrozenLeaseOrderItemDepositAmount(Long id) {
        LeaseOrderItem condition = DTOUtils.newInstance(LeaseOrderItem.class);
        condition.setLeaseOrderId(id);
        List<LeaseOrderItem> leaseOrderItems = leaseOrderItemService.listByExample(condition);
        List<Long> depositAmountSourceIds = leaseOrderItems.stream().map(LeaseOrderItem::getDepositAmountSourceId).collect(Collectors.toList());

        LeaseOrderItemListDto depositAmountSourceCondition = DTOUtils.newInstance(LeaseOrderItemListDto.class);
        depositAmountSourceCondition.setIds(depositAmountSourceIds);
        List<LeaseOrderItem> depositAmountSourceItems = leaseOrderItemService.listByExample(depositAmountSourceCondition);
        depositAmountSourceItems.stream().forEach(o -> {
            if (DepositAmountFlagEnum.FROZEN.getCode().equals(o.getDepositAmountFlag())) {
                o.setDepositAmountFlag(DepositAmountFlagEnum.TRANSFERRED.getCode());
            } else {
                String operateName = DepositAmountFlagEnum.getDepositAmountFlagEnum(o.getDepositAmountFlag()).getOperateName();
                LOG.info("{}保证金已被解冻,不可重复操作", o.getBoothName());
                throw new RuntimeException(o.getBoothName() + "保证金已被解冻,不可重复操作");
            }
        });
        if (leaseOrderItemService.batchUpdateSelective(depositAmountSourceItems) != depositAmountSourceItems.size()) {
            LOG.info("源摊位租赁单项解冻失败 【租赁单id={}】", id);
            throw new RuntimeException("多人操作，请重试！");
        }

    }

    /**
     * 级联更新摊位租赁订单状态 订单项状态级联发生变化
     *
     * @param leaseOrder
     * @param isCascade  false不级联更新订单项 true 级联更新订单项
     * @param stateEnum  isCascade为false时，此处可以传null
     */
    @Transactional
    public void cascadeUpdateLeaseOrderState(LeaseOrder leaseOrder, boolean isCascade, LeaseOrderItemStateEnum stateEnum) {
        if (updateSelective(leaseOrder) == 0) {
            LOG.info("摊位租赁单提交状态更新失败 乐观锁生效 【租赁单ID {}】", leaseOrder.getId());
            throw new RuntimeException("多人操作，请重试");
        }

        if (isCascade) {
            LeaseOrderItem condition = DTOUtils.newInstance(LeaseOrderItem.class);
            condition.setLeaseOrderId(leaseOrder.getId());
            List<LeaseOrderItem> leaseOrderItems = leaseOrderItemService.listByExample(condition);
            leaseOrderItems.stream().forEach(o -> o.setState(stateEnum.getCode()));
            if (leaseOrderItemService.batchUpdateSelective(leaseOrderItems) != leaseOrderItems.size()) {
                throw new RuntimeException("多人操作，请重试！");
            }
        }
    }

    /**
     * 冗余结算编号到缴费单
     *
     * @param paymentId
     * @param settlementCode
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveSettlementCode(Long paymentId, String settlementCode) {
        PaymentOrder paymentOrderPo = paymentOrderService.get(paymentId);
        paymentOrderPo.setSettlementCode(settlementCode);
        paymentOrderService.updateSelective(paymentOrderPo);
    }

    /**
     * 构建缴费单数据
     *
     * @param leaseOrder
     * @return
     */
    private PaymentOrder buildPaymentOrder(LeaseOrder leaseOrder) {
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        if (userTicket == null) {
            throw new RuntimeException("未登录");
        }
        PaymentOrder paymentOrder = DTOUtils.newInstance(PaymentOrder.class);
        BaseOutput<String> bizNumberOutput = uidFeignRpc.bizNumber(BizNumberTypeEnum.PAYMENT_ORDER.getCode());
        if (!bizNumberOutput.isSuccess()) {
            throw new RuntimeException("编号生成器微服务异常");
        }
        paymentOrder.setCode(userTicket.getFirmCode().toUpperCase() + bizNumberOutput.getData());
        paymentOrder.setBusinessCode(leaseOrder.getCode());
        paymentOrder.setBusinessId(leaseOrder.getId());
        paymentOrder.setMarketId(userTicket.getFirmId());
        paymentOrder.setMarketCode(userTicket.getFirmCode());
        paymentOrder.setCreatorId(userTicket.getId());
        paymentOrder.setCreator(userTicket.getRealName());
        paymentOrder.setState(PaymentOrderStateEnum.NOT_PAID.getCode());
        paymentOrder.setBizType(BizTypeEnum.BOOTH_LEASE.getCode());
        return paymentOrder;
    }

    /**
     * 构造结算单数据
     *
     * @param leaseOrder
     * @return
     */
    private SettleOrderDto buildSettleOrderDto(LeaseOrder leaseOrder) {
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        if (userTicket == null) {
            throw new RuntimeException("未登录");
        }
        SettleOrderDto settleOrder = new SettleOrderDto();
        settleOrder.setAppId(settlementAppId);
        settleOrder.setBusinessCode(leaseOrder.getCode());
        settleOrder.setBusinessDepId(leaseOrder.getDepartmentId());
        settleOrder.setBusinessDepName(leaseOrder.getDepartmentName());
        settleOrder.setBusinessType(BizTypeEnum.BOOTH_LEASE.getCode());
        settleOrder.setCustomerId(leaseOrder.getCustomerId());
        settleOrder.setCustomerName(leaseOrder.getCustomerName());
        settleOrder.setCustomerPhone(leaseOrder.getCustomerCellphone());
        settleOrder.setMarketId(userTicket.getFirmId());
        settleOrder.setMarketCode(userTicket.getFirmCode());
        settleOrder.setReturnUrl(settlerHandlerUrl);
        settleOrder.setSubmitterDepId(userTicket.getDepartmentId());
        settleOrder.setSubmitterDepName(null == userTicket.getDepartmentId() ? null : departmentRpc.get(userTicket.getDepartmentId()).getData().getName());
        settleOrder.setSubmitterId(userTicket.getId());
        settleOrder.setSubmitterName(userTicket.getRealName());
        settleOrder.setSubmitTime(LocalDateTime.now());
        settleOrder.setType(SettleTypeEnum.PAY.getCode());
        settleOrder.setState(SettleStateEnum.WAIT_DEAL.getCode());
        return settleOrder;
    }

    /**
     * 取消摊位租赁订单
     *
     * @param id
     * @return
     */
    @Override
    @Transactional
    public BaseOutput cancelOrder(Long id) {
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        if (userTicket == null) {
            return BaseOutput.failure("未登录");
        }
        LeaseOrder leaseOrder = get(id);
        if (!LeaseOrderStateEnum.CREATED.getCode().equals(leaseOrder.getState())) {
            String stateName = LeaseOrderStateEnum.getLeaseOrderStateEnum(leaseOrder.getState()).getName();
            LOG.info("租赁单【code:{}】状态为【{}】，不可以进行取消操作", leaseOrder.getCode(), stateName);
            throw new RuntimeException("租赁单状态为【" + stateName + "】，不可以进行取消操作");
        }
        leaseOrder.setState(LeaseOrderStateEnum.CANCELD.getCode());
        leaseOrder.setCancelerId(userTicket.getId());
        leaseOrder.setCanceler(userTicket.getRealName());

        //联动摊位租赁单项状态 取消
        cascadeUpdateLeaseOrderState(leaseOrder, true, LeaseOrderItemStateEnum.CANCELD);

        return BaseOutput.success();
    }

    /**
     * 撤回摊位租赁订单
     *
     * @param id
     * @return
     */
    @Transactional
    @GlobalTransactional
    @Override
    public BaseOutput withdrawOrder(Long id) {
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        if (userTicket == null) {
            return BaseOutput.failure("未登录");
        }
        LeaseOrder leaseOrder = get(id);
        if (!LeaseOrderStateEnum.SUBMITTED.getCode().equals(leaseOrder.getState())) {
            String stateName = LeaseOrderStateEnum.getLeaseOrderStateEnum(leaseOrder.getState()).getName();
            LOG.info("租赁单【code:{}】状态为【{}】，不可以进行撤回操作", leaseOrder.getCode(), stateName);
            throw new RuntimeException("租赁单状态为【" + stateName + "】，不可以进行撤回操作");
        }
        if (null != leaseOrder.getPaymentId() && 0L != leaseOrder.getPaymentId()) {
            withdrawPaymentOrder(leaseOrder.getPaymentId());
            leaseOrder.setPaymentId(0L);
        }
        leaseOrder.setState(LeaseOrderStateEnum.CREATED.getCode());
        cascadeUpdateLeaseOrderState(leaseOrder, true, LeaseOrderItemStateEnum.CREATED);

        //解冻摊位保证金
        unFrozenLeaseOrderItemDepositAmount(id);
        //解冻定金、转抵
        BaseOutput customerAccountOutput = customerAccountService.withdrawLeaseOrderCustomerAmountUnFrozen(leaseOrder.getId(), leaseOrder.getCode(), leaseOrder.getCustomerId(), leaseOrder.getEarnestDeduction(), leaseOrder.getTransferDeduction(), leaseOrder.getDepositDeduction(), leaseOrder.getMarketId());
        if(!customerAccountOutput.isSuccess()){
            throw new RuntimeException(customerAccountOutput.getMessage());
        }
        //解冻摊位
        unFrozenBooth(leaseOrder);

        return BaseOutput.success();
    }

    /**
     * 撤回缴费单 判断缴费单是否需要撤回 需要撤回则撤回
     * 如果撤回时发现缴费单状态为及时同步结算状态 则抛出异常 提示用户带结算同步后再操作
     *
     * @param paymentId
     */
    @GlobalTransactional
    private void withdrawPaymentOrder(Long paymentId) {
        PaymentOrder payingOrder = paymentOrderService.get(paymentId);
        if (PaymentOrderStateEnum.NOT_PAID.getCode().equals(payingOrder.getState())) {
            String paymentCode = payingOrder.getCode();
            BaseOutput<SettleOrder> settleOrderBaseOutput = settlementRpc.get(settlementAppId,paymentCode);
            if (!settleOrderBaseOutput.isSuccess()) {
                LOG.info("结算单查询异常 【缴费单CODE {}】", paymentCode);
                throw new RuntimeException("结算单查询异常");
            }
            SettleOrder settleOrder = settleOrderBaseOutput.getData();
            //缴费单对应的结算单未处理
            if (settleOrder.getState().equals(SettleStateEnum.WAIT_DEAL.getCode())) {
                if (!settlementRpc.cancel(settlementAppId,paymentCode).isSuccess()) {
                    LOG.info("结算单撤回异常 【缴费单CODE {}】", paymentCode);
                    throw new RuntimeException("结算单撤回异常");
                }
            } else {
                String stateName = SettleStateEnum.getNameByCode(settleOrder.getState());
                throw new RuntimeException("状态已发生变更，目前状态【" + stateName + "】不能进行撤回，等结算数据同步后再操作");
            }
        }
    }

    /**
     * 扫描待生效的订单，做生效处理
     *
     * @return
     */
    @Override
    public BaseOutput<Boolean> scanNotActiveLeaseOrder() {
        while (true) {
            LeaseOrderListDto condition = DTOUtils.newInstance(LeaseOrderListDto.class);
            condition.setStartTimeLT(new Date());
            condition.setState(LeaseOrderStateEnum.NOT_ACTIVE.getCode());
            condition.setRows(100);
            condition.setPage(1);
            List<LeaseOrder> leaseOrders = listByExample(condition);
            if (CollectionUtils.isEmpty(leaseOrders)) {
                break;
            }

            leaseOrders.stream().forEach(o -> {
                try {
                    leaseOrderEffectiveHandler(o);
                } catch (Exception e) {
                    LOG.error("租赁单【code:{}】变更生效异常。{}", o.getCode(), e.getMessage());
                    LOG.error("租赁单变更生效异常", e);
                }
            });
        }
        return BaseOutput.success().setData(true);
    }

    /**
     * 租赁单生效处理
     *
     * @param o
     */
    @Transactional
    public void leaseOrderEffectiveHandler(LeaseOrder o) {
        o.setState(LeaseOrderStateEnum.EFFECTIVE.getCode());
        if (updateSelective(o) == 0) {
            LOG.info("摊位租赁单提交状态更新失败 乐观锁生效 【租赁单ID {}】", o.getId());
            throw new RuntimeException("多人操作，请重试");
        }

        LeaseOrderItem itemCondition = DTOUtils.newInstance(LeaseOrderItem.class);
        itemCondition.setLeaseOrderId(o.getId());
        List<LeaseOrderItem> leaseOrderItems = leaseOrderItemService.listByExample(itemCondition);

        List<LeaseOrderItem> waitItems = new ArrayList<>();
        leaseOrderItems.stream().forEach(leaseOrderItem -> {
            //只更新待生效的订单项
            if (LeaseOrderItemStateEnum.NOT_ACTIVE.getCode().equals(leaseOrderItem.getState())) {
                leaseOrderItem.setState(LeaseOrderItemStateEnum.EFFECTIVE.getCode());
                waitItems.add(leaseOrderItem);
            }
        });
        if (leaseOrderItemService.batchUpdateSelective(waitItems) != waitItems.size()) {
            throw new RuntimeException("多人操作，请重试！");
        }
    }

    /**
     * 扫描待到期的订单，做到期处理
     *
     * @return
     */
    @Override
    public BaseOutput<Boolean> scanExpiredLeaseOrder() {
        while (true) {
            LeaseOrderListDto condition = DTOUtils.newInstance(LeaseOrderListDto.class);
            condition.setEndTimeLT(new Date());
            condition.setState(LeaseOrderStateEnum.EFFECTIVE.getCode());
            condition.setRows(100);
            condition.setPage(1);
            List<LeaseOrder> leaseOrders = listByExample(condition);
            if (CollectionUtils.isEmpty(leaseOrders)) {
                break;
            }

            leaseOrders.stream().forEach(o -> {
                try {
                    leaseOrderExpiredHandler(o);
                } catch (Exception e) {
                    LOG.error("租赁单【code:{}】变更到期异常。{}", o.getCode(), e.getMessage());
                    LOG.error("租赁单变更到期异常", e);
                }
            });
        }
        return BaseOutput.success().setData(true);
    }

    /**
     * 租赁单到期处理
     *
     * @param o
     */
    @Transactional
    public void leaseOrderExpiredHandler(LeaseOrder o) {
        o.setState(LeaseOrderStateEnum.EXPIRED.getCode());
        if (updateSelective(o) == 0) {
            LOG.info("摊位租赁单提交状态更新失败 乐观锁生效 【租赁单ID {}】", o.getId());
            throw new RuntimeException("多人操作，请重试");
        }

        LeaseOrderItem itemCondition = DTOUtils.newInstance(LeaseOrderItem.class);
        itemCondition.setLeaseOrderId(o.getId());
        List<LeaseOrderItem> leaseOrderItems = leaseOrderItemService.listByExample(itemCondition);

        List<LeaseOrderItem> waitItems = new ArrayList<>();
        leaseOrderItems.stream().forEach(leaseOrderItem -> {
            //只更新待到期的订单项
            if (LeaseOrderItemStateEnum.EFFECTIVE.getCode().equals(leaseOrderItem.getState())) {
                leaseOrderItem.setState(LeaseOrderItemStateEnum.EXPIRED.getCode());
                waitItems.add(leaseOrderItem);
            }
        });
        if (leaseOrderItemService.batchUpdateSelective(waitItems) != waitItems.size()) {
            throw new RuntimeException("多人操作，请重试！");
        }
    }

    @Override
    public BaseOutput<PrintDataDto> queryPrintData(String businessCode, Integer reprint) {
        PaymentOrder paymentOrderCondition = DTOUtils.newInstance(PaymentOrder.class);
        paymentOrderCondition.setCode(businessCode);
        PaymentOrder paymentOrder = paymentOrderService.list(paymentOrderCondition).stream().findFirst().orElse(null);
        if (null == paymentOrder) {
            throw new RuntimeException("businessCode无效");
        }
        if (!PaymentOrderStateEnum.PAID.getCode().equals(paymentOrder.getState())) {
            return BaseOutput.failure("此单未支付");
        }

        LeaseOrder leaseOrder = get(paymentOrder.getBusinessId());
        PrintDataDto printDataDto = new PrintDataDto();
        LeaseOrderPrintDto leaseOrderPrintDto = new LeaseOrderPrintDto();
        leaseOrderPrintDto.setPrintTime(new Date());
        leaseOrderPrintDto.setReprint(reprint == 2 ? "(补打)" : "");
        leaseOrderPrintDto.setLeaseOrderCode(leaseOrder.getCode());
        if (PayStateEnum.PAID.getCode().equals(leaseOrder.getPayState())) {
            leaseOrderPrintDto.setBusinessType(BizTypeEnum.BOOTH_LEASE.getName());
            printDataDto.setName(PrintTemplateEnum.BOOTH_LEASE_PAID.getCode());
        } else {
            leaseOrderPrintDto.setBusinessType(BizTypeEnum.EARNEST.getName());
            printDataDto.setName(PrintTemplateEnum.BOOTH_LEASE_NOT_PAID.getCode());
        }
        leaseOrderPrintDto.setCustomerName(leaseOrder.getCustomerName());
        leaseOrderPrintDto.setCustomerCellphone(leaseOrder.getCustomerCellphone());
        leaseOrderPrintDto.setStartTime(leaseOrder.getStartTime());
        leaseOrderPrintDto.setEndTime(leaseOrder.getEndTime());
        leaseOrderPrintDto.setIsRenew(IsRenewEnum.getIsRenewEnum(leaseOrder.getIsRenew()).getName());
        leaseOrderPrintDto.setCategoryName(leaseOrder.getCategoryName());
        leaseOrderPrintDto.setNotes(leaseOrder.getNotes());
        leaseOrderPrintDto.setTotalAmount(MoneyUtils.centToYuan(leaseOrder.getTotalAmount()));
        leaseOrderPrintDto.setDepositDeduction(MoneyUtils.centToYuan(leaseOrder.getDepositDeduction()));

        PaymentOrder paymentOrderConditions = DTOUtils.newInstance(PaymentOrder.class);
        paymentOrderConditions.setBusinessId(paymentOrder.getBusinessId());
        List<PaymentOrder> paymentOrders = paymentOrderService.list(paymentOrderConditions);
        Long totalPayAmountExcludeLast = 0L;
        for (PaymentOrder order : paymentOrders) {
            if (!order.getCode().equals(businessCode) && order.getState().equals(PaymentOrderStateEnum.PAID.getCode())) {
                totalPayAmountExcludeLast += order.getAmount();
            }
        }
        //除最后一次所交费用+定金抵扣 之和未总定金
        leaseOrderPrintDto.setEarnestDeduction(MoneyUtils.centToYuan(leaseOrder.getEarnestDeduction() + totalPayAmountExcludeLast));
        leaseOrderPrintDto.setTransferDeduction(MoneyUtils.centToYuan(leaseOrder.getTransferDeduction()));
        leaseOrderPrintDto.setPayAmount(MoneyUtils.centToYuan(leaseOrder.getPayAmount()));
        leaseOrderPrintDto.setAmount(MoneyUtils.centToYuan(paymentOrder.getAmount()));
        leaseOrderPrintDto.setSettlementWay(SettleWayEnum.getNameByCode(paymentOrder.getSettlementWay()));
        leaseOrderPrintDto.setSettlementOperator(paymentOrder.getSettlementOperator());
        leaseOrderPrintDto.setSubmitter(paymentOrder.getCreator());

        LeaseOrderItem leaseOrderItemCondition = DTOUtils.newInstance(LeaseOrderItem.class);
        leaseOrderItemCondition.setLeaseOrderId(leaseOrder.getId());
        List<LeaseOrderItemPrintDto> leaseOrderItemPrintDtos = new ArrayList<>();
        leaseOrderItemService.list(leaseOrderItemCondition).forEach(o -> {
            leaseOrderItemPrintDtos.add(LeaseOrderRefundOrderServiceImpl.leaseOrderItem2PrintDto(o));
        });
        leaseOrderPrintDto.setLeaseOrderItems(leaseOrderItemPrintDtos);
        printDataDto.setItem(BeanMapUtil.beanToMap(leaseOrderPrintDto));
        return BaseOutput.success().setData(printDataDto);
    }

    @Autowired
    private TransferDeductionItemService transferDeductionItemService;

    @Override
    @Transactional
    public BaseOutput createRefundOrder(RefundOrderDto refundOrderDto) {
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        if (userTicket == null) {
            return BaseOutput.failure("未登录");
        }
        if(null == refundOrderDto.getOrderItemId()){
            //主单上退款申请
            LeaseOrder leaseOrder = get(refundOrderDto.getOrderId());
            LeaseOrderItem condition = DTOUtils.newInstance(LeaseOrderItem.class);
            condition.setLeaseOrderId(leaseOrder.getId());
            List<LeaseOrderItem> leaseOrderItems = leaseOrderItemService.listByExample(condition);

            if(!RefundStateEnum.WAIT_APPLY.getCode().equals(leaseOrder.getRefundState())){
                throw new RuntimeException("租赁单状态已发生变更，不能发起退款申请");
            }
            if(PayStateEnum.PAID.getCode().equals(leaseOrder.getPayState())){
                throw new RuntimeException("租赁单费用已交清不能，只能在租赁摊位上进行退款");
            }
            //退款总金额不能大于未交清可退金额 （已交金额+所有抵扣项）
            if (refundOrderDto.getTotalRefundAmount() > (leaseOrder.getPaidAmount() + leaseOrder.getDepositDeduction() + leaseOrder.getEarnestDeduction() + leaseOrder.getTransferDeduction())) {
                throw new RuntimeException("退款总金额不能大于可退金额");
            }
            leaseOrder.setRefundState(RefundStateEnum.REFUNDING.getCode());
            leaseOrder.setRefundAmount(refundOrderDto.getTotalRefundAmount());
            if(updateSelective(leaseOrder) == 0){
                throw new RuntimeException("多人操作，请重试！");
            }

            //级联订单项退款状态
            leaseOrderItems.stream().forEach(o -> o.setRefundState(RefundStateEnum.REFUNDING.getCode()));
            if (leaseOrderItemService.batchUpdateSelective(leaseOrderItems) != leaseOrderItems.size()) {
                throw new RuntimeException("多人操作，请重试！");
            }
        }else{
            //订单项退款申请
            LeaseOrderItem leaseOrderItem = leaseOrderItemService.get(refundOrderDto.getOrderItemId());
            //已到期或已停租状态才能发起退款申请
            if (LeaseOrderItemStateEnum.EXPIRED.getCode().equals(leaseOrderItem.getState()) && LeaseOrderItemStateEnum.RENTED_OUT.getCode().equals(leaseOrderItem.getState())) {
                throw new RuntimeException("摊位项状态已发生变更，不能发起退款申请");
            }
            if(!RefundStateEnum.WAIT_APPLY.getCode().equals(leaseOrderItem.getRefundState())){
                throw new RuntimeException("摊位项状态已发生变更，不能发起退款申请");
            }
            //保证金已转入状态才可退
            if(refundOrderDto.getDepositRefundAmount() > 0 && !leaseOrderItem.getDepositAmountFlag().equals(DepositAmountFlagEnum.TRANSFERRED.getCode())){
                throw new RuntimeException("摊位保证金状态已发生变更，不能进行退款，请修改");
            }
            if(refundOrderDto.getRentRefundAmount() > leaseOrderItem.getRentAmount()){
                throw new RuntimeException("租金退款额大于可退款额");
            }
            if(refundOrderDto.getDepositRefundAmount() > leaseOrderItem.getDepositAmount()){
                throw new RuntimeException("保证金退款额大于可退款额");
            }
            if(refundOrderDto.getManageRefundAmount() > leaseOrderItem.getManageAmount()){
                throw new RuntimeException("物管费退款额大于可退款额");
            }
            if(!refundOrderDto.getTotalRefundAmount().equals(refundOrderDto.getRentRefundAmount() + refundOrderDto.getDepositRefundAmount() + refundOrderDto.getManageRefundAmount())){
                throw new RuntimeException("退款金额分配错误，请重新修改再保存");
            }

            leaseOrderItem.setRefundState(RefundStateEnum.REFUNDING.getCode());
            leaseOrderItem.setRefundAmount(refundOrderDto.getTotalRefundAmount());
            leaseOrderItem.setDepositRefundAmount(refundOrderDto.getDepositRefundAmount());
            leaseOrderItem.setRentRefundAmount(refundOrderDto.getRentRefundAmount());
            leaseOrderItem.setManageRefundAmount(refundOrderDto.getManageRefundAmount());
            if(leaseOrderItemService.updateSelective(leaseOrderItem) == 0){
                throw new RuntimeException("多人操作，请重试！");
            }
        }

        refundOrderDto.setBizType(BizTypeEnum.BOOTH_LEASE.getCode());
        BaseOutput<String> bizNumberOutput = uidFeignRpc.bizNumber(BizNumberTypeEnum.LEASE_REFUND_ORDER.getCode());
        if (!bizNumberOutput.isSuccess()) {
            throw new RuntimeException("编号生成器微服务异常");
        }
        refundOrderDto.setCode(userTicket.getFirmCode().toUpperCase() + bizNumberOutput.getData());
        if(!refundOrderService.doAddHandler(refundOrderDto).isSuccess()){
            throw new RuntimeException("退款单新增异常");
        }

        if(CollectionUtils.isNotEmpty(refundOrderDto.getTransferDeductionItems())){
            refundOrderDto.getTransferDeductionItems().forEach(o->{
                o.setRefundOrderId(refundOrderDto.getId());
                transferDeductionItemService.insertSelective(o);
            });
        }
        return BaseOutput.success();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BaseOutput cancelRefundOrderHandler(Long leaseOrderId,Long leaseOrderItemId) {
        if(null == leaseOrderItemId){
            LeaseOrder leaseOrder = get(leaseOrderId);
            if(!RefundStateEnum.REFUNDING.getCode().equals(leaseOrder.getRefundState())){
                throw new RuntimeException("退款状态已发生变更，不能取消退款");
            }
            leaseOrder.setRefundState(RefundStateEnum.WAIT_APPLY.getCode());
            leaseOrder.setRefundAmount(0L);
            if(updateSelective(leaseOrder) == 0){
                throw new RuntimeException("多人操作，请重试！");
            }

            //级联订单项退款状态
            LeaseOrderItem condition = DTOUtils.newInstance(LeaseOrderItem.class);
            condition.setLeaseOrderId(leaseOrder.getId());
            List<LeaseOrderItem> leaseOrderItems = leaseOrderItemService.listByExample(condition);
            leaseOrderItems.stream().forEach(o -> o.setRefundState(RefundStateEnum.WAIT_APPLY.getCode()));
            if (leaseOrderItemService.batchUpdateSelective(leaseOrderItems) != leaseOrderItems.size()) {
                throw new RuntimeException("多人操作，请重试！");
            }
        }else{
            //订单项退款申请
            LeaseOrderItem leaseOrderItem = leaseOrderItemService.get(leaseOrderItemId);
            if(!RefundStateEnum.REFUNDING.getCode().equals(leaseOrderItem.getRefundState())){
                throw new RuntimeException("退款状态已发生变更，不能取消退款");
            }

            leaseOrderItem.setRefundState(RefundStateEnum.WAIT_APPLY.getCode());
            leaseOrderItem.setRefundAmount(0L);
            leaseOrderItem.setDepositRefundAmount(0L);
            leaseOrderItem.setRentRefundAmount(0L);
            leaseOrderItem.setManageRefundAmount(0L);
            if(leaseOrderItemService.updateSelective(leaseOrderItem) == 0){
                throw new RuntimeException("多人操作，请重试！");
            }
        }
        return BaseOutput.success();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BaseOutput settleSuccessRefundOrderHandler(RefundOrder refundOrder) {
        LeaseOrder leaseOrder = get(refundOrder.getOrderId());
        if(null == refundOrder.getOrderItemId()){
            if(RefundStateEnum.REFUNDED.getCode().equals(leaseOrder.getRefundState())){
                LOG.info("此单已退款【leaseOrderId={}】",refundOrder.getOrderId());
                return BaseOutput.success();
            }
            leaseOrder.setRefundState(RefundStateEnum.REFUNDED.getCode());
            leaseOrder.setState(LeaseOrderStateEnum.REFUNDED.getCode());
            if(updateSelective(leaseOrder) == 0){
                throw new RuntimeException("多人操作，请重试！");
            }

            //级联订单项退款状态
            LeaseOrderItem condition = DTOUtils.newInstance(LeaseOrderItem.class);
            condition.setLeaseOrderId(leaseOrder.getId());
            List<LeaseOrderItem> leaseOrderItems = leaseOrderItemService.listByExample(condition);
            leaseOrderItems.stream().forEach(o -> {
                o.setRefundState(RefundStateEnum.REFUNDED.getCode());
                o.setState(LeaseOrderItemStateEnum.REFUNDED.getCode());
            });
            if (leaseOrderItemService.batchUpdateSelective(leaseOrderItems) != leaseOrderItems.size()) {
                throw new RuntimeException("多人操作，请重试！");
            }
        }else{
            //订单项退款申请
            LeaseOrderItem leaseOrderItem = leaseOrderItemService.get(refundOrder.getOrderItemId());
            if(RefundStateEnum.REFUNDED.getCode().equals(leaseOrderItem.getRefundState())){
                LOG.info("此单已退款【leaseOrderItemId={}】",refundOrder.getOrderItemId());
                return BaseOutput.success();
            }
            leaseOrderItem.setRefundState(RefundStateEnum.REFUNDED.getCode());

            if(leaseOrderItemService.updateSelective(leaseOrderItem) == 0){
                throw new RuntimeException("多人操作，请重试！");
            }

            //级联检查其他订单项状态，如果全部为已退款，则需联动更新订单状态为已退款
            LeaseOrderItem condition = DTOUtils.newInstance(LeaseOrderItem.class);
            condition.setLeaseOrderId(leaseOrder.getId());
            List<LeaseOrderItem> leaseOrderItems = leaseOrderItemService.listByExample(condition);
            boolean isUpdateLeaseOrderState = true;
            for (LeaseOrderItem orderItem : leaseOrderItems) {
                if (orderItem.getId().equals(refundOrder.getOrderItemId())) {
                    continue;
                } else if (LeaseOrderItemStateEnum.REFUNDED.getCode().equals(orderItem.getState())) {
                    isUpdateLeaseOrderState = false;
                    break;
                }
            }
            if(isUpdateLeaseOrderState){
                leaseOrder.setState(LeaseOrderStateEnum.REFUNDED.getCode());
                if(updateSelective(leaseOrder) == 0){
                    throw new RuntimeException("多人操作，请重试！");
                }
            }
        }

        //转抵扣充值
        TransferDeductionItem transferDeductionItemCondition = DTOUtils.newInstance(TransferDeductionItem.class);
        transferDeductionItemCondition.setRefundOrderId(refundOrder.getId());
        List<TransferDeductionItem> transferDeductionItems = transferDeductionItemService.list(transferDeductionItemCondition);
        if(CollectionUtils.isNotEmpty(transferDeductionItems)){
            transferDeductionItems.forEach(o->{
                BaseOutput accountOutput = customerAccountService.leaseOrderRechargTransfer(refundOrder.getId(),refundOrder.getCode(),refundOrder.getCustomerId(),o.getPayeeAmount(),refundOrder.getMarketId());
                if(!accountOutput.isSuccess()){
                    LOG.error("退款单转低异常，【退款编号:{},收款人:{},收款金额:{},msg:{}】",refundOrder.getCode(),o.getPayee(),o.getPayeeAmount(),accountOutput.getMessage());
                    throw new RuntimeException(accountOutput.getMessage());
                }
            });
        }
        return BaseOutput.success();
    }
}