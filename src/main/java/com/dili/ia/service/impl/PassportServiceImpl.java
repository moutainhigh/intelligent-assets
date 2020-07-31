package com.dili.ia.service.impl;

import com.dili.ia.domain.Passport;
import com.dili.ia.domain.PaymentOrder;
import com.dili.ia.domain.RefundOrder;
import com.dili.ia.domain.dto.MeterDetailDto;
import com.dili.ia.domain.dto.PassportDto;
import com.dili.ia.domain.dto.PassportRefundOrderDto;
import com.dili.ia.domain.dto.PrintDataDto;
import com.dili.ia.domain.dto.SettleOrderInfoDto;
import com.dili.ia.domain.dto.printDto.PassportPrintDto;
import com.dili.ia.glossary.BizNumberTypeEnum;
import com.dili.ia.glossary.BizTypeEnum;
import com.dili.ia.glossary.MeterDetailStateEnum;
import com.dili.ia.glossary.PassportStateEnum;
import com.dili.ia.glossary.PaymentOrderStateEnum;
import com.dili.ia.glossary.PrintTemplateEnum;
import com.dili.ia.mapper.PassportMapper;
import com.dili.ia.rpc.SettlementRpcResolver;
import com.dili.ia.rpc.UidRpcResolver;
import com.dili.ia.service.PassportService;
import com.dili.ia.service.PaymentOrderService;
import com.dili.ia.service.RefundOrderService;
import com.dili.ia.util.LoggerUtil;
import com.dili.settlement.domain.SettleOrder;
import com.dili.settlement.dto.SettleOrderDto;
import com.dili.settlement.enums.SettleStateEnum;
import com.dili.settlement.enums.SettleTypeEnum;
import com.dili.settlement.enums.SettleWayEnum;
import com.dili.ss.base.BaseServiceImpl;
import com.dili.ss.constant.ResultCode;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.domain.EasyuiPageOutput;
import com.dili.ss.dto.DTOUtils;
import com.dili.ss.exception.BusinessException;
import com.dili.ss.metadata.ValueProviderUtils;
import com.dili.ss.util.MoneyUtils;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.rpc.DepartmentRpc;
import com.dili.uap.sdk.session.SessionContext;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author:      xiaosa
 * @date:        2020/7/27
 * @version:     农批业务系统重构
 * @description: 通行证
 */
@Service
public class PassportServiceImpl extends BaseServiceImpl<Passport, Long> implements PassportService {

    public PassportMapper getActualDao() {
        return (PassportMapper)getDao();
    }


    private final static Logger logger = LoggerFactory.getLogger(PassportServiceImpl.class);

    @Autowired
    private UidRpcResolver uidRpcResolver;

    @Autowired
    private DepartmentRpc departmentRpc;

    @Autowired
    private PaymentOrderService paymentOrderService;

    @Autowired
    private SettlementRpcResolver settlementRpcResolver;

    @Autowired
    private RefundOrderService refundOrderService;

    @Value("${settlement.app-id}")
    private Long settlementAppId;

    private String settlerHandlerUrl = "http://ia.diligrp.com:8381/api/passport/settlementDealHandler";

    /**
     * 查询列表
     *
     * @param  passport
     * @param  useProvider
     * @return EasyuiPageOutput
     * @date   2020/7/29
     */
    @Override
    public EasyuiPageOutput listPassports(PassportDto passportDto, boolean useProvider) throws Exception {
        // 分页
        if (passportDto.getRows() != null && passportDto.getRows() >= 1) {
            PageHelper.startPage(passportDto.getPage(), passportDto.getRows());
        }

        List<Passport> passportInfoList= this.getActualDao().listPassports(passportDto);

        // 基础代码
        long total = passportInfoList instanceof Page ? ((Page)passportInfoList).getTotal() : (long)passportInfoList.size();
        List results = useProvider ? ValueProviderUtils.buildDataByProvider(passportDto, passportInfoList) : passportInfoList;

        return new EasyuiPageOutput(Integer.parseInt(String.valueOf(total)), results);
    }

