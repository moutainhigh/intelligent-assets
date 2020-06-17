<script>
    /**
     *
     * @Date 2019-11-06 17:30:00
     * @author jiangchengyong
     *
     ***/

    /*********************变量定义区 begin*************/
    //行索引计数器
    let itemIndex = 0;
    let isInitCheckDeduction = ${isNotEmpty(leaseOrder) ? true : false};
    $.extend(customerNameAutoCompleteOption,{
        selectFn: function (suggestion) {
            $('#certificateNumber').val(suggestion.certificateNumber);
            $('#_certificateNumber').val(suggestion.certificateNumber);
            $('#customerCellphone').val(suggestion.contactsPhone);
            $("#_certificateNumber,#customerCellphone").valid();
        }
    });
    $.extend(certificateNumberAutoCompleteOption,{
        selectFn: function (suggestion) {
            $('#customerName').val(suggestion.name);
            $('#customerId').val(suggestion.id);
            $('#customerCellphone').val(suggestion.contactsPhone);
            $("#customerName,#customerCellphone").valid();
        }
    });

    //资产搜索自动完成
    var assetAutoCompleteOption = {
        paramName: 'keyword',
        displayFieldName: 'name',
        serviceUrl: '/booth/search.action',
        selectFn: assetSelectHandler,
        transformResult: function (result) {
            if(result.success){
                let data = result.data;
                return {
                    suggestions: $.map(data, function (dataItem) {
                        return $.extend(dataItem, {
                            value: dataItem.name + '(' + (dataItem.secondAreaName ? dataItem.areaName + '->' + dataItem.secondAreaName : dataItem.areaName) + ')'
                            }
                        );
                    })
                }
            }else{
                bs4pop.alert(result.message, {type: 'error'});
                return;
            }
        }
    }

    //品类搜索自动完成
    var categoryAutoCompleteOption = {
        paramName: 'keyword',
        displayFieldName: 'name',
        serviceUrl: '/category/search.action',
        transformResult: function (result) {
            if(result.success){
                let data = result.data;
                return {
                    suggestions: $.map(data, function (dataItem) {
                        return $.extend(dataItem, {
                                value: dataItem.name + '(' + dataItem.code + ')'
                            }
                        );
                    })
                }
            }else{
                bs4pop.alert(result.message, {type: 'error'});
                return;
            }
        }
    }
    /*********************变量定义区 end***************/


    /******************************驱动执行区 begin***************************/
    $(function () {
        //初始化刷卡
        initSwipeCard({
            id:'getCustomer'
        });

        //监听客户注册
        registerMsg();

        laydate.render({
                elem: '#startTime',
                type: 'date',
                theme: '#007bff',
                trigger:'click',
            <% if(isNotEmpty(isRenew) && isRenew == 1){ %>
                value: moment("${leaseOrder.endTime!,dateFormat='yyyy-MM-dd'}").add(1,"days").format("YYYY-MM-DD"),
            <% } else if(isEmpty(leaseOrder)){ %>
                value: new Date(),
            <% }%>
            done: function(value, date){
                $('#startTime').val(value);
                startTimeChangeHandler();
                $("#saveForm").validate().element($("#startTime"));
                $("#saveForm").validate().element($("#days"));
            }
        });
        laydate.render({
            elem: '#endTime',
            type: 'date',
            theme: '#007bff',
            trigger:'click',
            done: function(value, date){
                $('#endTime').val(value);
                endTimeChangeHandler();
                $("#saveForm").validate().element($("#endTime"));
                $("#saveForm").validate().element($("#days"));
            }
        });

        <% if(isNotEmpty(isRenew) && isRenew == 1){ %>
            //重新计算续租日期
            $('#endTime').val(moment($('#startTime').val()).add($('#days').val()-1,"days").format("YYYY-MM-DD"));
        <% } %>

        <% if(isNotEmpty(leaseOrderItems)){ %>
            itemIndex += ${leaseOrderItems.~size};
        <% }else{%>
            while (itemIndex<1) {
                addBoothItem({index: ++itemIndex});
            }
        <% }%>
    });
    /******************************驱动执行区 end****************************/

    /*****************************************函数区 begin************************************/

    //获取table Index
    function getIndex(str) {
        return str.split('_')[1];
    }

    /**
     * 添加资产
     * @param leaseOrderItem
     */
    function addBoothItem(leaseOrderItem){
        $('#assetTable tbody').append(bui.util.HTMLDecode(template('assetItem',leaseOrderItem)))
    }

    /**
     * 天数改变处理Handler
     * 三者相互联动；三者都有值情况，修改天数，变结束；修改开始或者结束，都变天数
     * */
    function daysChangeHandler(){
        let days = $('#days').val();
        let startTime = $('#startTime').val();
        let endTime = $('#endTime').val();
        if(days){
            //天数变更优先计算结束日期
            if(startTime){
                $('#endTime').val(moment(startTime).add(days-1,"days").format("YYYY-MM-DD"));
                $("#saveForm").validate().element($("#endTime"));
                return;
            }

            if(endTime){
                $('#startTime').val(moment(endTime).subtract(days-1,"days").format("YYYY-MM-DD"));
                $("#saveForm").validate().element($("#startTime"));
                return;
            }
        }
    }

    /**
     * 开始日期值改变处理Handler
     * 三者相互联动；三者都有值情况，修改天数，变结束；修改开始或者结束，都变天数
     * */
    function startTimeChangeHandler(){
        let days = $('#days').val();
        let startTime = $('#startTime').val();
        let endTime = $('#endTime').val();
        if(startTime && !moment(startTime,"YYYY-MM-DD",true).isValid()){
            $('#days').val('');
            return;
        }
        if(startTime){
            //开始结束日期变更优先计算天数
            if(endTime){
                $('#days').val(moment(endTime).diff(moment(startTime),'days') + 1);
                $("#saveForm").validate().element($("#days"));
                return;
            }

            if(days){
                $('#endTime').val(moment(startTime).add(days-1,"days").format("YYYY-MM-DD"));
                return;
            }
        }
    }

    /**
     * 结束日期值改变处理Handler
     * 三者相互联动；三者都有值情况，修改天数，变结束；修改开始或者结束，都变天数
     * */
    function endTimeChangeHandler(){
        let days = $('#days').val();
        let startTime = $('#startTime').val();
        let endTime = $('#endTime').val();
        if(endTime && !moment(endTime,"YYYY-MM-DD",true).isValid()){
            $('#days').val('');
            return;
        }
        if(endTime){
            //开始结束日期变更优先计算天数
            if(startTime){
                $('#days').val(moment(endTime).diff(moment(startTime),'days') + 1);
                return;
            }
            if(days){
                $('#startTime').val(moment(endTime).subtract(days-1,"days").format("YYYY-MM-DD"));
                return;
            }
        }
    }

    /**
     * 资产选择事件Handler
     * */
    function assetSelectHandler(suggestion,element) {
        let index = getIndex($(element).attr('id'));
        $('#number_'+index).val(suggestion.number);
        $('#unitCode_'+index).val(suggestion.unit);
        $('#unitName_'+index).val(suggestion.unitName);
        $('#sku_'+index).val(suggestion.number+suggestion.unitName);
        $('#isCorner_'+index).val(suggestion.cornerName);
        $('#districtId_'+index).val(suggestion.secondArea?suggestion.secondArea : suggestion.area);
        $('#districtName_' + index).val(suggestion.secondAreaName ? suggestion.areaName + '->' + suggestion.secondAreaName : suggestion.areaName);
    }


    /**
     * 构建资产租赁表单提交数据
     * @returns {{}|jQuery}
     */
    function buildFormData(){
        let formData = $("input:not(table input),textarea,select").serializeObject();
        let leaseOrderItems = [];
        let leaseTermName = $('#leaseTermCode').find("option:selected").text();
        let engageName = $('#engageCode').find("option:selected").text();
        let departmentName = $('#departmentId').find("option:selected").text();

        bui.util.yuanToCentForMoneyEl(formData);
        $("#assetTable tbody").find("tr").each(function(){
            let leaseOrderItem = {};
            leaseOrderItem.businessChargeItems = [];
            $(this).find("input").each(function(t,el){
                let nameArr = $(this).attr("name").split('_');
                let fieldName = nameArr[0];
                if(fieldName.includes("chargeItem")){
                    let businessChargeItem = {};
                    businessChargeItem.chargeItemName = this.dataset.chargeItemName;
                    businessChargeItem.chargeItemId = this.dataset.chargeItemId;
                    businessChargeItem.bizType = ${@com.dili.ia.glossary.AssetsTypeEnum.getAssetsTypeEnum(assetType).getBizType()};
                    businessChargeItem.amount = $(this).hasClass('money')? Number($(this).val()).mul(100) : $(this).val();
                    leaseOrderItem.businessChargeItems.push(businessChargeItem);
                }else{
                    leaseOrderItem[fieldName] = $(this).hasClass('money')? Number($(this).val()).mul(100) : $(this).val();
                }
            });
            leaseOrderItem.assetType = $('#assetType').val();
            leaseOrderItems.push(leaseOrderItem);
        });

        $.extend(formData, {
            leaseOrderItems,
            leaseTermName,
            engageName,
            departmentName,
            logContent: $('#id').val() ? Log.buildUpdateContent() : ''
        });
        return formData;
    }

    /**
     * 判断数组中的元素是否重复出现
     * 验证重复元素，有重复返回true；否则返回false
     * @param arr
     * @returns {boolean}
     */
    function arrRepeatCheck(arr) {
        var hash = {};
        for(var i in arr) {
            if(hash[arr[i]]) {
                return true;
            }
            // 不存在该元素，则赋值为true，可以赋任意值，相应的修改if判断条件即可
            hash[arr[i]] = true;
        }
        return false;
    }

    /**
     * 表单baocun
     * @returns {boolean}
     */
    function saveFormHandler(){
        let validator = $('#saveForm').validate({ignore:''})
        if (!validator.form()) {
            $('.breadcrumb [data-toggle="collapse"]').html('收起 <i class="fa fa-angle-double-up" aria-hidden="true"></i>');
            $('.collapse:not(.show)').addClass('show');
            return false;
        }

        let assetIds = $("table input[name^='assetId']").filter(function () {
            return this.value
        }).map(function(){
            return $('#assetId_'+getIndex(this.id)).val();
        }).get();

        if(!assetIds || assetIds.length == 0){
            bs4pop.alert('请添加资产！')
            return false;
        }

        if(arrRepeatCheck(assetIds)){
            bs4pop.alert('存在重复资产，请检查！')
            return false;
        }

        if(assetIds.length > 10){
            bs4pop.notice('最多10个资产', {position: 'leftcenter', type: 'warning'});
        }

        bui.loading.show('努力提交中，请稍候。。。');
        $.ajax({
            type: "POST",
            url: "/assetLeaseOrder/saveLeaseOrder.action",
            data: JSON.stringify(buildFormData()),
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            success: function (ret) {
                if(!ret.success){
                    bs4pop.alert(ret.message, {type: 'error'});
                }else{
                    parent.closeDialog(parent.dia);
                }
                bui.loading.hide();
            },
            error: function (a, b, c) {
                bui.loading.hide();
                bs4pop.alert('远程访问失败', {type: 'error'});
            }
        });
    }

    /*****************************************函数区 end**************************************/

    /*****************************************自定义事件区 begin************************************/
    //资产新增事件
    $('#addBooth').on('click', function(){
        if ($('#assetTable tr').length < 11) {
            addBoothItem({index: ++itemIndex});
        } else {
            bs4pop.notice('最多10个资产', {position: 'leftcenter', type: 'warning'})
        }
    });

    //资产删除事件
    $(document).on('click', '.item-del', function () {
        if ($('#assetTable tr').length > 1) {
            $(this).closest('tr').remove();
        }
    });

    /*****************************************自定义事件区 end**************************************/
</script>