package com.dili.ia.domain.dto;

import com.dili.ia.domain.EarnestOrder;
import com.dili.ia.domain.EarnestOrderDetail;
import com.dili.ss.domain.annotation.Like;
import com.dili.ss.domain.annotation.Operator;

import javax.persistence.Column;
import javax.persistence.Transient;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <B>Description</B>
 * 本软件源代码版权归农丰时代及其团队所有,未经许可不得任意复制与传播
 * <B>农丰时代科技有限公司</B>
 *
 * @author qinkelan
 * @createTime 2020-02-18 15:28
 */
public class EarnestOrderListDto extends EarnestOrder {
    @Column(name = "`create_time`")
    @Operator(Operator.GREAT_EQUAL_THAN)
    private LocalDateTime createdStart;

    @Column(name = "`create_time`")
    @Operator(Operator.LITTLE_EQUAL_THAN)
    private LocalDateTime createdEnd;

    /**
     * 昵称模糊查询
     * @return
     */
    @Column(name = "customer_name")
    @Like
    private String likeCustomerName;

    @Operator(Operator.IN)
    @Column(name = "department_id")
    private List<Long> departmentIds;

    @Transient
    private List<EarnestOrderDetail> earnestOrderdetails;

    @Transient
    private String logContent;

    public LocalDateTime getCreatedStart() {
        return createdStart;
    }

    public void setCreatedStart(LocalDateTime createdStart) {
        this.createdStart = createdStart;
    }

    public LocalDateTime getCreatedEnd() {
        return createdEnd;
    }

    public void setCreatedEnd(LocalDateTime createdEnd) {
        this.createdEnd = createdEnd;
    }

    public String getLikeCustomerName() {
        return likeCustomerName;
    }

    public void setLikeCustomerName(String likeCustomerName) {
        this.likeCustomerName = likeCustomerName;
    }

    public List<Long> getDepartmentIds() {
        return departmentIds;
    }

    public void setDepartmentIds(List<Long> departmentIds) {
        this.departmentIds = departmentIds;
    }

    public List<EarnestOrderDetail> getEarnestOrderdetails() {
        return earnestOrderdetails;
    }

    public void setEarnestOrderdetails(List<EarnestOrderDetail> earnestOrderdetails) {
        this.earnestOrderdetails = earnestOrderdetails;
    }

    public String getLogContent() {
        return logContent;
    }

    public void setLogContent(String logContent) {
        this.logContent = logContent;
    }
}