    /**
     * 新增通行证
     *
     * @param  passportDto
     * @return BaseOutput
     * @date   2020/7/27
     */
    @Override
    public BaseOutput<Passport> addPassport(PassportDto passportDto, UserTicket userTicket) {
        Passport passport = new Passport();
        BeanUtils.copyProperties(passportDto, passport);

        // TODO 证件号


        // 生成通行证交费的 code
        String passportCode = uidRpcResolver.bizNumber(BizNumberTypeEnum.PASSPORT.getCode());
        passport.setVersion(0);
        passport.setCode(passportCode);
        passport.setCreatorId(userTicket.getId());
        passport.setCreator(userTicket.getRealName());
        passport.setMarketId(userTicket.getFirmId());
        passport.setMarketCode(userTicket.getFirmCode());
        passport.setCreateTime(LocalDateTime.now());
        passport.setModifyTime(LocalDateTime.now());
        passport.setState(PassportStateEnum.CREATED.getCode());

        this.getActualDao().insertSelective(passport);

        return BaseOutput.success().setData(passport);
    }

    /**
     * 修改通行证
     *
     * @param  passportDto
     * @return BaseOutput
     * @date   2020/7/27
     */
    @Override
    public BaseOutput<Passport> updatePassport(PassportDto passportDto, UserTicket userTicket)  throws Exception {

        Passport passportInfo = this.get(passportDto.getId());
        if (passportInfo == null) {
            throw new BusinessException(ResultCode.DATA_ERROR, "该记录已删除，修改失败。");
        }

        // 已创建状态才能修改
        if (!PassportStateEnum.CREATED.getCode().equals(passportInfo.getState())) {
            throw new BusinessException(ResultCode.DATA_ERROR, "该状态不是已创建，不能修改");
        }

        BeanUtils.copyProperties(passportDto, passportInfo);
        passportInfo.setModifyTime(LocalDateTime.now());
        passportInfo.setVersion(passportInfo.getVersion() + 1);

        if (this.updateSelective(passportInfo) == 0) {
            throw new BusinessException(ResultCode.DATA_ERROR, "多人操作，请刷新页面重试！");
        }

        return BaseOutput.success().setData(passportInfo);
    }

    /**
     * 提交通行证缴费
     *
     * @param  id
     * @param userTicket
     * @return BaseOutput
     * @date   2020/7/27
     */
    @Override
    public BaseOutput<Passport> submit(Long id, UserTicket userTicket)  throws Exception {
        // 先查询通行证
        Passport passportInfo = this.get(id);
        if (passportInfo == null) {
            throw new BusinessException(ResultCode.DATA_ERROR, "该通行证信息已不存在!");
        }

        // 已创建状态才能提交
        if (!PassportStateEnum.CREATED.getCode().equals(passportInfo.getState())) {
            throw new BusinessException(ResultCode.DATA_ERROR, "该状态不是已创建，不能取消");
        }

        // 修改水电费单状态为已提交
        passportInfo.setState(PassportStateEnum.SUBMITTED.getCode());
        passportInfo.setSubmitterId(userTicket.getId());
        passportInfo.setSubmitter(userTicket.getRealName());
        passportInfo.setSubmitTime(LocalDateTime.now());
        if (this.updateSelective(passportInfo) == 0) {
            logger.info("多人提交通行证付款!");
            throw new BusinessException(ResultCode.DATA_ERROR, "多人操作，请重试！");
        }

        // 创建缴费单
        PaymentOrder paymentOrder = paymentOrderService.buildPaymentOrder(userTicket);
        paymentOrder.setBusinessId(passportInfo.getId());
        paymentOrder.setBusinessCode(passportInfo.getCode());
        paymentOrder.setAmount(passportInfo.getAmount());
        paymentOrder.setBizType(BizTypeEnum.PASSPORT.getCode());
        paymentOrderService.insertSelective(paymentOrder);

        // 调用结算接口,缴费
        SettleOrderDto settleOrderDto = buildSettleOrderDto(userTicket, passportInfo, paymentOrder.getCode(), paymentOrder.getAmount());
        settlementRpcResolver.submit(settleOrderDto);

        return BaseOutput.success().setData(passportInfo);
    }

