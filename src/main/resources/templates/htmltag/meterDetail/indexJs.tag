<script>

    /*********************变量定义区 begin*************/
//行索引计数器
//如 let itemIndex = 0;
    let _grid = $('#grid');
    let _form = $('#_form');
    let _modal = $('#_modal');
    var dia;
    /*********************变量定义区 end***************/


    /******************************驱动执行区 begin***************************/
    $(function () {
        $(window).resize(function () {
            _grid.bootstrapTable('resetView')
        });
        queryDataHandler();

        lay('.laymonth').each(function () {
            laydate.render({
                elem: this,
                trigger: 'click',
                type: 'month',
                theme: '#007bff',
                done: function () {
                }
            });
        });
    });
    /******************************驱动执行区 end****************************/

    /*****************************************函数区 begin************************************/

    /**
     * 打开查看
     * @param id
     */
    function openViewHandler(id) {
        let _formData = $('#queryForm').serializeObject();
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
            title: '费用详情',
            content: '/meterDetail/view.action?id='+id,
            isIframe : true,
            closeBtn: true,
            backdrop : 'static',
            width: '80%',
            height : '95%',
            btns: [{label: '关闭', className: 'btn-secondary', onClick(e) {}}]
        });
    }

    /**
     * 查询处理
     */
    function queryDataHandler() {
        let meterType = $('#metertype').val();
        _grid.bootstrapTable('refreshOptions', {pageNumber: 1, url: '/meterDetail/listPage.action?meterType=' + meterType});
    }

    /**
     * table参数组装
     * 可修改queryParams向服务器发送其余的参数
     * 前置 table初始化时 queryParams方法拿不到option对象，需要在页面加载时初始化option
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
     打开新增水费窗口
     */
    function openWaterInsertHandler() {
        let _formData = $('#saveForm').serializeObject();
        let meterType = $('#metertype').val();
        dia = bs4pop.dialog({
            title: '费用新增',//对话框title
            content: '${contextPath}/meterDetail/water/add.html',//对话框内容，可以是 string、element，$object
            width: '80%',//宽度
            height: '95%',//高度
            isIframe: true,//默认是页面层，非iframe
        });
    }

    /**
     打开新增电费窗口
     */
    function openElectricityInsertHandler() {
        let _formData = $('#saveForm').serializeObject();
        let meterType = $('#metertype').val();
        dia = bs4pop.dialog({
            title: '费用新增',//对话框title
            content: '${contextPath}/meterDetail/electricity/add.html',//对话框内容，可以是 string、element，$object
            width: '80%',//宽度
            height: '95%',//高度
            isIframe: true,//默认是页面层，非iframe
        });
    }


    /**
     打开更新水费窗口
     */
    function openWaterUpdateHandler() {
        //获取选中行的数据
        let rows = _grid.bootstrapTable('getSelections');
        if (null == rows || rows.length == 0 || rows.length != 1) {
            bs4pop.alert('请选中一条数据');
            return false;
        }
        dia = bs4pop.dialog({
            title: '修改水费',//对话框title
            content: '${contextPath}/meterDetail/water/update.html?id='+rows[0].id, //对话框内容，可以是 string、element，$object
            width: '80%',//宽度
            height: '500px',//高度
            isIframe: true,//默认是页面层，非iframe
            //按钮放在父页面用此处的 btns 选项。也可以放在页面里直接在页面写div。
            btns: [{label: '取消',className: 'btn-secondary',onClick(e, $iframe){
                }
            }, {label: '确定',className: 'btn-primary',onClick(e, $iframe){
                    let diaWindow = $iframe[0].contentWindow;
                    bui.util.debounce(diaWindow.saveOrUpdateHandler,1000,true)()
                    return false;
                }
            }]
        });
    }


    /**
     打开更新电费窗口
     */
    function openElectricityUpdateHandler() {
        //获取选中行的数据
        let rows = _grid.bootstrapTable('getSelections');
        if (null == rows || rows.length == 0 || rows.length != 1) {
            bs4pop.alert('请选中一条数据');
            return false;
        }
        dia = bs4pop.dialog({
            title: '修改水费',//对话框title
            content: '${contextPath}/meterDetail/electricity/update.html?id='+rows[0].id, //对话框内容，可以是 string、element，$object
            width: '80%',//宽度
            height: '500px',//高度
            isIframe: true,//默认是页面层，非iframe
            //按钮放在父页面用此处的 btns 选项。也可以放在页面里直接在页面写div。
            btns: [{label: '取消',className: 'btn-secondary',onClick(e, $iframe){
                }
            }, {label: '确定',className: 'btn-primary',onClick(e, $iframe){
                    let diaWindow = $iframe[0].contentWindow;
                    bui.util.debounce(diaWindow.saveOrUpdateHandler,1000,true)()
                    return false;
                }
            }]
        });
    }


    /**
     提交处理
     */
    function openSubmitHandler() {
        if(isSelectRow()){
            bs4pop.confirm('提交后该信息不可更改，并且可进行缴费，确认提交？', {}, function (sure) {
                if(sure){
                    bui.util.debounce(function () {
                        bui.loading.show('努力提交中，请稍候。。。');
                        //获取选中行的数据
                        let rows = _grid.bootstrapTable('getSelections');
                        let ids =[];
                        rows.forEach(item =>{
                            ids.push(item.id)
                        })

                        $.ajax({
                            type: "POST",
                            url: "${contextPath}/meterDetail/submit.action",
                            data: {ids:ids.join()},
                            processData:true,
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
                    },1000,true)();
                }
            })
        }
    }

    /**
     全部水费单提交处理
     */
    function openSubmitAllWaterHandler() {
            bs4pop.confirm('所有已创建的都提交，提交后该信息不可更改，并且可进行缴费，确认提交？', {}, function (sure) {
                if(sure){
                    bui.util.debounce(function () {


                        bui.loading.show('努力提交中，请稍候。。。');
                        $.ajax({
                            type: "POST",
                            url: "${contextPath}/meterDetail/submitAll.action?metertype=1",
                            data: {},
                            processData:true,
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
                    },1000,true)();
                }
            })
    }

    /**
     全部电费单提交处理
     */
    function openSubmitAllElectricityHandler() {
            bs4pop.confirm('所有已创建的都提交，提交后该信息不可更改，并且可进行缴费，确认提交？', {}, function (sure) {
                if(sure){
                    bui.util.debounce(function () {


                        bui.loading.show('努力提交中，请稍候。。。');
                        $.ajax({
                            type: "POST",
                            url: "${contextPath}/meterDetail/submitAll.action?metertype=2",
                            data: {},
                            processData:true,
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
                    },1000,true)();
                }
            })

    }


    /**
     撤回
     */
    function openWithdrawHandler() {
        if(isSelectRow()){
            bs4pop.confirm('撤回之后该业务单可继续修改，但不能交费，如需继续交费可以再次提交。确定撤回？', {}, function (sure) {
                if(sure){
                    bui.loading.show('努力提交中，请稍候。。。');
                    //获取选中行的数据
                    let rows = _grid.bootstrapTable('getSelections');

                    if (null == rows || rows.length == 0 || rows.length != 1) {
                        bs4pop.alert('请选中一条数据');
                        return false;
                    }

                    let selectedRow = rows[0];

                    $.ajax({
                        type: "POST",
                        url: "${contextPath}/meterDetail/withdraw.action",
                        data: {id: selectedRow.id},
                        processData:true,
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
    }

    /**
     取消
     */
    function openCancelHandler() {
        if(isSelectRow()){
            bs4pop.confirm('确定取消该业务单？', {}, function (sure) {
                if(sure){
                    bui.loading.show('努力提交中，请稍候。。。');
                    //获取选中行的数据
                    let rows = _grid.bootstrapTable('getSelections');

                    if (null == rows || rows.length == 0 || rows.length != 1) {
                        bs4pop.alert('请选中一条数据');
                        return false;
                    }

                    let selectedRow = rows[0];

                    $.ajax({
                        type: "POST",
                        url: "${contextPath}/meterDetail/cancel.action",
                        data: {id: selectedRow.id},
                        processData:true,
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
    }


    //选中行事件 -- 可操作按钮控制
    _grid.on('check.bs.table', function (e, row, $element) {
        let state = row.$_state;
        if (state == ${@com.dili.ia.glossary.MeterDetailStateEnum.CREATED.getCode()}) {
            $('#toolbar button').attr('disabled', true);
            $('#btn_view').attr('disabled', false);
            $('#btn_add').attr('disabled', false);
            $('#btn_update').attr('disabled', false);
            $('#btn_submit').attr('disabled', false);
            $('#btn_cancel').attr('disabled', false);
            $('#export').attr('disabled', false);
        } else if (state == ${@com.dili.ia.glossary.MeterDetailStateEnum.SUBMITED.getCode()}) {
            $('#toolbar button').attr('disabled', true);
            $('#btn_view').attr('disabled', false);
            $('#btn_add').attr('disabled', false);
            $('#btn_withdraw').attr('disabled', false);
            $('#export').attr('disabled', false);
        } else if (state == ${@com.dili.ia.glossary.MeterDetailStateEnum.CANCELLED.getCode()}) {
            $('#toolbar button').attr('disabled', true);
            $('#btn_view').attr('disabled', false);
            $('#btn_add').attr('disabled', false);
            $('#export').attr('disabled', false);
        } else if (state == ${@com.dili.ia.glossary.MeterDetailStateEnum.PAID.getCode()}) {
            $('#toolbar button').attr('disabled', true);
            $('#btn_view').attr('disabled', false);
            $('#btn_add').attr('disabled', false);
            $('#export').attr('disabled', false);
        }
    });
    /*****************************************函数区 end**************************************/

    /*****************************************自定义事件区 begin************************************/




    /*****************************************自定义事件区 end**************************************/
</script>