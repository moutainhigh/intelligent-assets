package com.dili.ia.service.impl;

import com.dili.assets.sdk.dto.BusinessChargeItemDto;
import com.dili.commons.glossary.YesOrNoEnum;
import com.dili.ia.domain.AssetsLeaseOrder;
import com.dili.ia.domain.AssetsLeaseOrderItem;
import com.dili.ia.domain.PaymentOrder;
import com.dili.ia.domain.dto.AssetsLeaseOrderItemListDto;
import com.dili.ia.domain.dto.printDto.LeaseOrderItemPrintDto;
import com.dili.ia.domain.dto.printDto.LeaseOrderPrintDto;
import com.dili.ia.domain.dto.printDto.PrintDataDto;
import com.dili.ia.glossary.AssetsTypeEnum;
import com.dili.ia.glossary.BizTypeEnum;
import com.dili.ia.glossary.PaymentOrderStateEnum;
import com.dili.ia.glossary.PrintTemplateEnum;
import com.dili.ia.rpc.SettlementRpc;
import com.dili.ia.service.*;
import com.dili.settlement.domain.SettleOrder;
import com.dili.settlement.domain.SettleWayDetail;
import com.dili.settlement.enums.SettleWayEnum;
import com.dili.ss.constant.ResultCode;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.exception.BusinessException;
import com.dili.ss.util.MoneyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 打印业务逻辑
 */
@Service
public class PrintServiceImpl implements PrintService {
    private final static Logger LOG = LoggerFactory.getLogger(PrintServiceImpl.class);

    @Autowired
    private AssetsLeaseOrderService assetsLeaseOrderService;
    @Autowired
    private AssetsLeaseOrderItemService assetsLeaseOrderItemService;
    @Autowired
    private SettlementRpc settlementRpc;
    @Autowired
    private PaymentOrderService paymentOrderService;
    @Autowired
    private BusinessChargeItemService businessChargeItemService;
    @Autowired
    DepositOrderService depositOrderService;