    /**
     * 取消通行证付款
     *
     * @param  id
     * @param userTicket
     * @return BaseOutput
     * @date   2020/7/27
     */
    @Override
    public BaseOutput<Passport> cancle(Long id, UserTicket userTicket)  throws Exception {
        // 先查询通行证
        Passport passportInfo = this.get(id);
        if (passportInfo == null) {
            throw new BusinessException(ResultCode.DATA_ERROR, "该水电费单号已不存在!");
        }

        // 已创建状态才能取消
        if (!PassportStateEnum.CREATED.getCode().equals(passportInfo.getState())) {
            throw new BusinessException(ResultCode.DATA_ERROR, "该状态不是已创建，不能取消");
        }

        passportInfo.setCancelerId(userTicket.getId());
        passportInfo.setCanceler(userTicket.getRealName());
        passportInfo.setCancelTime(LocalDateTime.now());
        passportInfo.setState(PassportStateEnum.CANCELLED.getCode());
        if (this.updateSelective(passportInfo) == 0) {
            throw new BusinessException(ResultCode.DATA_ERROR, "多人操作，请重试！");
        }

        return BaseOutput.success().setData(passportInfo);
    }

    /**
     * 撤回通行证缴费
     *
     * @param  id
     * @param userTicket
     * @return BaseOutput
     * @date   2020/7/27
     */
    @Override
    public BaseOutput<Passport> withdraw(Long id, UserTicket userTicket) throws Exception {
        // 先查询通行证
        Passport passportInfo = this.get(id);
        if (passportInfo == null) {
            throw new BusinessException(ResultCode.DATA_ERROR, "该水电费单号已不存在!");
        }

        // 已提交状态才能撤回
        if (!PassportStateEnum.SUBMITTED.getCode().equals(passportInfo.getState())) {
            throw new BusinessException(ResultCode.DATA_ERROR, "该状态不是已创建，不能取消");
        }

        passportInfo.setWithdrawOperatorId(userTicket.getId());
        passportInfo.setWithdrawOperator(userTicket.getRealName());
        passportInfo.setWithdrawTime(LocalDateTime.now());
        passportInfo.setState(PassportStateEnum.CREATED.getCode());
        if (this.updateSelective(passportInfo) == 0) {
            logger.info("撤回水电费【修改为已创建】失败.");
            throw new BusinessException(ResultCode.DATA_ERROR, "多人操作，请重试！");
        }

        // 撤销缴费单
        PaymentOrder paymentOrder = this.findPaymentOrder(userTicket, passportInfo.getId(), passportInfo.getCode());
        paymentOrder.setState(PaymentOrderStateEnum.CANCEL.getCode());
        if (paymentOrderService.updateSelective(paymentOrder) == 0) {
            logger.info("撤回水电费【删除缴费单】失败.");
            throw new BusinessException(ResultCode.DATA_ERROR, "多人操作，请重试！");
        }

        // 撤回结算单多人操作已判断
        settlementRpcResolver.cancel(settlementAppId, paymentOrder.getCode());

        return BaseOutput.success().setData(passportInfo);
    }

