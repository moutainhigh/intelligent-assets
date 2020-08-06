package com.dili.ia.service.impl;

import com.dili.assets.sdk.dto.AssetsDTO;
import com.dili.assets.sdk.rpc.AssetsRpc;
import com.dili.commons.glossary.EnabledStateEnum;
import com.dili.commons.glossary.YesOrNoEnum;
import com.dili.ia.domain.*;
import com.dili.ia.domain.dto.DepositRefundOrderDto;
import com.dili.ia.domain.dto.PrintDataDto;
import com.dili.ia.domain.dto.printDto.DepositOrderPrintDto;
import com.dili.ia.glossary.*;
import com.dili.ia.mapper.DepositOrderMapper;
import com.dili.ia.rpc.CustomerRpc;
import com.dili.ia.rpc.SettlementRpc;
import com.dili.ia.rpc.UidFeignRpc;
import com.dili.ia.service.*;
import com.dili.ia.util.BeanMapUtil;
import com.dili.ia.util.LogBizTypeConst;
import com.dili.logger.sdk.component.MsgService;
import com.dili.logger.sdk.domain.BusinessLog;
import com.dili.settlement.domain.SettleOrder;
import com.dili.settlement.domain.SettleWayDetail;
import com.dili.settlement.dto.SettleOrderDto;
import com.dili.settlement.enums.SettleStateEnum;
import com.dili.settlement.enums.SettleTypeEnum;
import com.dili.settlement.enums.SettleWayEnum;
import com.dili.ss.base.BaseServiceImpl;
import com.dili.ss.constant.ResultCode;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.exception.BusinessException;
import com.dili.ss.util.MoneyUtils;
import com.dili.uap.sdk.domain.Department;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.rpc.DepartmentRpc;
import com.dili.uap.sdk.session.SessionContext;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 由MyBatis Generator工具自动生成
 * This file was generated on 2020-05-22 17:54:56.
 */
@Service
public class DepositOrderServiceImpl extends BaseServiceImpl<DepositOrder, Long> implements DepositOrderService {

    private static final Logger LOG = LoggerFactory.getLogger(DepositOrderServiceImpl.class);
    @Autowired
    DepartmentRpc departmentRpc;
    @Autowired
    MsgService msgService;
    @Autowired
    CustomerRpc customerRpc;
    @Autowired
    AssetsRpc assetsRpc;
    @Autowired
    SettlementRpc settlementRpc;
    @Autowired
    PaymentOrderService paymentOrderService;
    @Autowired
    UidFeignRpc uidFeignRpc;
    @Autowired
    RefundOrderService refundOrderService;
    @Autowired
    DepositBalanceService depositBalanceService;
    @Autowired
    TransferDeductionItemService transferDeductionItemService;
    @Autowired
    CustomerAccountService customerAccountService;

    public DepositOrderMapper getActualDao() {
        return (DepositOrderMapper)getDao();
    }

    @Value("${settlement.app-id}")
    private Long settlementAppId;
    @Value("${depositOrder.settlement.handler.url}")
    private String settlerHandlerUrl;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseOutput<DepositOrder> addDepositOrder(DepositOrder depositOrder) {
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        if(null == userTicket){
            return BaseOutput.failure("未登录");
        }
        //检查参数
        BaseOutput checkOut = checkparams(depositOrder);
        if (!checkOut.isSuccess()){
            return checkOut;
        }
        //检查客户状态
        checkCustomerState(depositOrder.getCustomerId(),userTicket.getFirmId());
        //检查摊位状态 @TODO 检查公寓，冷库状态
        if(AssetsTypeEnum.BOOTH.getCode().equals(depositOrder.getAssetsType())){
            checkBoothState(depositOrder.getAssetsId());
        }
        BaseOutput<Department> depOut = departmentRpc.get(depositOrder.getDepartmentId());
        if(!depOut.isSuccess()){
            LOGGER.info("获取部门失败！" + depOut.getMessage());
            throw new BusinessException(ResultCode.DATA_ERROR, "获取部门失败！");
        }

        depositOrder.setCode(userTicket.getFirmCode().toUpperCase() + this.getBizNumber(userTicket.getFirmCode() + "_" + BizNumberTypeEnum.DEPOSIT_ORDER.getCode()));
        depositOrder.setCreatorId(userTicket.getId());
        depositOrder.setCreator(userTicket.getRealName());
        depositOrder.setMarketId(userTicket.getFirmId());
        depositOrder.setMarketCode(userTicket.getFirmCode());
        depositOrder.setDepartmentName(depOut.getData().getName());
        depositOrder.setState(DepositOrderStateEnum.CREATED.getCode());
        depositOrder.setPayState(DepositPayStateEnum.UNPAID.getCode());
        depositOrder.setRefundState(DepositRefundStateEnum.NO_REFUNDED.getCode());
        depositOrder.setIsImport(YesOrNoEnum.NO.getCode());
        depositOrder.setWaitAmount(depositOrder.getAmount());

        this.insertSelective(depositOrder);
        return BaseOutput.success().setData(depositOrder);
    }
    private BaseOutput<Object> checkparams(DepositOrder depositOrder){
        if (depositOrder.getCustomerId() == null){
            return BaseOutput.failure(ResultCode.PARAMS_ERROR, "客户Id不能为空");
        }
        if (depositOrder.getCustomerName() == null){
            return BaseOutput.failure(ResultCode.PARAMS_ERROR, "客户名称不能为空");
        }
        if (depositOrder.getCertificateNumber() == null){
            return BaseOutput.failure(ResultCode.PARAMS_ERROR, "客户证件号不能为空");
        }
        if (depositOrder.getCustomerCellphone() == null){
            return BaseOutput.failure(ResultCode.PARAMS_ERROR, "客户电话不能为空");
        }
        if (depositOrder.getDepartmentId() == null){
            return BaseOutput.failure(ResultCode.PARAMS_ERROR, "业务所属部门ID不能为空");
        }
        if (depositOrder.getTypeCode() == null){
            return BaseOutput.failure(ResultCode.PARAMS_ERROR, "保证金类型不能为空");
        }
        if (depositOrder.getTypeName() == null){
            return BaseOutput.failure(ResultCode.PARAMS_ERROR, "保证金类型名称不能为空");
        }
        if (depositOrder.getAssetsType() == null){
            return BaseOutput.failure(ResultCode.PARAMS_ERROR, "资产类型不能为空");
        }
        if (!depositOrder.getAssetsType().equals(AssetsTypeEnum.OTHER.getCode()) && depositOrder.getAssetsId() == null){
            return BaseOutput.failure(ResultCode.PARAMS_ERROR, "资产ID不能为空");
        }
        if (!depositOrder.getAssetsType().equals(AssetsTypeEnum.OTHER.getCode()) && depositOrder.getAssetsName() == null){
            return BaseOutput.failure(ResultCode.PARAMS_ERROR, "资产名称不能为空");
        }
        if (depositOrder.getAmount() == null){
            return BaseOutput.failure(ResultCode.PARAMS_ERROR, "保证金金额不能为空");
        }
        return BaseOutput.success();
    }

    private String getBizNumber(String type){
        BaseOutput<String> bizNumberOutput = uidFeignRpc.bizNumber(type);
        if(!bizNumberOutput.isSuccess()){
            LOGGER.info("编号生成失败!" + bizNumberOutput.getMessage());
            throw new BusinessException(ResultCode.DATA_ERROR, "编号生成失败!");
        }
        if (bizNumberOutput.getData() == null){
            LOGGER.info("未获取到有效编号！检查是否配置编号类型type{}" + bizNumberOutput.getMessage(), type);
            throw new BusinessException(ResultCode.DATA_ERROR, "未获取到有效编号！"+ bizNumberOutput.getMessage());
        }

        return bizNumberOutput.getData();
    }

    /**
     * 检查摊位状态
     * @param boothId
     */
    private void checkBoothState(Long boothId){
        BaseOutput<AssetsDTO> output = assetsRpc.getBoothById(boothId);
        if(!output.isSuccess()){
            throw new BusinessException(ResultCode.DATA_ERROR, "摊位接口调用异常 "+output.getMessage());
        }
        AssetsDTO booth = output.getData();
        if(null == booth){
            throw new BusinessException(ResultCode.DATA_ERROR, "摊位不存在，请核实和修改后再保存");
        }else if(EnabledStateEnum.DISABLED.getCode().equals(booth.getState())){
            throw new BusinessException(ResultCode.DATA_ERROR, "摊位已禁用，请核实和修改后再保存");
        }else if(YesOrNoEnum.YES.getCode().equals(booth.getIsDelete())){
            throw new BusinessException(ResultCode.DATA_ERROR, "摊位已删除，请核实和修改后再保存");
        }
    }