    @Override
    public BaseOutput<PrintDataDto> queryPrintLeaseOrderData(String orderCode, Integer reprint) {
        PaymentOrder paymentOrderCondition = new PaymentOrder();
        paymentOrderCondition.setCode(orderCode);
        PaymentOrder paymentOrder = paymentOrderService.list(paymentOrderCondition).stream().findFirst().orElse(null);
        if (null == paymentOrder) {
            LOG.info("租赁单打印异常 【businessCode:{}】无效", orderCode);
            throw new BusinessException(ResultCode.DATA_ERROR, "businessCode无效");
        }
        if (!PaymentOrderStateEnum.PAID.getCode().equals(paymentOrder.getState())) {
            LOG.info("租赁单打印异常 【businessCode:{}】此单未支付", orderCode);
            return BaseOutput.failure("此单未支付");
        }

        AssetsLeaseOrder leaseOrder = assetsLeaseOrderService.get(paymentOrder.getBusinessId());
        PrintDataDto<LeaseOrderPrintDto> printDataDto = new PrintDataDto<LeaseOrderPrintDto>();
        LeaseOrderPrintDto leaseOrderPrintDto = new LeaseOrderPrintDto();
        leaseOrderPrintDto.setPrintTime(LocalDate.now());
        leaseOrderPrintDto.setReprint(reprint == 2 ? "(补打)" : "");
        leaseOrderPrintDto.setLeaseOrderCode(leaseOrder.getCode());
        if (YesOrNoEnum.YES.getCode().equals(paymentOrder.getIsSettle())) {
            leaseOrderPrintDto.setBusinessType(BizTypeEnum.BOOTH_LEASE.getName());
            printDataDto.setName(PrintTemplateEnum.BOOTH_LEASE_PAID.getCode());
        } else {
            leaseOrderPrintDto.setBusinessType(BizTypeEnum.EARNEST.getName());
            printDataDto.setName(PrintTemplateEnum.BOOTH_LEASE_NOT_PAID.getCode());
        }
        leaseOrderPrintDto.setCustomerName(leaseOrder.getCustomerName());
        leaseOrderPrintDto.setCustomerCellphone(leaseOrder.getCustomerCellphone());
        leaseOrderPrintDto.setStartTime(leaseOrder.getStartTime().toLocalDate());
        leaseOrderPrintDto.setEndTime(leaseOrder.getEndTime().toLocalDate());
        leaseOrderPrintDto.setIsRenew(YesOrNoEnum.getYesOrNoEnum(leaseOrder.getIsRenew()).getName());
        leaseOrderPrintDto.setCategoryName(leaseOrder.getCategoryName());
        leaseOrderPrintDto.setNotes(leaseOrder.getNotes());
        leaseOrderPrintDto.setTotalAmount(MoneyUtils.centToYuan(leaseOrder.getTotalAmount()));
        leaseOrderPrintDto.setWaitAmount(MoneyUtils.centToYuan(leaseOrder.getWaitAmount()));

        Long totalPayAmountExcludeLast = 0L;
        //已结清时  定金需要加前几次支付金额
        if (YesOrNoEnum.YES.getCode().equals(paymentOrder.getIsSettle())) {
            PaymentOrder paymentOrderConditions = new PaymentOrder();
            paymentOrderConditions.setBusinessId(paymentOrder.getBusinessId());
            paymentOrderConditions.setBizType(BizTypeEnum.BOOTH_LEASE.getCode());
            List<PaymentOrder> paymentOrders = paymentOrderService.list(paymentOrderConditions);

            for (PaymentOrder order : paymentOrders) {
                if (!order.getCode().equals(orderCode) && order.getState().equals(PaymentOrderStateEnum.PAID.getCode())) {
                    totalPayAmountExcludeLast += order.getAmount();
                }
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
        leaseOrderPrintDto.setSettleWayDetails(this.buildSettleWayDetails(paymentOrder.getSettlementWay(), paymentOrder.getSettlementCode()));

        AssetsLeaseOrderItem leaseOrderItemCondition = new AssetsLeaseOrderItem();
        leaseOrderItemCondition.setLeaseOrderId(leaseOrder.getId());
        List<AssetsLeaseOrderItem> leaseOrderItems = assetsLeaseOrderItemService.list(leaseOrderItemCondition);
        List<BusinessChargeItemDto> chargeItemDtos = businessChargeItemService.queryBusinessChargeItemMeta(AssetsTypeEnum.getAssetsTypeEnum(leaseOrder.getAssetsType()).getBizType(), leaseOrderItems.stream().map(o -> o.getId()).collect(Collectors.toList()));
        leaseOrderPrintDto.setChargeItems(chargeItemDtos);
        List<AssetsLeaseOrderItemListDto> leaseOrderItemListDtos = assetsLeaseOrderItemService.leaseOrderItemListToDto(leaseOrderItems, AssetsTypeEnum.getAssetsTypeEnum(leaseOrder.getAssetsType()).getBizType(), chargeItemDtos);
        List<LeaseOrderItemPrintDto> leaseOrderItemPrintDtos = new ArrayList<>();
        leaseOrderItemListDtos.forEach(o -> {
            leaseOrderItemPrintDtos.add(leaseOrderItem2PrintDto(o));
        });
        leaseOrderPrintDto.setLeaseOrderItems(leaseOrderItemPrintDtos);
        printDataDto.setItem(leaseOrderPrintDto);
        return BaseOutput.success().setData(printDataDto);
    }



    /**
     * 订单项Bean转PrintDto
     *
     * @param leaseOrderItem
     * @return
     */
    private LeaseOrderItemPrintDto leaseOrderItem2PrintDto(AssetsLeaseOrderItemListDto leaseOrderItem) {
        LeaseOrderItemPrintDto leaseOrderItemPrintDto = new LeaseOrderItemPrintDto();
        leaseOrderItemPrintDto.setAssetsName(leaseOrderItem.getAssetsName());
        leaseOrderItemPrintDto.setDistrictName(leaseOrderItem.getDistrictName());
        leaseOrderItemPrintDto.setNumber(leaseOrderItem.getNumber().toString());
        leaseOrderItemPrintDto.setUnitName(leaseOrderItem.getUnitName());
        leaseOrderItemPrintDto.setUnitPrice(MoneyUtils.centToYuan(leaseOrderItem.getUnitPrice()));
        leaseOrderItemPrintDto.setIsCorner(leaseOrderItem.getIsCorner());
        leaseOrderItemPrintDto.setPaymentMonth(leaseOrderItem.getPaymentMonth().toString());
        leaseOrderItemPrintDto.setDiscountAmount(MoneyUtils.centToYuan(leaseOrderItem.getDiscountAmount()));
        leaseOrderItemPrintDto.setBusinessChargeItem(leaseOrderItem.getBusinessChargeItem());
        return leaseOrderItemPrintDto;
    }

    /**
     * 票据获取结算详情
     * 组合支付，结算详情格式 : 【微信 150.00 2020-08-19 4237458467568870 备注：微信付款150元;银行卡 150.00 2020-08-19 4237458467568870 备注：微信付款150元】
     * 园区卡支付，结算详情格式：【卡号：428838247888（李四）】
     * 除了园区卡 和 组合支付 ，结算详情格式：【2020-08-19 4237458467568870 备注：微信付款150元】
     * @param settlementWay 结算方式
     * @param settlementCode 结算详情
     * @return
     * */
    private String buildSettleWayDetails(Integer settlementWay, String settlementCode){
        //组合支付需要显示结算详情
        StringBuffer settleWayDetails = new StringBuffer();
        settleWayDetails.append("【");
        if (settlementWay.equals(SettleWayEnum.MIXED_PAY.getCode())){
            //摊位租赁单据的交款时间，也就是结算时填写的时间，显示到结算详情中，显示内容为：支付方式（组合支付的，只显示该类型下的具体支付方式）、金额、收款日期、流水号、结算备注，每个字段间隔一个空格；如没填写的则不显示；
            // 多个支付方式的，均在一行显示，当前行满之后换行，支付方式之间用;隔开；
            BaseOutput<List<SettleWayDetail>> output = settlementRpc.listSettleWayDetailsByCode(settlementCode);
            List<SettleWayDetail> swdList = output.getData();
            if (output.isSuccess() && CollectionUtils.isNotEmpty(swdList)){
                for(SettleWayDetail swd : swdList){
                    //此循环字符串拼接顺序不可修改，组合支付，结算详情格式 : 【微信 150.00 2020-08-19 4237458467568870 备注：微信付款150元;银行卡 150.00 2020-08-19 4237458467568870 备注：微信付款150元】
                    settleWayDetails.append(SettleWayEnum.getNameByCode(swd.getWay())).append(" ").append(MoneyUtils.centToYuan(swd.getAmount()));
                    if (null != swd.getChargeDate()){
                        settleWayDetails.append(" ").append(DateTimeFormatter.ofPattern("yyyy-MM-dd").format(swd.getChargeDate()));
                    }
                    if (StringUtils.isNotEmpty(swd.getSerialNumber())){
                        settleWayDetails.append(" ").append(swd.getSerialNumber());
                    }
                    if (StringUtils.isNotEmpty(swd.getNotes())){
                        settleWayDetails.append(" ").append("备注：").append(swd.getNotes());
                    }
                    settleWayDetails.append("；");
                }
                //去掉最后一个; 符
                settleWayDetails.replace(settleWayDetails.length()-1, settleWayDetails.length(), " ");
            }else {
                LOG.info("查询结算微服务组合支付，支付详情失败；原因：{}",output.getMessage());
            }
        } else if (settlementWay.equals(SettleWayEnum.CARD.getCode())){
            // 园区卡支付，结算详情格式：【卡号：428838247888（李四）】
            BaseOutput<SettleOrder> output = settlementRpc.getByCode(settlementCode);
            if(output.isSuccess()){
                SettleOrder settleOrder = output.getData();
                if (null != settleOrder.getTradeCardNo()){
                    settleWayDetails.append("卡号:" + settleOrder.getTradeCardNo());
                }
                if(StringUtils.isNotBlank(settleOrder.getTradeCustomerName())){
                    settleWayDetails.append("（").append(settleOrder.getTradeCustomerName()).append("）");
                }
            }else {
                LOG.info("查询结算微服务非组合支付，支付详情失败；原因：{}",output.getMessage());
            }
        }else{
            // 除了园区卡 和 组合支付 ，结算详情格式：【2020-08-19 4237458467568870 备注：微信付款150元】
            BaseOutput<SettleOrder> output = settlementRpc.getByCode(settlementCode);
            if(output.isSuccess()){
                SettleOrder settleOrder = output.getData();
                if (null != settleOrder.getChargeDate()){
                    settleWayDetails.append(DateTimeFormatter.ofPattern("yyyy-MM-dd").format(settleOrder.getChargeDate()));
                }
                if(StringUtils.isNotBlank(settleOrder.getSerialNumber())){
                    settleWayDetails.append(" ").append(settleOrder.getSerialNumber());
                }
                if (StringUtils.isNotBlank(settleOrder.getNotes())){
                    settleWayDetails.append(" ").append("备注：").append(settleOrder.getNotes());
                }
            }else {
                LOG.info("查询结算微服务非组合支付，支付详情失败；原因：{}",output.getMessage());
            }
        }
        settleWayDetails.append("】");
        if (StringUtils.isNotEmpty(settleWayDetails) && settleWayDetails.length() > 2){ // 长度大于2 是因为，避免内容为空，显示成 【】
            return settleWayDetails.toString();
        }
        return "";
    }

    @Override
    public BaseOutput<PrintDataDto> queryPrintContractSigningData(Long leaseOrderId) {
        AssetsLeaseOrder leaseOrder = assetsLeaseOrderService.get(leaseOrderId);

        PaymentOrder paymentOrder = paymentOrderService.get(leaseOrder.getPaymentId());

        PrintDataDto<LeaseOrderPrintDto> printDataDto = new PrintDataDto<LeaseOrderPrintDto>();
        printDataDto.setName(PrintTemplateEnum.BOOTH_LEASE_NOT_PAID.getCode());
        LeaseOrderPrintDto leaseOrderPrintDto = new LeaseOrderPrintDto();
        leaseOrderPrintDto.setPrintTime(LocalDate.now());
        leaseOrderPrintDto.setLeaseOrderCode(leaseOrder.getCode());
        leaseOrderPrintDto.setCustomerName(leaseOrder.getCustomerName());
        leaseOrderPrintDto.setCustomerCellphone(leaseOrder.getCustomerCellphone());
        leaseOrderPrintDto.setStartTime(leaseOrder.getStartTime().toLocalDate());
        leaseOrderPrintDto.setEndTime(leaseOrder.getEndTime().toLocalDate());
        leaseOrderPrintDto.setIsRenew(YesOrNoEnum.getYesOrNoEnum(leaseOrder.getIsRenew()).getName());
        leaseOrderPrintDto.setCategoryName(leaseOrder.getCategoryName());
        leaseOrderPrintDto.setNotes(leaseOrder.getNotes());
        leaseOrderPrintDto.setTotalAmount(MoneyUtils.centToYuan(leaseOrder.getTotalAmount()));
        leaseOrderPrintDto.setWaitAmount(MoneyUtils.centToYuan(leaseOrder.getWaitAmount()));


        //除最后一次所交费用+定金抵扣 之和未总定金
        leaseOrderPrintDto.setEarnestDeduction(MoneyUtils.centToYuan(leaseOrder.getEarnestDeduction()));
        leaseOrderPrintDto.setTransferDeduction(MoneyUtils.centToYuan(leaseOrder.getTransferDeduction()));
        leaseOrderPrintDto.setPayAmount(MoneyUtils.centToYuan(leaseOrder.getPayAmount()));
        leaseOrderPrintDto.setAmount(MoneyUtils.centToYuan(paymentOrder.getAmount()));
        leaseOrderPrintDto.setSubmitter(paymentOrder.getCreator());

        AssetsLeaseOrderItem leaseOrderItemCondition = new AssetsLeaseOrderItem();
        leaseOrderItemCondition.setLeaseOrderId(leaseOrder.getId());
        List<AssetsLeaseOrderItem> leaseOrderItems = assetsLeaseOrderItemService.list(leaseOrderItemCondition);
        List<BusinessChargeItemDto> chargeItemDtos = businessChargeItemService.queryBusinessChargeItemMeta(AssetsTypeEnum.getAssetsTypeEnum(leaseOrder.getAssetsType()).getBizType(), leaseOrderItems.stream().map(o -> o.getId()).collect(Collectors.toList()));
        leaseOrderPrintDto.setChargeItems(chargeItemDtos);
        List<AssetsLeaseOrderItemListDto> leaseOrderItemListDtos = assetsLeaseOrderItemService.leaseOrderItemListToDto(leaseOrderItems, AssetsTypeEnum.getAssetsTypeEnum(leaseOrder.getAssetsType()).getBizType(), chargeItemDtos);
        List<LeaseOrderItemPrintDto> leaseOrderItemPrintDtos = new ArrayList<>();
        leaseOrderItemListDtos.forEach(o -> {
            leaseOrderItemPrintDtos.add(leaseOrderItem2PrintDto(o));
        });
        leaseOrderPrintDto.setLeaseOrderItems(leaseOrderItemPrintDtos);
        printDataDto.setItem(leaseOrderPrintDto);
        return BaseOutput.success().setData(printDataDto);
    }
}