    /**
     * 通行证交费成功回调
     *
     * @param  settleOrder
     * @return BaseOutput
     * @date   2020/7/27
     */
    @Override
    public BaseOutput<Passport> settlementDealHandler(SettleOrder settleOrder) {

        // 修改缴费单相关数据
        if (null == settleOrder){
            throw new BusinessException(ResultCode.PARAMS_ERROR, "回调参数为空！");
        }
        PaymentOrder condition = DTOUtils.newInstance(PaymentOrder.class);
        //结算单code唯一
        condition.setCode(settleOrder.getOrderCode());
        condition.setBizType(BizTypeEnum.PASSPORT.getCode());
        PaymentOrder paymentOrderPO = paymentOrderService.listByExample(condition).stream().findFirst().orElse(null);
        Passport passportInfo = this.get(paymentOrderPO.getBusinessId());
        if (PaymentOrderStateEnum.PAID.getCode().equals(paymentOrderPO.getState())) { //如果已支付，直接返回
            return BaseOutput.success().setData(passportInfo);
        }
        if (!paymentOrderPO.getState().equals(PaymentOrderStateEnum.NOT_PAID.getCode())){
            logger.info("缴费单状态已变更！状态为：" + PaymentOrderStateEnum.getPaymentOrderStateEnum(paymentOrderPO.getState()).getName() );
            return BaseOutput.failure("缴费单状态已变更！");
        }

        //缴费单数据更新
        paymentOrderPO.setState(PaymentOrderStateEnum.PAID.getCode());
        paymentOrderPO.setPayedTime(settleOrder.getOperateTime());
        paymentOrderPO.setSettlementCode(settleOrder.getCode());
        paymentOrderPO.setSettlementOperator(settleOrder.getOperatorName());
        paymentOrderPO.setSettlementWay(settleOrder.getWay());
        if (paymentOrderService.updateSelective(paymentOrderPO) == 0) {
            logger.info("缴费单成功回调 -- 更新【缴费单】,乐观锁生效！【付款单paymentOrderID:{}】", paymentOrderPO.getId());
            throw new BusinessException(ResultCode.DATA_ERROR, "多人操作，请重试！");
        }

        // 修改通行证
        passportInfo.setModifyTime(LocalDateTime.now());
        // 判断交费后状态
        if(passportInfo.getStartTime() != null && LocalDateTime.now().isBefore(passportInfo.getStartTime())){
            passportInfo.setState(PassportStateEnum.NOT_START.getCode());
        } else if (passportInfo.getStartTime() != null && passportInfo.getEndTime() != null && passportInfo.getStartTime().isBefore(LocalDateTime.now())
            && LocalDateTime.now().isBefore(passportInfo.getEndTime())){
            passportInfo.setState(PassportStateEnum.IN_FORCE.getCode());
        } else if (passportInfo.getEndTime() != null && passportInfo.getEndTime().isBefore(LocalDateTime.now())) {
            passportInfo.setState(PassportStateEnum.EXPIRED.getCode());
        }

        if (this.updateSelective(passportInfo) == 0) {
            logger.info("缴费单成功回调 -- 更新【通行证】状态,乐观锁生效！【通行证Id:{}】", passportInfo.getId());
            throw new BusinessException(ResultCode.DATA_ERROR, "多人操作，请重试！");
        }

        return BaseOutput.success().setData(passportInfo);
    }

