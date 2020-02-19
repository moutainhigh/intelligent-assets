<script>

    //时间范围
    lay('.laydatetime').each(function() {
        laydate.render({
            elem : this
            ,trigger : 'click'
            ,range: true
        });
    });

    function openInsertHandler() {
        let dia = bs4pop.dialog({
            id: 'addModal',
            title: '新增定金',//对话框title
            content: '/earnestOrder/add.html', //对话框内容，可以是 string、element，$object
            width: 900,//宽度
            height: 700,//高度
            isIframe : true,//默认是页面层，非iframe
        });
        // dia.modal('show');
        $('#addModal').modal('handleUpdate')
    }



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
        queryDataHandler();
    });
    /******************************驱动执行区 end****************************/

    /**
     * 查询处理
     */
    function queryDataHandler() {
        // currentSelectRowIndex = undefined;
        // $('#toolbar button').attr('disabled', false);
        _grid.bootstrapTable('refreshOptions', {url: '/earnestOrder/listPage.action'});
    }

</script>

<!--
http://ia.diligrp.com:8381/earnestOrder/view.html 查看
http://ia.diligrp.com:8381/earnestOrder/add.html 新增
http://ia.diligrp.com:8381/earnestOrder/update.html 修改
-->
