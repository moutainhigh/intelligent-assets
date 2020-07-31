<script>
    /**
     *
     * @Date 2019-11-06 17:30:00
     * @author jiangchengyong
     *
     ***/

    /*********************变量定义区 begin*************/
        //行索引计数器
        //如 let itemIndex = 0;
    let _grid = $('#grid');
    let _form = $('#_form');
    let _modal = $('#_modal');
    let currentSelectRowIndex;
    var dia;

    /*********************变量定义区 end***************/


    /******************************驱动执行区 begin***************************/
    $(function () {
        $(window).resize(function () {
            _grid.bootstrapTable('resetView')
        });
        let size = ($(window).height() - $('#queryForm').height() - 210) / 40;
        size = size > 10 ? size : 10;
        _grid.bootstrapTable('refreshOptions', {url: '/assetsLeaseOrder/listPage.action',pageSize: parseInt(size)});
    });


    /******************************驱动执行区 end****************************/

    /*****************************************函数区 begin************************************/
    /**
     * 打开新增窗口
     */
    function openInsertHandler() {
       dia = bs4pop.dialog({
            title: '新增租赁',
           content: '/assetsLeaseOrder/preSave.html?assetsType=' + $('#assetsType').val(),
            isIframe : true,
            closeBtn: true,
            backdrop : 'static',
            width: '95%',
            height : '95%',
            btns: []
        });
    }

    /**
     * 打开修改Handler
     */
    function openUpdateHandler() {
        //获取选中行的数据
        let rows = _grid.bootstrapTable('getSelections');
        if (null == rows || rows.length == 0) {
            bs4pop.alert('请选中一条数据');
            return;
        }

        dia = bs4pop.dialog({
            title: '修改租赁',
            content: '/assetsLeaseOrder/preSave.html?id=' + rows[0].id + '&assetsType=' + $('#assetsType').val(),
            isIframe : true,
            closeBtn: true,
            backdrop : 'static',
            width: '95%',
            height : '95%',
            btns: []
        });
    }

    /**
     * 打开查看
     * @param id
     */
    function openViewHandler(id) {
        if(!id){
            //获取选中行的数据
            let rows = _grid.bootstrapTable('getSelections');
            if (null == rows || rows.length == 0) {
                bs4pop.alert('请选中一条数据');
                return;
            }
            id = rows[0].id;
        }


        dia = bs4pop.dialog({
            title: '摊位租赁详情',
            content: '/assetsLeaseOrder/view.action?id='+id,
            isIframe : true,
            closeBtn: true,
            backdrop : 'static',
            width: '95%',
            height : '95%',
            btns: [{label: '关闭', className: 'btn-secondary', onClick(e) {}}]
        });
    }

    /**
     * 业务编号formatter
     * @param value
     * @param row
     * @param index
     * @returns {string}
     */
    function codeFormatter(value,row,index) {
        return '<a href="javascript:openViewHandler('+row.id+')">'+value+'</a>';
    }

    /**
     * 打开补录Handler
     */
    function openSupplementHandler() {
        //获取选中行的数据
        let rows = _grid.bootstrapTable('getSelections');
        if (null == rows || rows.length == 0) {
            bs4pop.alert('请选中一条数据');
            return;
        }

        bs4pop.dialog({
            title: '补录',
            content: template('supplementTpl',{}),
            closeBtn: true,
            backdrop : 'static',
            width: '40%',
            btns: [
                {
                    label: '确定', className: 'btn-primary', onClick(e) {
                        if (!$('#supplementForm').valid()) {
                            return false;
                        }
                        bui.loading.show('努力提交中，请稍候。。。');
                        $.ajax({
                            type: "POST",
                            url: "${contextPath}/assetsLeaseOrder/supplement.action",
                            data: {id: rows[0].id,contractNo : $('#contractNo').val()},
                            processData:true,
                            dataType: "json",
                            success : function(data) {
                                bui.loading.hide();
                                if(!data.success){
                                    bs4pop.alert(data.result, {type: 'error'});
                                }
                            },
                            error : function() {
                                bui.loading.hide();
                                bs4pop.alert('远程访问失败', {type: 'error'});
                            }
                        });
                    }
                },
                {label: '取消', className: 'btn-default', onClick(e) {}}
            ]
        });
    }

    /**
     * 打开取消Handler
     */
    function openCancelHandler() {
        //获取选中行的数据
        let rows = _grid.bootstrapTable('getSelections');
        if (null == rows || rows.length == 0) {
            bs4pop.alert('请选中一条数据');
            return;
        }

        let selectedRow = rows[0];
        bs4pop.confirm('确定取消该业务单？', undefined, function (sure) {
            if(sure){
                bui.loading.show('努力提交中，请稍候。。。');
                $.ajax({
                    type: "POST",
                    url: "${contextPath}/assetsLeaseOrder/cancelOrder.action",
                    data: {id: selectedRow.id},
                    dataType: "json",
                    success : function(ret) {
                        bui.loading.hide();
                        if(ret.success){
                            queryDataHandler();
                        }else{
                            bs4pop.alert(ret.message, {type: 'error'});
                        }
                    },
                    error : function() {
                        bui.loading.hide();
                        bs4pop.alert('远程访问失败', {type: 'error'});
                    }
                });
            }

        })

    }

    /**
     * 打开撤回Handler
     */
    function openWithdrawHandler() {
        //获取选中行的数据
        let rows = _grid.bootstrapTable('getSelections');
        if (null == rows || rows.length == 0) {
            bs4pop.alert('请选中一条数据');
            return;
        }

        let selectedRow = rows[0];
        bs4pop.confirm('撤回之后该业务单可继续修改，但不能交费，如需继续交费可以再次提交。确定撤回？', undefined, function (sure) {
            if(sure){
                bui.loading.show('努力提交中，请稍候。。。');
                $.ajax({
                    type: "POST",
                    url: "${contextPath}/assetsLeaseOrder/withdrawOrder.action",
                    data: {id: selectedRow.id},
                    dataType: "json",
                    success : function(ret) {
                        bui.loading.hide();
                        if(ret.success){
                            queryDataHandler();
                        }else{
                            bs4pop.alert(ret.message, {type: 'error'});
                        }
                    },
                    error : function() {
                        bui.loading.hide();
                        bs4pop.alert('远程访问失败', {type: 'error'});
                    }
                });
            }
        })

    }

    /**
     * 打开提交付款Handler
     */
    function openSubmitPaymentHandler() {
        //获取选中行的数据
        let rows = _grid.bootstrapTable('getSelections');
        if (null == rows || rows.length == 0) {
            bs4pop.alert('请选中一条数据');
            return;
        }
        dia = bs4pop.dialog({
            title: '提交付款',
            content: '/assetsLeaseOrder/submitPayment.html?id='+rows[0].id,
            isIframe : true,
            closeBtn: true,
            backdrop : 'static',
            width: '850px',
            height : '550px',
            btns: []
        });
    }

    /**
     * 打开续租Handler
     */
    function openRenewHandler() {
        //获取选中行的数据
        let rows = _grid.bootstrapTable('getSelections');
        if (null == rows || rows.length == 0) {
            bs4pop.alert('请选中一条数据');
            return;
        }

        dia = bs4pop.dialog({
            title: '摊位续租',
            content: '/assetsLeaseOrder/preSave.html?isRenew=1&id=' + rows[0].id + '&assetsType=' + rows[0].assetsType,
            isIframe : true,
            closeBtn: true,
            backdrop : 'static',
            width: '95%',
            height : '95%',
            btns: []
        });
    }

    /**
     *  显示流程图
     */
    function showProgress() {
        //获取选中行的数据
        let rows = _grid.bootstrapTable('getSelections');
        if (null == rows || rows.length == 0) {
            bs4pop.alert('请选中一条数据');
            return;
        }
        let selected = rows[0];
        //var href = '<#config name="bpmc.server.address"/>/api/runtime/progress?processInstanceId='+selected.processInstanceId+'&processDefinitionId='+selected.processDefinitionId+"&"+Math.random();
        //$("#processInstanceImg").attr("src", href);
        let url = '<#config name="bpmc.server.address"/>/api/runtime/progress?processInstanceId='+selected.processInstanceId+'&processDefinitionId='+selected.processDefinitionId+"&"+Math.random();
        dia = bs4pop.dialog({
            title: '流程图',
            content: url,
            isIframe : true,
            closeBtn: true,
            backdrop : 'static',
            width: '95%',
            height : '95%',
            btns: []
        });
    }
    /**
     *  提交审批
     */
    function submitForApproval(){
        //获取选中行的数据
        let rows = _grid.bootstrapTable('getSelections');
        if (null == rows || rows.length == 0) {
            bs4pop.alert('请选中一条数据');
            return;
        }
        let selectedRow = rows[0];
        bs4pop.confirm('确定提交审批？', undefined, function (sure) {
            if(sure){
                bui.loading.show('努力提交中，请稍候。。。');
                $.ajax({
                    type: "POST",
                    url: "${contextPath}/assetsLeaseOrder/submitForApproval.action",
                    data: {id: selectedRow.id},
                    dataType: "json",
                    success : function(ret) {
                        bui.loading.hide();
                        if(ret.success){
                            queryDataHandler();
                        }else{
                            bs4pop.alert(ret.message, {type: 'error'});
                        }
                    },
                    error : function() {
                        bui.loading.hide();
                        bs4pop.alert('远程访问失败', {type: 'error'});
                    }
                });
            }

        })
    }

    /**
     * 摊位订单停租
     * @returns {boolean}
     */
    function openStopRentHandler(subTableIndex) {
        //获取选中行的数据
        let rows = $('#subGrid'+subTableIndex).bootstrapTable('getSelections');
        if (null == rows || rows.length == 0) {
            bs4pop.alert('请选中一条数据');
            return;
        }

        let leaseOrder = _grid.bootstrapTable('getRowByUniqueId',rows[0].leaseOrderId);

        let dia = bs4pop.dialog({
            title: '停租',
            content: bui.util.HTMLDecode(template('stopRentTpl',$.extend({},rows[0],{endTime : leaseOrder.endTime}))),
            closeBtn: true,
            backdrop : 'static',
            width: '40%',
            onShowEnd(){
                let now = moment(new Date()).format("YYYY-MM-DD");
                let minDate = moment(now).isBefore(leaseOrder.startTime)?leaseOrder.startTime : now;
                laydate.render({
                        elem: '#stopTime',
                        type: 'date',
                        theme: '#007bff',
                        trigger:'click',
                        min : minDate,
                        done: function(value, date){
                            $("#stopRentForm").validate().element($("#stopTime"));
                        }
                });
                $('#stopWay').on('change',':radio',function () {
                    $('#stopDateSelect').toggle();
                });
                $('#stopRentForm').validate({
                    rules: {
                        stopTime: {
                            required: true,
                            minDate: minDate,
                            maxDate: leaseOrder.endTime
                        }
                    }
                });
            },
            btns: [
                {
                    label: '确定', className: 'btn-primary', onClick(e) {
                        if (!$('#stopRentForm').valid()) {
                            return false;
                        }
                        bui.loading.show('努力提交中，请稍候。。。');
                        $.ajax({
                            type: "POST",
                            url: "${contextPath}/leaseOrderItem/stopRent.action",
                            data: $('#stopRentForm').serializeObject(),
                            dataType: "json",
                            success : function(data) {
                                bui.loading.hide();
                                queryDataHandler();
                                if(!data.success){
                                    bs4pop.alert(data.result, {type: 'error'});
                                }else{
                                    dia.hide();
                                }
                            },
                            error : function() {
                                bui.loading.hide();
                                bs4pop.alert('远程访问失败', {type: 'error'});
                            }
                        });
                        return false;
                    }
                },
                {label: '取消', className: 'btn-default', onClick(e) {}}
            ]
        });
    }

    /**
     * 打开退款申请Handler
     * @param type 1：租赁单退款 2： 子单退款
     * @param subTableIndex
     */
    function openRefundApplyHandler(type,subTableIndex) {
        //获取选中行的数据
        let rows;
        if(type == 1){
            rows = _grid.bootstrapTable('getSelections');
        }else if(type == 2){
            rows = $('#subGrid' + subTableIndex).bootstrapTable('getSelections');
        }
        if (null == rows || rows.length == 0) {
            bs4pop.alert('请选中一条数据');
            return;
        }
        let url = '/assetsLeaseOrder/refundApply.html?id=' + rows[0].id + '&type=' + type;
        dia = bs4pop.dialog({
            title: '退款申请',
            content: url,
            isIframe : true,
            closeBtn: true,
            backdrop : 'static',
            width: '95%',
            height : '95%',
            btns: []
        });
    }

    /**
     * 查询处理
     */
    function queryDataHandler() {
        currentSelectRowIndex = undefined;
        $('#toolbar button').attr('disabled', false);
        _grid.bootstrapTable('refreshOptions', {pageNumber: 1});
    }

    /**
     * table参数组装
     * 可修改queryParams向服务器发送其余的参数
     * @param params
     */
    function queryParams(params) {
        let temp = {
            rows: params.limit,   //页面大小
            page: ((params.offset / params.limit) + 1) || 1, //页码
            sort: params.sort,
            order: params.order
        }
        return $.extend(temp, bui.util.bindGridMeta2Form('grid', 'queryForm'));
    }

    /**
     * table参数组装
     * 可修改queryParams向服务器发送其余的参数
     * @param params
     */
    function subQueryParams(params) {
        let temp = {
            rows: params.limit,   //页面大小
            page: ((params.offset / params.limit) + 1) || 1, //页码
            sort: params.sort,
            order: params.order
        }
        return $.extend(temp, bui.util.bindMetadata(this.id), {assetsType: $('#assetsType').val()});
    }

    /**
     * 关闭弹窗
     */
    function closeDialog(dialog){
        dialog.hide();
        queryDataHandler();
    }

    /*****************************************函数区 end**************************************/

    /*****************************************自定义事件区 begin************************************/
    //表单弹框关闭事件
    // _modal.on('hidden.bs.modal', function () {
    //     _form[0].reset();
    //     //重置表单验证到初始状态
    //     $(this).find('input,select,textarea').removeClass('is-invalid is-valid');
    //     $(this).find('input,select,textarea').removeAttr('disabled readonly');
    //     $(this).find('.invalid-feedback').css('display','none');
    // });

    //展开事件
    _grid.on('expand-row.bs.table', function (e,index, row, $detail){
        //展开选中行
        // if(currentSelectRowIndex != index){
        //     _grid.bootstrapTable('collapseRow', currentSelectRowIndex);
        //     currentSelectRowIndex = index;
        //     _grid.bootstrapTable('check',index);
        // }


        var cur_table = $detail.html(template('subTable',{index})).find('table');
        $(cur_table).bootstrapTable();
        $(cur_table).bootstrapTable('refreshOptions', {url: '/assetsLeaseOrderItem/listPage.action?leaseOrderId='+row.id});
        //选中行事件
        $(cur_table).on('check.bs.table', function (e,row, $element){
            e.stopPropagation();
            let state = row.$_state;
            $('#toolbar'+index+' button').attr('disabled', true);
            //（未生效 || 已生效）&& 已交清方可停租
            if ((state == ${@com.dili.ia.glossary.LeaseOrderStateEnum.NOT_ACTIVE.getCode()}
                || state == ${@com.dili.ia.glossary.LeaseOrderStateEnum.EFFECTIVE.getCode()})
                && row.$_refundState == ${@com.dili.ia.glossary.LeaseRefundStateEnum.WAIT_APPLY.getCode()}
            ) {
                $('#btn_stop_rent'+index).attr('disabled', false);
            }

            if(row.$_refundState == ${@com.dili.ia.glossary.LeaseRefundStateEnum.WAIT_APPLY.getCode()}){
                $('#btn_refund_apply'+index).attr('disabled', false);
            }
        });
    });

    //选中行事件
    _grid.on('uncheck.bs.table', function (e, row, $element) {
        currentSelectRowIndex = undefined;
    });

    //选中行事件
    _grid.on('check.bs.table', function (e, row, $element) {
        // let newIndex = $element.parents('tr').data('index');
        // if (currentSelectRowIndex !== newIndex) {
        //     _grid.bootstrapTable('collapseRow', currentSelectRowIndex);
        //     _grid.bootstrapTable('expandRow', newIndex);
        //     currentSelectRowIndex = newIndex;
        // }

        let state = row.$_state;
        //审批状态
        let approvalState = row.$_approvalState;
        if (state == ${@com.dili.ia.glossary.LeaseOrderStateEnum.CREATED.getCode()}) {
            $('#toolbar button').attr('disabled', true);
            $('#btn_view').attr('disabled', false);
            $('#btn_add').attr('disabled', false);
            //没有审批状态可以 提交审批，修改和取消
            if(!approvalState){
                $('#btn_approval').attr('disabled', false);
                $('#btn_edit').attr('disabled', false);
                $('#btn_cancel').attr('disabled', false);
                return;
            }
            //待审批时可以 提交审批，修改和取消
            else if(approvalState == ${@com.dili.ia.glossary.ApprovalStateEnum.WAIT_SUBMIT_APPROVAL.getCode()}){
                $('#btn_approval').attr('disabled', false);
                $('#btn_edit').attr('disabled', false);
                $('#btn_cancel').attr('disabled', false);
            }
            //审批中不允许修改、取消和提交付款
            else if(approvalState == ${@com.dili.ia.glossary.ApprovalStateEnum.IN_REVIEW.getCode()}) {
            }
            //审批通过后不能修改和取消，可以提交付款
            else if(approvalState == ${@com.dili.ia.glossary.ApprovalStateEnum.APPROVED.getCode()}){
                $('#btn_submit').attr('disabled', false);
            }
            //审批拒绝后 可以再次提交审批，修改和取消
            else if(approvalState == ${@com.dili.ia.glossary.ApprovalStateEnum.APPROVAL_DENIED.getCode()}){
                $('#btn_approval').attr('disabled', false);
                $('#btn_edit').attr('disabled', false);
                $('#btn_cancel').attr('disabled', false);
            }
        } else if (state == ${@com.dili.ia.glossary.LeaseOrderStateEnum.CANCELD.getCode()}) {
            $('#toolbar button').attr('disabled', true);
            $('#btn_view').attr('disabled', false);
            $('#btn_add').attr('disabled', false);
        } else if (state == ${@com.dili.ia.glossary.LeaseOrderStateEnum.SUBMITTED.getCode()}) {
            $('#toolbar button').attr('disabled', true);
            $('#btn_view').attr('disabled', false);
            $('#btn_add').attr('disabled', false);
            if(row.$_payState == ${@com.dili.ia.glossary.PayStateEnum.NOT_PAID.getCode()}){
                $('#btn_submit').attr('disabled', false);
            }
            $('#btn_withdraw').attr('disabled', false);
        } else if (state == ${@com.dili.ia.glossary.LeaseOrderStateEnum.NOT_ACTIVE.getCode()}
            || state == ${@com.dili.ia.glossary.LeaseOrderStateEnum.EFFECTIVE.getCode()}) {
            $('#toolbar button').attr('disabled', true);
            $('#btn_view').attr('disabled', false);
            $('#btn_add').attr('disabled', false);
            $('#btn_supplement').attr('disabled', false);
            $('#btn_renew').attr('disabled', false);
            if (row.$_payState == ${@com.dili.ia.glossary.PayStateEnum.NOT_PAID.getCode()}
                && row.$_refundState != ${@com.dili.ia.glossary.LeaseRefundStateEnum.REFUNDED.getCode()}
                && row.$_refundState != ${@com.dili.ia.glossary.LeaseRefundStateEnum.REFUNDING.getCode()}) {
                $('#btn_submit').attr('disabled', false);
            }
        } else if (state == ${@com.dili.ia.glossary.LeaseOrderStateEnum.RENTED_OUT.getCode()}) {
            $('#toolbar button').attr('disabled', true);
            $('#btn_view').attr('disabled', false);
            $('#btn_add').attr('disabled', false);
            $('#btn_supplement').attr('disabled', false);
            if (row.$_payState == ${@com.dili.ia.glossary.PayStateEnum.NOT_PAID.getCode()}
                && row.$_refundState != ${@com.dili.ia.glossary.LeaseRefundStateEnum.REFUNDED.getCode()}
                && row.$_refundState != ${@com.dili.ia.glossary.LeaseRefundStateEnum.REFUNDING.getCode()}) {
                $('#btn_submit').attr('disabled', false);
            }
        }else if (state == ${@com.dili.ia.glossary.LeaseOrderStateEnum.EXPIRED.getCode()}) {
            $('#toolbar button').attr('disabled', true);
            $('#btn_view').attr('disabled', false);
            $('#btn_add').attr('disabled', false);
            $('#btn_renew').attr('disabled', false);
            $('#btn_supplement').attr('disabled', false);
            if (row.$_payState == ${@com.dili.ia.glossary.PayStateEnum.NOT_PAID.getCode()}
                && row.$_refundState != ${@com.dili.ia.glossary.LeaseRefundStateEnum.REFUNDED.getCode()}
                && row.$_refundState != ${@com.dili.ia.glossary.LeaseRefundStateEnum.REFUNDING.getCode()}) {
                $('#btn_submit').attr('disabled', false);
            }
        }
        //只能有流程实例id就可以查看流程图
        if(row.processInstanceId) {
            $("#btn_showProgress").attr('disabled', false);
        }
    });
    /*****************************************自定义事件区 end**************************************/
</script>