    /**
     * 检查客户状态
     * @param customerId
     * @param marketId
     */
    @Override
    public void checkCustomerState(Long customerId,Long marketId){
        BaseOutput<Customer> output = customerRpc.get(customerId,marketId);
        if(!output.isSuccess()){
            throw new BusinessException(ResultCode.DATA_ERROR, "客户接口调用异常 "+output.getMessage());
        }
        Customer customer = output.getData();
        if(null == customer){
            throw new BusinessException(ResultCode.DATA_ERROR, "客户不存在，请核实和修改后再保存");
        }else if(EnabledStateEnum.DISABLED.getCode().equals(customer.getState())){
            throw new BusinessException(ResultCode.DATA_ERROR, "客户已禁用，请核实和修改后再保存");
        }else if(YesOrNoEnum.YES.getCode().equals(customer.getIsDelete())){
            throw new BusinessException(ResultCode.DATA_ERROR, "客户已删除，请核实和修改后再保存");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseOutput<DepositOrder> updateDepositOrder(DepositOrder depositOrder) {
        DepositOrder oldDTO = this.get(depositOrder.getId());
        if (null == oldDTO || !oldDTO.getState().equals(DepositOrderStateEnum.CREATED.getCode())){
            return BaseOutput.failure("修改失败，保证金单状态已变更！");
        }
        //修改有清空修改，所以使用update
        if (this.update(this.buildUpdateDto(oldDTO, depositOrder)) == 0){
            LOG.info("修改保证金单失败,乐观锁生效【客户名称：{}】 【保证金单ID:{}】", depositOrder.getCustomerName(), depositOrder.getId());
            throw new BusinessException(ResultCode.DATA_ERROR, "多人操作，请重试！");
        }
        return BaseOutput.success("修改成功！").setData(depositOrder);
    }

    private DepositOrder buildUpdateDto(DepositOrder oldDto, DepositOrder dto){
        BaseOutput<Department> depOut = departmentRpc.get(dto.getDepartmentId());
        if(!depOut.isSuccess()){
            LOGGER.info("获取部门失败！" + depOut.getMessage());
            throw new BusinessException(ResultCode.DATA_ERROR, "获取部门失败！");
        }
        oldDto.setDepartmentName(depOut.getData().getName());
        oldDto.setTypeCode(dto.getTypeCode());
        oldDto.setTypeName(dto.getTypeName());
        oldDto.setAssetsId(dto.getAssetsId());
        oldDto.setAssetsName(dto.getAssetsName());
        oldDto.setAssetsType(dto.getAssetsType());
        oldDto.setCustomerId(dto.getCustomerId());
        oldDto.setCustomerName(dto.getCustomerName());
        oldDto.setCertificateNumber(dto.getCertificateNumber());
        oldDto.setCustomerCellphone(dto.getCustomerCellphone());
        oldDto.setDepartmentId(dto.getDepartmentId());
        oldDto.setAmount(dto.getAmount());
        oldDto.setNotes(dto.getNotes());
        oldDto.setModifyTime(LocalDateTime.now());
        oldDto.setVersion(dto.getVersion());

        return oldDto;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseOutput<DepositOrder> submitDepositOrder(Long id, Long amount, Long waitAmount) {
        DepositOrder de = this.get(id);
        if (null == de){
            LOG.info("提交失败，没有查询到保证金！id={}", id);
            return BaseOutput.failure("提交失败，没有查询到保证金单！");
        }
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        if(null == userTicket){
            return BaseOutput.failure("未登录");
        }
        //检查客户状态
        checkCustomerState(de.getCustomerId(),de.getMarketId());
        //检查摊位状态 @TODO 检查公寓，冷库状态
        if(AssetsTypeEnum.BOOTH.getCode().equals(de.getAssetsType())){
            checkBoothState(de.getAssetsId());
        }
        //检查是否可以进行提交付款
        checkSubmitPayment(id, amount, waitAmount, de);
        //首次提交更改状态为 -- > 已提交
        if (de.getState().equals(DepositOrderStateEnum.CREATED.getCode())){
            de.setState(DepositOrderStateEnum.SUBMITTED.getCode());
            if (this.updateSelective(de) == 0) {
                LOG.info("提交保证金【修改保证金单状态】失败 ,乐观锁生效！【保证金单ID:{}】", de.getId());
                throw new BusinessException(ResultCode.DATA_ERROR, "多人操作，请重试！");
            }
        }else {//非第一次付款，相关业务实现---直接撤回已提交的未支付的缴费单然后创建新的缴费单。
            //判断缴费单是否需要撤回 需要撤回则撤回
            PaymentOrder oldPb = this.findPaymentOrder(userTicket.getFirmId(),PaymentOrderStateEnum.NOT_PAID.getCode(), de.getId(), de.getCode());
            if (null != oldPb){
                withdrawPaymentOrder(oldPb);
            }
        }
        PaymentOrder pb = this.buildPaymentOrder(userTicket, de, amount);
        paymentOrderService.insertSelective(pb);

        //提交到结算中心 --- 执行顺序不可调整！！因为异常只能回滚自己系统，无法回滚其它远程系统
        BaseOutput<SettleOrder> out= settlementRpc.submit(buildSettleOrderDto(userTicket, de, pb, amount));
        if (!out.isSuccess()){
            LOGGER.info("提交到结算中心失败！" + out.getMessage() + out.getErrorData());
            throw new BusinessException(ResultCode.DATA_ERROR, out.getMessage());
        }
        return BaseOutput.success().setData(de);
    }

    //组装缴费单 PaymentOrder
    private PaymentOrder buildPaymentOrder(UserTicket userTicket, DepositOrder depositOrder, Long paidAmount){
        PaymentOrder pb = new PaymentOrder();
        pb.setCode(userTicket.getFirmCode().toUpperCase() + this.getBizNumber(BizNumberTypeEnum.PAYMENT_ORDER.getCode()));
        pb.setAmount(paidAmount);
        pb.setBusinessId(depositOrder.getId());
        pb.setBusinessCode(depositOrder.getCode());
        pb.setCreatorId(userTicket.getId());
        pb.setCreator(userTicket.getRealName());
        pb.setMarketId(userTicket.getFirmId());
        pb.setMarketCode(userTicket.getFirmCode());
        pb.setBizType(BizTypeEnum.DEPOSIT_ORDER.getCode());
        pb.setState(PayStateEnum.NOT_PAID.getCode());
        pb.setVersion(0);
        return pb;
    }
    //组装 -- 结算中心缴费单 SettleOrder
    private SettleOrderDto buildSettleOrderDto(UserTicket userTicket,DepositOrder depositOrder, PaymentOrder paymentOrder, Long paidAmount){
        SettleOrderDto settleOrder = new SettleOrderDto();
        //以下是提交到结算中心的必填字段
        settleOrder.setMarketId(depositOrder.getMarketId()); //市场ID
        settleOrder.setMarketCode(userTicket.getFirmCode());
        settleOrder.setOrderCode(paymentOrder.getCode());//订单号 唯一
        settleOrder.setBusinessCode(paymentOrder.getBusinessCode()); //缴费单业务单号
        settleOrder.setCustomerId(depositOrder.getCustomerId());//客户ID
        settleOrder.setCustomerName(depositOrder.getCustomerName());// "客户姓名
        settleOrder.setCustomerPhone(depositOrder.getCustomerCellphone());//"客户手机号
        settleOrder.setAmount(paidAmount); //金额
        settleOrder.setBusinessDepId(depositOrder.getDepartmentId()); //"业务部门ID
        settleOrder.setBusinessDepName(departmentRpc.get(depositOrder.getDepartmentId()).getData().getName());//"业务部门名称
        settleOrder.setSubmitterId(userTicket.getId());// "提交人ID
        settleOrder.setSubmitterName(userTicket.getRealName());// "提交人姓名
        if (userTicket.getDepartmentId() != null){
            settleOrder.setSubmitterDepId(userTicket.getDepartmentId()); //"提交人部门ID
            settleOrder.setSubmitterDepName(departmentRpc.get(userTicket.getDepartmentId()).getData().getName());
        }
        settleOrder.setSubmitTime(LocalDateTime.now());
        settleOrder.setAppId(settlementAppId);//应用ID
        //@TODO 结算单需要调整类型，为String
        settleOrder.setBusinessType(Integer.valueOf(BizTypeEnum.DEPOSIT_ORDER.getCode())); // 业务类型
        settleOrder.setType(SettleTypeEnum.PAY.getCode());// "结算类型  -- 付款
        settleOrder.setState(SettleStateEnum.WAIT_DEAL.getCode());
        settleOrder.setReturnUrl(settlerHandlerUrl); // 结算-- 缴费成功后回调路径

        return settleOrder;
    }

    /**
     * 检查是否可以进行提交付款
     * @param id 保证金单ID
     * @param amount 保证金单付款金额
     * @param waitAmount 保证金单待付金额
     * @param depositOrder 原来保证金单
     */
    private void checkSubmitPayment(Long id, Long amount, Long waitAmount,DepositOrder depositOrder) {
        //提交付款条件：已交清或退款中、已退款不能进行提交付款操作
        if (DepositPayStateEnum.PAID.getCode().equals(depositOrder.getPayState())) {
            LOG.info("保证金单编号【{}】 已交清，不可以进行提交付款操作", depositOrder.getCode());
            throw new BusinessException(ResultCode.DATA_ERROR, "保证金单编号【" + depositOrder.getCode() + "】 已交清，不可以进行提交付款操作");
        }
        if(!DepositRefundStateEnum.NO_REFUNDED.getCode().equals(depositOrder.getRefundState())){
            LOG.info("保证金单编号【{}】已发起退款，不可以进行提交付款操作", depositOrder.getCode());
            throw new BusinessException(ResultCode.DATA_ERROR, "保证金单编号【" + depositOrder.getCode() + "】 已发起退款，不可以进行提交付款操作");
        }
        if(DepositOrderStateEnum.CANCELD.getCode().equals(depositOrder.getState())){
            LOG.info("保证金单编号【{}】已取消，不可以进行提交付款操作", depositOrder.getCode());
            throw new BusinessException(ResultCode.DATA_ERROR, "保证金单编号【" + depositOrder.getCode() + "】 已取消，不可以进行提交付款操作");
        }
        if (!amount.equals(0L) && waitAmount.equals(0L)) {
            throw new BusinessException(ResultCode.DATA_ERROR,"保证金单费用已结清");
        }
        if (amount > depositOrder.getWaitAmount()) {
            LOG.info("保证金单【ID {}】 支付金额【{}】大于待付金额【{}】", id, amount, depositOrder.getWaitAmount());
            throw new BusinessException(ResultCode.DATA_ERROR,"支付金额大于待付金额");
        }
        if (!waitAmount.equals(depositOrder.getWaitAmount())) {
            LOG.info("保证金单待缴费金额已发生变更，请重试【ID {}】 旧金额【{}】新金额【{}】", id, waitAmount, depositOrder.getWaitAmount());
            throw new BusinessException(ResultCode.DATA_ERROR,"保证金单待缴费金额已发生变更，请重试");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseOutput<DepositOrder> withdrawDepositOrder(Long depositOrderId) {
        //改状态，删除缴费单，通知撤回结算中心缴费单
        DepositOrder depositOrder = this.get(depositOrderId);
        if (null == depositOrder || !depositOrder.getState().equals(DepositOrderStateEnum.SUBMITTED.getCode())){
            return BaseOutput.failure("撤回失败，状态已变更！");
        }
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        if (null == userTicket){
            return BaseOutput.failure("未登录！");
        }
        depositOrder.setState(DepositOrderStateEnum.CREATED.getCode());
        depositOrder.setWithdrawOperatorId(userTicket.getId());
        depositOrder.setWithdrawOperator(userTicket.getRealName());
        if (this.updateSelective(depositOrder) == 0) {
            LOG.info("撤回保证金【修改保证金单状态】失败,乐观锁生效。【保证金单ID：】" + depositOrderId);
            throw new BusinessException(ResultCode.DATA_ERROR, "多人操作，请重试！");
        }

        PaymentOrder pb = this.findPaymentOrder(userTicket.getFirmId(),PaymentOrderStateEnum.NOT_PAID.getCode(), depositOrder.getId(), depositOrder.getCode());
        withdrawPaymentOrder(pb);
        return BaseOutput.success().setData(depositOrder);
    }

    /**
     * 撤回缴费单 判断缴费单是否需要撤回 需要撤回则撤回
     *
     * @param payingOrder
     */
    public void withdrawPaymentOrder(PaymentOrder payingOrder) {
        if (null == payingOrder) {
            LOG.info("没有查询到付款单PaymentOrder");
            throw new BusinessException(ResultCode.DATA_ERROR, "没有查询到付款单！");
        }
        if (PaymentOrderStateEnum.NOT_PAID.getCode().equals(payingOrder.getState())) {
            String paymentCode = payingOrder.getCode();
            BaseOutput output = settlementRpc.cancel(settlementAppId,paymentCode);
            if (!output.isSuccess()) {
                LOG.info("结算单撤回异常 【缴费单CODE {}】", paymentCode);
                throw new BusinessException(ResultCode.DATA_ERROR,output.getMessage());
            }

            payingOrder.setState(PaymentOrderStateEnum.CANCEL.getCode());
            if(paymentOrderService.updateSelective(payingOrder) == 0){
                LOG.info("撤回缴费单异常，乐观锁生效，【缴费单编号:{}】",payingOrder.getCode());
                throw new BusinessException(ResultCode.DATA_ERROR,"多人操作，请重试！");
            }
        }
    }

    private PaymentOrder findPaymentOrder(Long marketId, Integer state, Long businessId, String businessCode){
        PaymentOrder pb = new PaymentOrder();
        pb.setBizType(BizTypeEnum.DEPOSIT_ORDER.getCode());
        pb.setBusinessId(businessId);
        pb.setBusinessCode(businessCode);
        pb.setMarketId(marketId);
        pb.setState(state);
        PaymentOrder order = paymentOrderService.listByExample(pb).stream().findFirst().orElse(null);
        return order;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseOutput<RefundOrder> saveOrUpdateRefundOrder(DepositRefundOrderDto depositRefundOrderDto) {
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        if (null == userTicket){
            return BaseOutput.failure("未登录！");
        }
        PaymentOrder paymentOrder = this.findPaymentOrder(userTicket.getFirmId(), PaymentOrderStateEnum.NOT_PAID.getCode(), depositRefundOrderDto.getBusinessId(), depositRefundOrderDto.getBusinessCode());
        if (paymentOrder != null){
            withdrawPaymentOrder(paymentOrder);
        }
        //检查客户状态
        checkCustomerState(depositRefundOrderDto.getPayeeId(), userTicket.getFirmId());
        DepositOrder depositOrder = this.get(depositRefundOrderDto.getBusinessId());
        if (null == depositOrder){
            LOG.info("保证金退款申请，保证金单【ID:{}】不存在！", depositRefundOrderDto.getBusinessId());
            return BaseOutput.failure("保证金业务单不存在！");
        }
        if (DepositPayStateEnum.UNPAID.getCode().equals(depositOrder.getPayState())){
            return BaseOutput.failure("创建失败，未交费业务单不能退款！");
        }
        Long totalRefundAmount = depositRefundOrderDto.getPayeeAmount() + depositOrder.getRefundAmount();
        if (depositOrder.getPaidAmount() < totalRefundAmount){
            return BaseOutput.failure("退款金额不能大于订单已交费金额！");
        }

        //新增
        if(null == depositRefundOrderDto.getId()){
            if (DepositOrderStateEnum.REFUNDING.getCode().equals(depositOrder.getState())){
                return BaseOutput.failure("创建失败，已存在退款中的业务单！");
            }
            if (DepositRefundStateEnum.REFUNDED.getCode().equals(depositOrder.getRefundState())){
                return BaseOutput.failure("创建失败，业务单已全额退款！");
            }
            depositOrder.setState(DepositOrderStateEnum.REFUNDING.getCode());
            if (this.updateSelective(depositOrder) == 0) {
                LOG.info("撤回保证金【修改保证金单状态】失败,乐观锁生效。【保证金单ID：】" + depositOrder.getId());
                throw new BusinessException(ResultCode.DATA_ERROR, "多人操作，请重试！");
            }
            depositRefundOrderDto.setCode(userTicket.getFirmCode().toUpperCase() + this.getBizNumber(userTicket.getFirmCode() + "_" + BizNumberTypeEnum.DEPOSIT_REFUND_ORDER.getCode()));
            depositRefundOrderDto.setBizType(BizTypeEnum.DEPOSIT_ORDER.getCode());
            BaseOutput output = refundOrderService.doAddHandler(depositRefundOrderDto);
            if (!output.isSuccess()) {
                LOG.info("租赁单【编号：{}】退款申请接口异常", depositRefundOrderDto.getBusinessCode());
                throw new BusinessException(ResultCode.DATA_ERROR, "退款申请接口异常");
            }
        }else { // 修改
            if (!refundOrderService.doUpdateDispatcher(depositRefundOrderDto).isSuccess()) {
                LOG.info("租赁单【编号：{}】退款修改接口异常", depositRefundOrderDto.getBusinessCode());
                throw new BusinessException(ResultCode.DATA_ERROR, "退款修改接口异常");
            }
            //删除转抵扣项的数据
            TransferDeductionItem transferDeductionItemCondition = new TransferDeductionItem();
            transferDeductionItemCondition.setRefundOrderId(depositRefundOrderDto.getId());
            transferDeductionItemService.deleteByExample(transferDeductionItemCondition);
        }

        if (CollectionUtils.isNotEmpty(depositRefundOrderDto.getTransferDeductionItems())) {
            depositRefundOrderDto.getTransferDeductionItems().forEach(o -> {
                o.setRefundOrderId(depositRefundOrderDto.getId());
                transferDeductionItemService.insertSelective(o);
            });
        }
        return BaseOutput.success().setData(depositRefundOrderDto);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseOutput<DepositOrder> paySuccessHandler(SettleOrder settleOrder) {
        if (null == settleOrder){
            return BaseOutput.failure("回调参数为空！");
        }
        PaymentOrder condition = new PaymentOrder();
        //结算单code唯一
        condition.setCode(settleOrder.getOrderCode());
        condition.setBizType(BizTypeEnum.DEPOSIT_ORDER.getCode());
        PaymentOrder paymentOrderPO = paymentOrderService.listByExample(condition).stream().findFirst().orElse(null);
        if (paymentOrderPO == null){
            LOG.info("缴费单异常，没有找到缴费单：{}, bizType={}",settleOrder.getOrderCode(),BizTypeEnum.DEPOSIT_ORDER.getCode());
            return BaseOutput.failure("缴费单异常，没有找到缴费单：" + settleOrder.getOrderCode());
        }
        DepositOrder depositOrder = this.get(paymentOrderPO.getBusinessId());
        if (PaymentOrderStateEnum.PAID.getCode().equals(paymentOrderPO.getState())) { //如果已支付，直接返回
            return BaseOutput.success().setData(depositOrder);
        }
        if (!paymentOrderPO.getState().equals(PaymentOrderStateEnum.NOT_PAID.getCode())){
            LOG.info("缴费单状态已变更！状态为：" + PaymentOrderStateEnum.getPaymentOrderStateEnum(paymentOrderPO.getState()).getName() );
            return BaseOutput.failure("缴费单状态已变更！");
        }
        Long paidAmount = depositOrder.getPaidAmount() + paymentOrderPO.getAmount();
        Long waitAmount = depositOrder.getWaitAmount() - paymentOrderPO.getAmount();
        if (!depositOrder.getAmount().equals(paidAmount + waitAmount)){
            LOG.info("校验数据异常，业务单完成此单缴费后【已交金额:{}】 + 【待付金额:{}】 ！= 【业务单总金额:{}】", paidAmount,waitAmount,depositOrder.getAmount() );
            return BaseOutput.failure("校验数据异常，业务单完成此单缴费后【已交金额】 + 【待付金额】 ！= 【业务单总金额】");
        }

        //缴费单数据更新
        paymentOrderPO.setState(PaymentOrderStateEnum.PAID.getCode());
        paymentOrderPO.setPayedTime(settleOrder.getOperateTime());
        paymentOrderPO.setSettlementCode(settleOrder.getCode());
        paymentOrderPO.setSettlementOperator(settleOrder.getOperatorName());
        paymentOrderPO.setSettlementWay(settleOrder.getWay());
        if (paymentOrderService.updateSelective(paymentOrderPO) == 0) {
            LOG.info("缴费单成功回调 -- 更新【缴费单】,乐观锁生效！【付款单paymentOrderID:{}】", paymentOrderPO.getId());
            throw new BusinessException(ResultCode.DATA_ERROR, "多人操作，请重试！");
        }

        //修改订单状态
        if (paymentOrderPO.getAmount() < depositOrder.getWaitAmount()){ // 如果付款金额 < 待付金额 ,当前业务单付款状态为【未交清】
            depositOrder.setPayState(DepositPayStateEnum.NOT_PAID.getCode());
        }
        if (paymentOrderPO.getAmount().equals(depositOrder.getWaitAmount())){ // 如果付款金额 == 待付金额 ,当前业务单付款状态为【已交清】
            depositOrder.setPayState(DepositPayStateEnum.PAID.getCode());
        }
        depositOrder.setState(DepositOrderStateEnum.PAID.getCode());
        depositOrder.setPaidAmount(paidAmount);
        depositOrder.setWaitAmount(waitAmount);

        if (this.updateSelective(depositOrder) == 0) {
            LOG.info("缴费单成功回调 -- 更新【保证金单】状态,乐观锁生效！【保证金单DepositOrderID:{}】", depositOrder.getId());
            throw new BusinessException(ResultCode.DATA_ERROR, "多人操作，请重试！");
        }
        //【更新/修改】保证金余额 --- 缴费成功保证金余额增加
        this.saveOrupdateDepositBalance(depositOrder, paymentOrderPO.getAmount());
        //记录日志
        msgService.sendBusinessLog(recordPayLog(settleOrder, depositOrder));

        return BaseOutput.success().setData(depositOrder);
    }

    /**
     * 记录交费日志
     *
     * @param settleOrder 结算单
     * @param depositOrder 保证金业务单
     */
    private BusinessLog recordPayLog(SettleOrder settleOrder, DepositOrder depositOrder) {
        BusinessLog businessLog = new BusinessLog();
        businessLog.setBusinessId(depositOrder.getId());
        businessLog.setBusinessCode(depositOrder.getCode());
        businessLog.setContent(settleOrder.getCode());
        businessLog.setOperationType("pay");
        businessLog.setMarketId(settleOrder.getMarketId());
        businessLog.setOperatorId(settleOrder.getOperatorId());
        businessLog.setOperatorName(settleOrder.getOperatorName());
        businessLog.setBusinessType(LogBizTypeConst.DEPOSIT_ORDER);
        businessLog.setSystemCode("INTELLIGENT_ASSETS");
        return businessLog;
    }

    private BaseOutput<String> saveOrupdateDepositBalance(DepositOrder depositOrder, Long payAmount){
        DepositBalance params = new DepositBalance();
        // 保证金余额维度： 保证金类型，资产类型，资产编号，客户
        params.setCustomerId(depositOrder.getCustomerId());
        params.setTypeCode(depositOrder.getTypeCode());
        params.setAssetsType(depositOrder.getAssetsType());
        params.setAssetsId(depositOrder.getAssetsId());
        params.setMarketId(depositOrder.getMarketId());
        params.setMarketCode(depositOrder.getMarketCode());
        DepositBalance depositBalance = depositBalanceService.listByExample(params).stream().findFirst().orElse(null);
        if (depositBalance == null){//创建客户账户余额
            params.setAssetsName(depositOrder.getAssetsName());
            params.setBalance(payAmount);
            params.setCertificateNumber(depositOrder.getCertificateNumber());
            params.setCustomerCellphone(depositOrder.getCustomerCellphone());
            params.setCustomerName(depositOrder.getCustomerName());
            params.setTypeName(depositOrder.getTypeName());
            depositBalanceService.insertSelective(params);
        }else {
            DepositBalance upDep = new DepositBalance();
            upDep.setId(depositBalance.getId());
            upDep.setBalance(depositBalance.getBalance() + payAmount);
            upDep.setVersion(depositBalance.getVersion());
            depositBalanceService.updateSelective(upDep);
        }
        return BaseOutput.success();
    }

    @Override
    public BaseOutput<PrintDataDto> queryPrintData(String orderCode, Integer reprint) {
        PaymentOrder paymentOrderCondition = new PaymentOrder();
        paymentOrderCondition.setCode(orderCode);
        paymentOrderCondition.setBizType(BizTypeEnum.DEPOSIT_ORDER.getCode());
        PaymentOrder paymentOrder = paymentOrderService.list(paymentOrderCondition).stream().findFirst().orElse(null);
        if (null == paymentOrder) {
            throw new RuntimeException("businessCode无效");
        }
        if (!PaymentOrderStateEnum.PAID.getCode().equals(paymentOrder.getState())) {
            return BaseOutput.failure("此单未支付");
        }

        DepositOrder depositOrder = get(paymentOrder.getBusinessId());
        Integer settlementWay = paymentOrder.getSettlementWay();

        DepositOrderPrintDto dePrintDto = new DepositOrderPrintDto();
        dePrintDto.setPrintTime(LocalDateTime.now());
        dePrintDto.setReprint(reprint == 2 ? "(补打)" : "");
        dePrintDto.setCode(depositOrder.getCode());
        dePrintDto.setCustomerName(depositOrder.getCustomerName());
        dePrintDto.setCustomerCellphone(depositOrder.getCustomerCellphone());
        dePrintDto.setNotes(depositOrder.getNotes());
        dePrintDto.setAmount(MoneyUtils.centToYuan(paymentOrder.getAmount())); // 付款金额
        dePrintDto.setTotalAmount(MoneyUtils.centToYuan(depositOrder.getAmount())); // 业务单合计总金额
        dePrintDto.setWaitAmount(MoneyUtils.centToYuan(depositOrder.getWaitAmount())); //待付款金额
        dePrintDto.setSubmitter(paymentOrder.getCreator());
        dePrintDto.setBizType(BizTypeEnum.DEPOSIT_ORDER.getName());
        dePrintDto.setTypeName(depositOrder.getTypeName());
        dePrintDto.setAssetsType(AssetsTypeEnum.getAssetsTypeEnum(depositOrder.getAssetsType()).getName());
        dePrintDto.setAssetsName(depositOrder.getAssetsName());
        dePrintDto.setSettlementWay(SettleWayEnum.getNameByCode(settlementWay));
        dePrintDto.setSettlementOperator(paymentOrder.getSettlementOperator());

        //组合支付需要显示结算详情.票据的资产类型和编号，如果有填写，就显示，没填写就不显示,银行卡、POS、微信、支付宝等支付方式均如此显示，现金则不显示流水号,如果是园区卡付款，则显示对应卡号和开卡人姓名
        StringBuffer settleWayDetails = new StringBuffer();
        if (paymentOrder.getSettlementWay().equals(SettleWayEnum.MIXED_PAY.getCode())){
            settleWayDetails.append("【");
            BaseOutput<List<SettleWayDetail>> output = settlementRpc.listSettleWayDetailsByCode(paymentOrder.getSettlementCode());
            if (output.isSuccess() && CollectionUtils.isNotEmpty(output.getData())){
                output.getData().forEach(o -> {
                    //此循环字符串拼接顺序不可修改，样式 微信  150.00，4237458467568870，备注：微信付款150元
                    settleWayDetails.append(SettleWayEnum.getNameByCode(o.getWay())).append("  ").append(MoneyUtils.centToYuan(o.getAmount()));
                    if (StringUtils.isNotEmpty(o.getSerialNumber())){
                        settleWayDetails.append(",").append(o.getSerialNumber());
                    }
                    if (StringUtils.isNotEmpty(o.getNotes())){
                        settleWayDetails.append(",").append("备注：").append(o.getNotes());
                    }
                    settleWayDetails.append("\r\n");
                });
            }else {
                LOGGER.info("查询结算微服务组合支付，支付详情失败；原因：{}",output.getMessage());
            }
            settleWayDetails.append("】");
        }else if ( !settlementWay.equals(SettleWayEnum.CASH.getCode())){
            BaseOutput<SettleOrder> output = settlementRpc.getByCode(paymentOrder.getSettlementCode());
            if(output.isSuccess()){
                SettleOrder settleOrder = output.getData();
                if(StringUtils.isNotBlank(settleOrder.getSerialNumber())){
                    settleWayDetails.append("流水号：");
                    settleWayDetails.append(settleOrder.getSerialNumber());
                }
            }else {
                LOGGER.info("查询结算微服务非组合支付，支付详情失败；原因：{}",output.getMessage());
            }
        }
        if (StringUtils.isNotBlank(settleWayDetails)){
            dePrintDto.setSettleWayDetails(settleWayDetails.toString());
        }

        PrintDataDto printDataDto = new PrintDataDto();
        printDataDto.setName(PrintTemplateEnum.DEPOSIT_ORDER.getCode());
        printDataDto.setItem(BeanMapUtil.beanToMap(dePrintDto));
        return BaseOutput.success().setData(printDataDto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseOutput refundSuccessHandler(RefundOrder refundOrder) {
        DepositOrder depositOrder = this.get(refundOrder.getBusinessId());
        if (DepositRefundStateEnum.REFUNDED.getCode().equals(depositOrder.getRefundState())) {
            LOG.info("此退款单【refundOrderId={}】关联的业务单【businessCode={}】已【全额退款】，退款失败！", refundOrder.getId(), refundOrder.getBusinessCode());
            return BaseOutput.failure("此退款单关联的业务单已【全额退款】，退款失败！");
        }
        if (!DepositOrderStateEnum.REFUNDING.getCode().equals(depositOrder.getState())){
            LOG.info("此退款单【refundOrderId={}】关联的业务单状态已变更【状态：{}】，退款失败！", refundOrder.getId(), DepositOrderStateEnum.getDepositOrderStateEnumName(depositOrder.getState()));
            return BaseOutput.failure("此退款单关联的业务单状态已变更，退款失败！");
        }
        Long totalRefundAmount = refundOrder.getTotalRefundAmount() + depositOrder.getRefundAmount();
        if (depositOrder.getPaidAmount() < totalRefundAmount){
            LOG.error("异常订单！！！---- 保证金单退款申请结算退款成功 但是退款单退款总金额大于订单可退金额【保证金单ID {}，退款单ID{}】", depositOrder.getId(), refundOrder.getId());
            throw new BusinessException(ResultCode.DATA_ERROR, "异常订单！！！-- 退款金额不能大于保证金单可退金额！");
        }
        if (depositOrder.getPaidAmount().equals(totalRefundAmount)){
            depositOrder.setRefundState(DepositRefundStateEnum.REFUNDED.getCode());
        }else {
            depositOrder.setRefundState(DepositRefundStateEnum.PART_REFUND.getCode());
        }
        depositOrder.setRefundAmount(totalRefundAmount);
        depositOrder.setState(DepositOrderStateEnum.REFUND.getCode());

        if (updateSelective(depositOrder) == 0) {
            LOG.info("保证金单退款申请结算退款成功 更新保证金单乐观锁生效 【保证金单ID {}】", depositOrder.getId());
            throw new BusinessException(ResultCode.DATA_ERROR, "多人操作，请重试！");
        }
        //更新保证金余额 ---- 退款扣减保证金余额
        this.saveOrupdateDepositBalance(depositOrder, 0 - refundOrder.getTotalRefundAmount());

        //转抵扣充值
        TransferDeductionItem transferDeductionItemCondition = new TransferDeductionItem();
        transferDeductionItemCondition.setRefundOrderId(refundOrder.getId());
        List<TransferDeductionItem> transferDeductionItems = transferDeductionItemService.list(transferDeductionItemCondition);
        if (CollectionUtils.isNotEmpty(transferDeductionItems)) {
            transferDeductionItems.forEach(o -> {
                BaseOutput accountOutput = customerAccountService.leaseOrderRechargTransfer(
                        refundOrder.getId(), refundOrder.getCode(), o.getPayeeId(), o.getPayeeAmount(),
                        refundOrder.getMarketId(), refundOrder.getRefundOperatorId(), refundOrder.getRefundOperator());
                if (!accountOutput.isSuccess()) {
                    LOG.info("退款单转抵异常，【退款编号:{},收款人:{},收款金额:{},msg:{}】", refundOrder.getCode(), o.getPayee(), o.getPayeeAmount(), accountOutput.getMessage());
                    throw new BusinessException(ResultCode.DATA_ERROR, accountOutput.getMessage());
                }
            });
        }

        //记录退款日志
        msgService.sendBusinessLog(recordRefundLog(refundOrder));

        return BaseOutput.success();
    }


    /**
     * 记录退款日志
     *
     * @param refundOrder 退款单
     */
    private BusinessLog recordRefundLog(RefundOrder refundOrder) {
        BusinessLog businessLog = new BusinessLog();
        businessLog.setBusinessId(refundOrder.getBusinessId());
        businessLog.setBusinessCode(refundOrder.getBusinessCode());
        businessLog.setContent(refundOrder.getSettlementCode());
        businessLog.setOperationType("refund");
        businessLog.setMarketId(refundOrder.getMarketId());
        businessLog.setOperatorId(refundOrder.getRefundOperatorId());
        businessLog.setOperatorName(refundOrder.getRefundOperator());
        businessLog.setBusinessType(LogBizTypeConst.DEPOSIT_ORDER);
        businessLog.setSystemCode("INTELLIGENT_ASSETS");
        return businessLog;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseOutput batchAddOrUpdateDepositOrder(List<DepositOrder> depositOrderList) {
        if (CollectionUtils.isEmpty(depositOrderList)){
            return BaseOutput.success();
        }
        List<DepositOrder> oldList = this.queryDepositOrder(depositOrderList.get(0).getBizType(), depositOrderList.get(0).getBusinessId(), null);
        Map<Long, Long> assetsIdsMap = new HashMap<>();
        oldList.stream().forEach(o ->{
            assetsIdsMap.put(o.getAssetsId(), o.getId());
        });

        depositOrderList.stream().forEach(o ->{
            List<DepositOrder> deList = queryDepositOrder(o.getBizType(), o.getBusinessId(), o.getAssetsId());
            if (CollectionUtils.isEmpty(deList)){ // 没有的话，就【新增】
                o.setIsRelated(YesOrNoEnum.YES.getCode());
                if (o.getBusinessId() == null){
                    throw new BusinessException(ResultCode.PARAMS_ERROR, "关联订单ID不能为空");
                }
                if (o.getBizType() == null){
                    throw new BusinessException(ResultCode.PARAMS_ERROR, "关联订单业务类型不能为空");
                }
                BaseOutput output = this.addDepositOrder(o);
                if (!output.isSuccess()){
                    throw new BusinessException(ResultCode.DATA_ERROR, output.getMessage());
                }
            }else {// 有的话， 就【修改】
                o.setId(deList.get(0).getId());
                BaseOutput output = this.updateDepositOrder(o);
                if (!output.isSuccess()){
                    throw new BusinessException(ResultCode.DATA_ERROR, output.getMessage());
                }

                if (assetsIdsMap.containsKey(o.getAssetsId())){
                    assetsIdsMap.remove(o.getAssetsId());
                }
            }
        });
        assetsIdsMap.forEach((key, value) -> { //【取消】
            DepositOrder depositOrder = this.get(value);
            this.cancelDepositOrder(depositOrder);
        });
        return BaseOutput.success();
    }

    private List<DepositOrder> queryDepositOrder(String bizType, Long businessId, Long assetsId){
        DepositOrder query = new DepositOrder();
        query.setBizType(bizType);
        query.setBusinessId(businessId);
        query.setAssetsId(assetsId);
        query.setIsRelated(YesOrNoEnum.YES.getCode()); //必须是关联订单
        List<DepositOrder> list = this.listByExample(query);
        return list;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseOutput batchSubmitDepositOrder(String bizType, Long businessId, Map<Long, Long> map) {
        if (map == null){
            return BaseOutput.success();
        }
        if (bizType == null){
            return BaseOutput.failure("参数bizType 不能为空！");
        }
        if (businessId == null){
            return BaseOutput.failure("参数businessId 不能为空！");
        }
        map.forEach((key, value) -> {
            List<DepositOrder> deList = this.queryDepositOrder(bizType, businessId, key);
            if (CollectionUtils.isNotEmpty(deList)){
                BaseOutput output = this.submitDepositOrder(deList.get(0).getId(), value, deList.get(0).getWaitAmount());
                if (!output.isSuccess()){
                    throw new BusinessException(ResultCode.DATA_ERROR, output.getMessage());
                }
            }
        });
        return BaseOutput.success();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseOutput batchWithdrawDepositOrder(String bizType, Long businessId) {
        if (businessId == null){
            return BaseOutput.failure("参数businessId 不能为空！");
        }
        if (bizType == null){
            return BaseOutput.failure("参数bizType 不能为空！");
        }
        List<DepositOrder> deList = this.queryDepositOrder(bizType, businessId, null);
        deList.stream().forEach(o -> {
            // 如果状态是【已提交】状态，就同步撤回
            if (o.getState().equals(DepositOrderStateEnum.SUBMITTED.getCode())){
                BaseOutput output = this.withdrawDepositOrder(o.getId());
                if (!output.isSuccess()){
                    throw new BusinessException(ResultCode.DATA_ERROR, output.getMessage());
                }
            }else if (o.getState().equals(DepositOrderStateEnum.CREATED.getCode())){
                //如果状态是【已创建】，就不做任何处理
            }else {// 如果状态不是【已提交】状态，就解除关联订单操作关系
                o.setIsRelated(YesOrNoEnum.NO.getCode());
                if (this.updateSelective(o) == 0) {
                    LOG.info("修改保证金【解除关联操作】失败 ,乐观锁生效！【保证金单ID:{}】", o.getId());
                    throw new BusinessException(ResultCode.DATA_ERROR, "多人操作，请重试！");
                }
            }
        });
        return BaseOutput.success();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseOutput batchSubmitDepositOrderFull(String bizType, Long businessId) {
        if (bizType == null){
            return BaseOutput.failure("参数bizType 不能为空！");
        }
        if (businessId == null){
            return BaseOutput.failure("参数businessId 不能为空！");
        }
        List<DepositOrder> deList = this.queryDepositOrder(bizType, businessId, null);
        if (CollectionUtils.isNotEmpty(deList)){
            deList.stream().forEach(o -> {
                BaseOutput output = this.submitDepositOrder(o.getId(), o.getAmount(), o.getWaitAmount());
                if (!output.isSuccess()){
                    throw new BusinessException(ResultCode.DATA_ERROR, output.getMessage());
                }
            });
        }
        return BaseOutput.success();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseOutput batchCancelDepositOrder(String bizType, Long businessId) {
        if (bizType == null){
            return BaseOutput.failure("参数bizType 不能为空！");
        }
        if (businessId == null){
            return BaseOutput.failure("参数businessId 不能为空！");
        }
        List<DepositOrder> deList = this.queryDepositOrder(bizType, businessId, null);
        if (CollectionUtils.isNotEmpty(deList)){
            deList.stream().forEach(o -> {
                this.cancelDepositOrder(o);
            });
        }
        return BaseOutput.success();
    }

    public void cancelDepositOrder(DepositOrder depositOrder) {
        if (!depositOrder.getState().equals(DepositOrderStateEnum.CREATED.getCode())){
            throw new BusinessException(ResultCode.DATA_ERROR, "取消失败，保证金单状态已变更！");
        }
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        depositOrder.setCancelerId(userTicket.getId());
        depositOrder.setCanceler(userTicket.getRealName());
        depositOrder.setState(DepositOrderStateEnum.CANCELD.getCode());
        //取消解除关联操作关系
        depositOrder.setIsRelated(YesOrNoEnum.NO.getCode());
        if (this.updateSelective(depositOrder) == 0){
            LOG.error("保证金取消失败，取消更新状态记录数为 0，取消保证金ID【{}】", depositOrder.getId());
            throw new BusinessException(ResultCode.DATA_ERROR, "取消失败！");
        }
    }

    @Override
    public BaseOutput<List<DepositBalance>> listDepositBalance(String bizType, Long customerId, List<Long> assetsIds) {
        if (bizType == null){
            return BaseOutput.failure("参数bizType 不能为空！");
        }
        if (customerId == null){
            return BaseOutput.failure("参数customerId 不能为空！");
        }
        if (CollectionUtils.isEmpty(assetsIds)){
            return BaseOutput.success();
        }
        List<DepositBalance> list = new ArrayList<>();
        assetsIds.stream().forEach(o -> {
            DepositBalance depositBalance = this.queryDepositBalance(customerId, o);
            if (depositBalance != null){
                list.add(depositBalance);
            }
        });

        return BaseOutput.success().setData(list);
    }

    private DepositBalance queryDepositBalance(Long customerId, Long assetsId){
        DepositBalance depositBalance = new DepositBalance();
        depositBalance.setCustomerId(customerId);
        depositBalance.setAssetsId(assetsId);
        return depositBalanceService.listByExample(depositBalance).stream().findFirst().orElse(null);
    }

    @Override
    public BaseOutput batchReleaseRelated(String bizType, Long businessId, Long assetsId) {
        if (businessId == null){
            return BaseOutput.failure("参数businessId 不能为空！");
        }
        if (bizType == null){
            return BaseOutput.failure("参数bizType 不能为空！");
        }
        List<DepositOrder> deList = this.queryDepositOrder(bizType, businessId, assetsId);
        deList.stream().forEach(o -> {
            o.setIsRelated(YesOrNoEnum.NO.getCode());
            if (this.updateSelective(o) == 0) {
                LOG.info("修改保证金【解除关联操作】失败 ,乐观锁生效！【保证金单ID:{}】", o.getId());
                throw new BusinessException(ResultCode.DATA_ERROR, "多人操作，请重试！");
            }
        });
        return BaseOutput.success();
    }


   /* ************************************************************** start 【老数据迁移 】 后期删除 ************************************************************************************/

    /**
     * 老数据处理-- 新增保证金单
     * 新增已交费的保证金单，
     * 修改/新增保证金余额，
     * 新增已交费的缴费单，
     * 新增已结算的结算单；
     *
     * */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseOutput oldDataHandler(List<DepositOrder> depositOrderList) {
        if (CollectionUtils.isEmpty(depositOrderList)){
            return BaseOutput.failure("数据为空！");
        }
        //用户默认处理为 "杭州系统"
        UserTicket userTicket = SessionContext.getSessionContext().fetchUserApi().getUserByUsername("杭州系统");
        depositOrderList.stream().forEach(o ->{
            List<DepositOrder> deList = queryDepositOrderOldDataHandler(o.getBizType(), o.getBusinessId(), o.getAssetsId());
            if (CollectionUtils.isEmpty(deList)){ // 没有的话，就【新增】
                o.setIsRelated(YesOrNoEnum.NO.getCode());
                if (o.getBusinessId() == null){
                    throw new BusinessException(ResultCode.PARAMS_ERROR, "关联订单ID不能为空");
                }
                if (o.getBizType() == null){
                    throw new BusinessException(ResultCode.PARAMS_ERROR, "关联订单业务类型不能为空");
                }
                //新增【已交费 -- 已交清 -- 未退款】的保证金单
                BaseOutput<DepositOrder> output = this.addPaidDepositOrder(userTicket, o);
                if (!output.isSuccess()){
                    throw new BusinessException(ResultCode.DATA_ERROR, output.getMessage());
                }
                DepositOrder newDepositOrder = output.getData();
                //结算编号
                String settleCode = o.getMarketCode().toUpperCase() + this.getBizNumber("settleOrder");
                //【更新/修改】保证金余额 --- 缴费成功保证金余额增加
                this.saveOrupdateDepositBalance(newDepositOrder, newDepositOrder.getAmount());
                //新增【已交费】的缴费单
                PaymentOrder pb = this.buildPaidPaymentOrder(userTicket, newDepositOrder, o.getAmount());
                //结算后，回写结算信息到缴费单 @TODO 待确认传入参数来源
                pb.setPayedTime(LocalDateTime.now());
                pb.setSettlementCode(settleCode);
                pb.setSettlementOperator(userTicket.getRealName());
                pb.setSettlementWay(SettleWayEnum.CASH.getCode());
                paymentOrderService.insertSelective(pb);

                //新增【已结算】的结算单，提交到结算中心 --- 执行顺序不可调整！！因为异常只能回滚自己系统，无法回滚其它远程系统
                //@TODO需要结算单独提供接口
                SettleOrderDto settleOrder = buildPaidSettleOrderDto(userTicket, o, pb, o.getAmount());
                settleOrder.setCode(settleCode);
                List<SettleOrder> settleOrderList = new ArrayList<>();
                settleOrderList.add(settleOrder);
                //@TODO 结算已交费的结算单接口提供
                BaseOutput<?> out= settlementRpc.batchSaveDealt(settleOrderList);
                if (!out.isSuccess()){
                    LOG.info("提交到结算中心失败！" + out.getMessage() + out.getErrorData());
                    throw new BusinessException(ResultCode.DATA_ERROR, out.getMessage());
                }
            }
        });
        return BaseOutput.success();
    }
    /**
     * 老数据处理-- 新增保证金单
     * 新增已交费的保证金单，
     * 修改/新增保证金余额，
     * 新增已交费的缴费单，
     * 新增已结算的结算单；
     *
     *新增关联退款单
     *新增退款单的退款结算单
     * */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseOutput oldRefundOrderDataHandler(List<DepositOrder> depositOrderList) {
        if (CollectionUtils.isEmpty(depositOrderList)){
            return BaseOutput.failure("数据为空！");
        }
        //用户默认处理为 "杭州系统"
        UserTicket userTicket = SessionContext.getSessionContext().fetchUserApi().getUserByUsername("杭州系统");
        depositOrderList.stream().forEach(o ->{
            List<DepositOrder> deList = queryDepositOrderOldDataHandler(o.getBizType(), o.getBusinessId(), o.getAssetsId());
            // 为空的话，抛出异常，因为退款单一定对应有业务单。 不为空的话就【新增】关联退款单
            if (CollectionUtils.isEmpty(deList)){
                throw new BusinessException(ResultCode.DATA_ERROR, "创建保证金退款单失败，退款单没有找到关联业务单，BusinessId=" + o.getBusinessId() + "，AssetsId=" + o.getAssetsId());
            }
            //不为空的话就【新增】关联退款单
            deList.stream().forEach(depOrder -> {
                //修改保证金业务单为【已退款费 -- 已交清 -- 全额退款】的保证金单
                depOrder.setState(DepositOrderStateEnum.REFUND.getCode());
                depOrder.setRefundState(DepositRefundStateEnum.REFUNDED.getCode());
                depOrder.setRefundAmount(depOrder.getAmount());
                if (this.updateSelective(depOrder) == 0) {
                    LOG.info("修改保证金【解除关联操作】失败 ,乐观锁生效！【保证金单ID:{}】", o.getId());
                    throw new BusinessException(ResultCode.DATA_ERROR, "多人操作，请重试！");
                }
                //【更新/修改】保证金余额 --- 缴费成功保证金余额增加
                this.saveOrupdateDepositBalance(depOrder, 0 - depOrder.getAmount());

                //结算编号
                String settleCode = o.getMarketCode().toUpperCase() + this.getBizNumber("settleOrder");
                //新增【已退款】的退款单
                RefundOrder refundOrder = this.buildRefundOrder(depOrder, userTicket, settleCode);
                refundOrderService.insertSelective(refundOrder);

                //新增【已处理】的结算单，提交到结算中心 --- 执行顺序不可调整！！因为异常只能回滚自己系统，无法回滚其它远程系统
                //@TODO 需要结算单独提供接口
                SettleOrderDto settleOrder = buildRefundSettleOrderDto(userTicket, refundOrder);
                settleOrder.setCode(settleCode);
                List<SettleOrder> settleOrderList = new ArrayList<>();
                settleOrderList.add(settleOrder);
                //@TODO 结算已交费的结算单接口提供
                BaseOutput<?> out= settlementRpc.batchSaveDealt(settleOrderList);
                if (!out.isSuccess()){
                    LOG.info("提交到结算中心失败！" + out.getMessage() + out.getErrorData());
                    throw new BusinessException(ResultCode.DATA_ERROR, out.getMessage());
                }
            });

        });
        return BaseOutput.success();
    }

    private List<DepositOrder> queryDepositOrderOldDataHandler(String bizType, Long businessId, Long assetsId){
        DepositOrder query = new DepositOrder();
        query.setBizType(bizType);
        query.setBusinessId(businessId);
        query.setAssetsId(assetsId);
        List<DepositOrder> list = this.listByExample(query);
        return list;
    }

    //构建已退款的退款单
    private RefundOrder buildRefundOrder(DepositOrder depositOrder, UserTicket userTicket, String settlementCode){
        //新增【已退款】的退款单
        RefundOrder refundOrder = new RefundOrder();
        refundOrder.setSettlementCode(settlementCode);

        refundOrder.setState(RefundOrderStateEnum.REFUNDED.getCode());
        refundOrder.setCode(userTicket.getFirmCode().toUpperCase() + this.getBizNumber(userTicket.getFirmCode() + "_" + BizNumberTypeEnum.DEPOSIT_REFUND_ORDER.getCode()));
        refundOrder.setBizType(BizTypeEnum.DEPOSIT_ORDER.getCode());
        refundOrder.setCreator(userTicket.getRealName());
        refundOrder.setCreatorId(userTicket.getId());
        refundOrder.setMarketId(userTicket.getFirmId());
        refundOrder.setMarketCode(userTicket.getFirmCode());
        refundOrder.setApprovalState(ApprovalStateEnum.WAIT_SUBMIT_APPROVAL.getCode());
        refundOrder.setVersion(0);
        refundOrder.setCustomerId(depositOrder.getCustomerId());
        refundOrder.setCustomerName(depositOrder.getCustomerName());
        refundOrder.setCustomerCellphone(depositOrder.getCustomerCellphone());
        refundOrder.setCertificateNumber(depositOrder.getCertificateNumber());
        refundOrder.setBusinessId(depositOrder.getId());
        refundOrder.setRefundTime(new Date());
        refundOrder.setTotalRefundAmount(depositOrder.getRefundAmount());
        refundOrder.setPayeeAmount(depositOrder.getRefundAmount());
        refundOrder.setSubmitter(userTicket.getRealName());
        refundOrder.setSubmitterId(userTicket.getId());
        refundOrder.setSubmitTime(LocalDateTime.now());

        //@TODO 租赁单传入
//        refundOrder.setPayee();
//        refundOrder.setPayeeId();
//        refundOrder.setPayeeCertificateNumber();
//        refundOrder.setRefundType();
//        refundOrder.setRefundOperatorId();
//        refundOrder.setRefundOperator();
        return refundOrder;
    }

    //组装缴费单 PaymentOrder
    private PaymentOrder buildPaidPaymentOrder(UserTicket submiterTicket, DepositOrder depositOrder, Long paidAmount){
        PaymentOrder pb = new PaymentOrder();
        pb.setCode(depositOrder.getMarketCode().toUpperCase() + this.getBizNumber(BizNumberTypeEnum.PAYMENT_ORDER.getCode()));
        pb.setAmount(paidAmount);
        pb.setBusinessId(depositOrder.getId());
        pb.setBusinessCode(depositOrder.getCode());
        pb.setCreatorId(submiterTicket.getId());
        pb.setCreator(submiterTicket.getRealName());
        pb.setMarketId(submiterTicket.getFirmId());
        pb.setMarketCode(submiterTicket.getFirmCode());
        pb.setBizType(BizTypeEnum.DEPOSIT_ORDER.getCode());
        pb.setState(PayStateEnum.PAID.getCode());
        pb.setVersion(0);
        pb.setIsSettle(YesOrNoEnum.YES.getCode());
        return pb;
    }

    //组装 -- 结算中心缴费单 SettleOrder
    private SettleOrderDto buildPaidSettleOrderDto(UserTicket submitterTicket,DepositOrder depositOrder, PaymentOrder paymentOrder, Long paidAmount){
        SettleOrderDto settleOrder = new SettleOrderDto();
        //以下是提交到结算中心的必填字段
        settleOrder.setMarketId(depositOrder.getMarketId()); //市场ID
        settleOrder.setMarketCode(depositOrder.getMarketCode());
        settleOrder.setOrderCode(paymentOrder.getCode());//订单号 唯一
        settleOrder.setBusinessCode(paymentOrder.getBusinessCode()); //缴费单业务单号
        settleOrder.setCustomerId(depositOrder.getCustomerId());//客户ID
        settleOrder.setCustomerName(depositOrder.getCustomerName());// "客户姓名
        settleOrder.setCustomerPhone(depositOrder.getCustomerCellphone());//"客户手机号
        settleOrder.setAmount(paidAmount); //金额
        settleOrder.setBusinessDepId(depositOrder.getDepartmentId()); //"业务部门ID
        settleOrder.setBusinessDepName(departmentRpc.get(depositOrder.getDepartmentId()).getData().getName());//"业务部门名称
        settleOrder.setSubmitterId(submitterTicket.getId());// "提交人ID
        settleOrder.setSubmitterName(submitterTicket.getRealName());// "提交人姓名
        if (submitterTicket.getDepartmentId() != null){
            settleOrder.setSubmitterDepId(submitterTicket.getDepartmentId()); //"提交人部门ID
            settleOrder.setSubmitterDepName(departmentRpc.get(submitterTicket.getDepartmentId()).getData().getName());
        }
        settleOrder.setSubmitTime(LocalDateTime.now());
        settleOrder.setAppId(settlementAppId);//应用ID
        //@TODO 结算单需要调整类型，为String
        settleOrder.setBusinessType(Integer.valueOf(BizTypeEnum.DEPOSIT_ORDER.getCode())); // 业务类型
        settleOrder.setType(SettleTypeEnum.PAY.getCode());// "结算类型  -- 付款
        settleOrder.setState(SettleStateEnum.DEAL.getCode());
        settleOrder.setReturnUrl(settlerHandlerUrl); // 结算-- 缴费成功后回调路径
        return settleOrder;
    }

    //组装 -- 结算中心退款单 SettleOrder
    private SettleOrderDto buildRefundSettleOrderDto(UserTicket userTicket, RefundOrder refundOrder){
        SettleOrderDto settleOrder = new SettleOrderDto();
        //以下是提交到结算中心的必填字段
        settleOrder.setMarketId(refundOrder.getMarketId()); //市场ID
        settleOrder.setMarketCode(refundOrder.getMarketCode());
        settleOrder.setOrderCode(refundOrder.getCode());//订单号 唯一
        settleOrder.setBusinessCode(refundOrder.getBusinessCode()); //缴费单业务单号
        settleOrder.setCustomerId(refundOrder.getPayeeId());//客户ID
        settleOrder.setCustomerName(refundOrder.getPayee());// "客户姓名
//        settleOrder.setCustomerPhone(refundOrder.get);//"客户手机号
        settleOrder.setAmount(refundOrder.getPayeeAmount()); //金额
        settleOrder.setBusinessDepId(refundOrder.getDepartmentId()); //"业务部门ID
        settleOrder.setBusinessDepName(departmentRpc.get(refundOrder.getDepartmentId()).getData().getName());//"业务部门名称
        settleOrder.setSubmitterId(userTicket.getId());// "提交人ID
        settleOrder.setSubmitterName(userTicket.getRealName());// "提交人姓名
        if (userTicket.getDepartmentId() != null){
            settleOrder.setSubmitterDepId(userTicket.getDepartmentId()); //"提交人部门ID
            settleOrder.setSubmitterDepName(departmentRpc.get(userTicket.getDepartmentId()).getData().getName());
        }
        settleOrder.setSubmitTime(LocalDateTime.now());
        settleOrder.setAppId(settlementAppId);//应用ID
        //@TODO 结算单需要调整类型，为String
        settleOrder.setBusinessType(Integer.valueOf(BizTypeEnum.DEPOSIT_ORDER.getCode())); // 业务类型
        settleOrder.setType(SettleTypeEnum.REFUND.getCode());// "结算类型  -- 付款
        settleOrder.setState(SettleStateEnum.DEAL.getCode());
        settleOrder.setReturnUrl(settlerHandlerUrl); // 结算-- 缴费成功后回调路径
        return settleOrder;
    }

    public BaseOutput<DepositOrder> addPaidDepositOrder(UserTicket userTicket, DepositOrder depositOrder) {
        //检查参数
        BaseOutput checkOut = checkparams(depositOrder);
        if (!checkOut.isSuccess()){
            return checkOut;
        }
        //检查客户状态
        checkCustomerState(depositOrder.getCustomerId(),userTicket.getFirmId());
        //检查摊位状态 @TODO 检查公寓，冷库状态
        if(AssetsTypeEnum.BOOTH.getCode().equals(depositOrder.getAssetsType())){
            checkBoothState(depositOrder.getAssetsId());
        }
        BaseOutput<Department> depOut = departmentRpc.get(depositOrder.getDepartmentId());
        if(!depOut.isSuccess()){
            LOGGER.info("获取部门失败！" + depOut.getMessage());
            throw new BusinessException(ResultCode.DATA_ERROR, "获取部门失败！");
        }

        depositOrder.setCode(userTicket.getFirmCode().toUpperCase() + this.getBizNumber(userTicket.getFirmCode() + "_" + BizNumberTypeEnum.DEPOSIT_ORDER.getCode()));
        depositOrder.setCreatorId(userTicket.getId());
        depositOrder.setCreator(userTicket.getRealName());
        depositOrder.setMarketId(userTicket.getFirmId());
        depositOrder.setMarketCode(userTicket.getFirmCode());
        depositOrder.setDepartmentName(depOut.getData().getName());
        //创建已完结的保证金单
        depositOrder.setState(DepositOrderStateEnum.PAID.getCode());
        depositOrder.setPayState(DepositPayStateEnum.PAID.getCode());
        depositOrder.setRefundState(DepositRefundStateEnum.NO_REFUNDED.getCode());
        depositOrder.setIsImport(YesOrNoEnum.YES.getCode());
        depositOrder.setWaitAmount(0L);
        depositOrder.setPaidAmount(depositOrder.getAmount());

        this.insertSelective(depositOrder);
        return BaseOutput.success().setData(depositOrder);
    }
    /* ************************************************************* end 【老数据迁移 】 后期删除 ************************************************************************************/

}