    /**
     * 通行证打印票据
     *
     * @param  orderCode
     * @return BaseOutput
     * @date   2020/7/27
     */
    @Override
    public PrintDataDto<PassportPrintDto> receiptPaymentData(String orderCode, Integer reprint) {
        PaymentOrder paymentOrderCondition = new PaymentOrder();

        paymentOrderCondition.setCode(orderCode);
        paymentOrderCondition.setBizType(BizTypeEnum.PASSPORT.getCode());
        PaymentOrder paymentOrder = paymentOrderService.list(paymentOrderCondition).stream().findFirst().orElse(null);
        if (null == paymentOrder) {
            throw new RuntimeException("businessCode无效");
        }
        if (!PaymentOrderStateEnum.PAID.getCode().equals(paymentOrder.getState())) {
            throw new BusinessException(ResultCode.DATA_ERROR, "此单未支付!");
        }

        // 组装数据
        Passport passportInfo = this.get(paymentOrder.getBusinessId());
        if (passportInfo == null) {
            throw new BusinessException(ResultCode.DATA_ERROR, "水电费单不存在!");
        }
        PassportPrintDto passportPrintDto = new PassportPrintDto();
        passportPrintDto.setPrintTime(LocalDateTime.now());
        passportPrintDto.setReprint(reprint == 2 ? "(补打)" : "");
        passportPrintDto.setCode(passportInfo.getCode());
        passportPrintDto.setCustomerName(passportInfo.getCustomerName());
        passportPrintDto.setCustomerCellphone(passportInfo.getCustomerCellphone());
        passportPrintDto.setStartTime(passportInfo.getStartTime());
        passportPrintDto.setEndTime(passportInfo.getEndTime());
        passportPrintDto.setNotes(passportInfo.getNotes());
        passportPrintDto.setAmount(MoneyUtils.centToYuan(passportInfo.getAmount()));
        passportPrintDto.setSettlementWay(SettleWayEnum.getNameByCode(paymentOrder.getSettlementWay()));
        passportPrintDto.setSettlementOperator(paymentOrder.getSettlementOperator());
        passportPrintDto.setSubmitter(paymentOrder.getCreator());
        passportPrintDto.setBusinessType(BizTypeEnum.PASSPORT.getName());

        PrintDataDto<PassportPrintDto> printDataDto = new PrintDataDto<>();
        printDataDto.setName(PrintTemplateEnum.PASSPORT.getCode());
        printDataDto.setItem(passportPrintDto);

        return printDataDto;
    }

    /**
     * 退款申请
     *
     * @param passportRefundOrderDto
     * @return BaseOutput
     * @date   2020/7/27
     */
    @Override
    public BaseOutput<Passport> refund(PassportRefundOrderDto passportRefundOrderDto) {
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();

        // 查询相关数据
        String code = passportRefundOrderDto.getBusinessCode();
        Passport passportInfo = this.getPassportByCode(code);
        if (PassportStateEnum.SUBMITTED_REFUND.getCode().equals(passportInfo.getState())) {
            throw new BusinessException(ResultCode.DATA_ERROR, "数据状态已改变,请刷新页面重试");
        }
        // 构建退款单以及新增
        RefundOrder refundOrder = buildRefundOrderDto(userTicket, passportInfo, passportRefundOrderDto);

        LoggerUtil.buildLoggerContext(passportInfo.getId(), passportInfo.getCode(), userTicket.getId(), userTicket.getRealName(), userTicket.getFirmId(), null);

        // 修改状态
        passportInfo.setModifyTime(LocalDateTime.now());
        passportInfo.setState(PassportStateEnum.SUBMITTED_REFUND.getCode());
        this.updateSelective(passportInfo);

        return BaseOutput.success().setData(passportInfo);
    }

    /**
     * 根据 code 查询通行证
     * 
     * @param  code
     * @return PassportDto
     * @date   2020/7/27
     */
    @Override
    public Passport getPassportByCode(String code) {
        return this.getActualDao().getPassportByCode(code);
    }

