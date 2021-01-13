<script>

    //初始化刷卡
    initSwipeIdCard({
        id:'getCustomer',
    });

    //对应摊位
    $(function () {
        registerMsg();
    });

    //品类搜索自动完成
    var categoryAutoCompleteOption = {
        serviceUrl: '/stock/categoryCycle/searchV2.action',
        paramName : 'keyword',
        displayFieldName : 'name',
        showNoSuggestionNotice: true,
        noSuggestionNotice: '无匹配结果',
        transformResult: function (result) {
            if(result.success){
                let data = result.data;
                return {
                    suggestions: $.map(data, function (dataItem) {
                        return $.extend(dataItem, {
                                value: dataItem.name + '（' + dataItem.keycode + '）'
                            }
                        );
                    })
                }
            }else{
                bs4pop.alert(result.message, {type: 'error'});
                return false;
            }
        },
        selectFn: function (suggestion) {
            $("#cycle").val(suggestion.cycle);
        }
    }
    function buildFormData(){
        // let formData = new FormData($('#saveForm')[0]);
        let formData = $("input:not(table input),textarea,select").serializeObject();
        let typeName = $('#typeCode').find("option:selected").text();
        // 部门名称
        let departmentName = $('#departmentId').find("option:selected").text();
        formData.departmentName = departmentName;
        // 收费项名称
        let chargeItemName = $('#chargeItemId').find("option:selected").text();
        formData.chargeItemName = chargeItemName;

        bui.util.yuanToCentForMoneyEl(formData);
        $.extend(formData,{typeName});
        return JSON.stringify(formData);
    }

    // 提交保存
    function doAddOtherFeeHandler(){
        let validator = $('#saveForm').validate({ignore:''})
        if (!validator.form()) {
            /*$(this).find('.collapse').each(function (index, element) {
                $(element).trigger('show.bs.collapse');
            });*/
            $('.breadcrumb [data-toggle="collapse"]').html('收起 <i class="fa fa-angle-double-up" aria-hidden="true"></i>');
            $('.collapse:not(.show)').addClass('show');
            return false;
        }
        bui.loading.show('努力提交中，请稍候。。。');
        // let _formData = new FormData($('#saveForm')[0]);
        $.ajax({
            type: "POST",
            url: "${contextPath}/otherFee/doAdd.action",
            data: buildFormData(),
            dataType: "json",
            contentType: "application/json",
            success: function (ret) {
                bui.loading.hide();
                if(!ret.success){
                    bs4pop.alert(ret.message, {type: 'error'});
                }else{
                    parent.closeDialog(parent.dia);
                    parent.$('#grid').bootstrapTable('refresh');
                }
            },
            error: function (error) {
                bui.loading.hide();
                bs4pop.alert('远程访问失败', {type: 'error'});
            }
        });
    }

    $('#departmentId').on('change', function () {
        let departmentId = $(this).val();
        $('#chargeItemId').html('<option value="" selected="">-- 请选择 --</option>');
        $('#chargeItemName').val('')
        $.ajax({
            type: 'get',
            dataType: "json",
            data: {departmentId: departmentId},
            url: '/departmentChargeItem/getChargeItemsByDepartment.action',
            success: function(res){
                if (res.success) {
                    for (let i=0; i<res.data.length; i++ ) {
                        $('#chargeItemId').append('<option value="' + res.data[i].chargeItemId + '" data-type="' + res.data[i].chargeItemName + '">' + res.data[i].chargeItemName + '</option>')
                    }
                }
            },
            error: function (error) {
                bs4pop.alert('远程访问失败', {type: 'error'});
            }
        })
    })
    $('#chargeItemId').on('change',  function () {
        $('#chargeItemName').val($(this.data('chargeItemName')))
    })
    $('#save').on('click', bui.util.debounce(doAddOtherFeeHandler,1000,true));

</script>