    /**
     * 构建结算实体类
     * @param userTicket
     * @param passportInfo
     * @param orderCode
     * @param amount
     * @return
     */
    private SettleOrderDto buildSettleOrderDto(UserTicket userTicket, Passport passportInfo, String orderCode, Long amount) {

        SettleOrderInfoDto settleOrderInfoDto = new SettleOrderInfoDto(userTicket, BizTypeEnum.PASSPORT, SettleTypeEnum.PAY, SettleStateEnum.WAIT_DEAL);
        settleOrderInfoDto.setMarketId(passportInfo.getMarketId());
        settleOrderInfoDto.setMarketCode(userTicket.getFirmCode());
        settleOrderInfoDto.setBusinessCode(passportInfo.getCode());
        settleOrderInfoDto.setOrderCode(orderCode);
        settleOrderInfoDto.setAmount(amount);
        settleOrderInfoDto.setAppId(settlementAppId);
        settleOrderInfoDto.setBusinessDepId(passportInfo.getDepartmentId());
        settleOrderInfoDto.setBusinessDepName(passportInfo.getDepartmentName());
        settleOrderInfoDto.setCustomerId(passportInfo.getCustomerId());
        settleOrderInfoDto.setCustomerName(passportInfo.getCustomerName());
        settleOrderInfoDto.setCustomerPhone(passportInfo.getCustomerCellphone());
        settleOrderInfoDto.setSubmitterId(userTicket.getId());
        settleOrderInfoDto.setSubmitterName(userTicket.getRealName());
        settleOrderInfoDto.setBusinessType(Integer.valueOf(BizTypeEnum.PASSPORT.getCode()));
        settleOrderInfoDto.setType(SettleTypeEnum.PAY.getCode());
        settleOrderInfoDto.setState(SettleStateEnum.WAIT_DEAL.getCode());
        settleOrderInfoDto.setReturnUrl(settlerHandlerUrl);
        if (userTicket.getDepartmentId() != null){
            settleOrderInfoDto.setSubmitterDepId(userTicket.getDepartmentId());
            settleOrderInfoDto.setSubmitterDepName(departmentRpc.get(userTicket.getDepartmentId()).getData().getName());
        }

        return settleOrderInfoDto;
    }

    /**
     * 根据条件查询缴费单
     * @param userTicket
     * @param businessId
     * @param businessCode
     * @return
     */
    private PaymentOrder findPaymentOrder(UserTicket userTicket, Long businessId, String businessCode){
        PaymentOrder pb = new PaymentOrder();

        pb.setBizType(BizTypeEnum.PASSPORT.getCode());
        pb.setBusinessId(businessId);
        pb.setBusinessCode(businessCode);
        pb.setMarketId(userTicket.getFirmId());
        pb.setState(PaymentOrderStateEnum.NOT_PAID.getCode());
        PaymentOrder order = paymentOrderService.listByExample(pb).stream().findFirst().orElse(null);
        if (null == order) {
            logger.info("没有查询到付款单PaymentOrder【业务单businessId：{}】 【业务单businessCode:{}】", businessId, businessCode);
            throw new BusinessException(ResultCode.DATA_ERROR, "没有查询到付款单！");
        }

        return order;
    }

    /**
     * 构建退款
     */
    private RefundOrder buildRefundOrderDto(UserTicket userTicket, Passport passportInfo, PassportRefundOrderDto passportRefundOrderDto) {
        //退款单
        RefundOrder refundOrder = new RefundOrder();

        refundOrder.setMarketId(userTicket.getFirmId());
        refundOrder.setMarketCode(userTicket.getFirmCode());

        refundOrder.setBusinessId(passportInfo.getId());
        refundOrder.setBusinessCode(passportInfo.getCode());
        refundOrder.setCustomerId(passportInfo.getCustomerId());
        refundOrder.setCustomerName(passportInfo.getCustomerName());
        refundOrder.setCustomerCellphone(passportInfo.getCustomerCellphone());
        refundOrder.setCertificateNumber(passportInfo.getCertificateNumber());

        refundOrder.setPayee(passportRefundOrderDto.getPayee());
        refundOrder.setPayeeId(passportRefundOrderDto.getPayeeId());
        refundOrder.setPayeeAmount(passportRefundOrderDto.getPayeeAmount());
        refundOrder.setRefundReason(passportRefundOrderDto.getRefundReason());
        refundOrder.setTotalRefundAmount(passportRefundOrderDto.getTotalRefundAmount());

        refundOrder.setBizType(BizTypeEnum.PASSPORT.getCode());
        refundOrder.setCode(uidRpcResolver.bizNumber(BizNumberTypeEnum.PASSPORT_REFUND.getCode()));

        if (!refundOrderService.doAddHandler(refundOrder).isSuccess()) {
            logger.info("通行证【编号：{}】退款申请接口异常", refundOrder.getBusinessCode());
            throw new BusinessException(ResultCode.DATA_ERROR, "退款申请接口异常");
        }
        return refundOrder;
    }
}