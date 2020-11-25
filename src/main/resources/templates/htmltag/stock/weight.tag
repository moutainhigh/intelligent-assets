
<script id="weighmanUpdate" type="text/html">
<form id="weighmanForm" role="form" novalidate>
<input type="hidden" value = "{{weightDetail.id}}" name="id" />
<input type="hidden" value = "{{weightDetail.version}}" name="version" />
<div class="row">
			<div class="form-group row col-8">
				<div class="form-group col-12">
					<label for="_certificateNumber">称台重量(公斤)：<i class="red">*</i></label>
					<input id="weight" type="number" class="form-control "
					 name="weight" range="0 9999999" required readonly/>
					<button type="button" class="btn btn-secondary px-5 readWeight" >写入重量</button>
				</div>
				<div class="form-group col-2">
				 	<input type="radio" name="weighman" class="" value="1">
				</div>
				<div class="form-group col-5">
					<label for="_certificateNumber">毛重(公斤)：<i class="red">*</i></label>
					<input id="grossWeight" type="number" class="form-control " value="{{weightDetail.grossWeight}}"
					 name="grossWeight" range="0 9999999" required readonly/>
				</div>	
				<div class="form-group col-5">
					<label for="_certificateNumber">毛重时间：<i class="red">*</i></label>
					<input id="grossWeightDate"  class="form-control "
					 name="grossWeightDate" value="{{weightDetail.grossWeightDate}}" required readonly/>
				</div>
				<div class="form-group col-2">
				 	<input type="radio" name="weighman" class="" value="2">
				</div>
				<div class="form-group col-5">
					<label for="tareWeight" class="">皮重(公斤)：<i class="red">*</i></label> <input id="tareWeight" type="number" class="form-control "
					 name="tareWeight" value="{{weightDetail.tareWeight}}" range="0 9999999" required readonly />
				</div>
				<div class="form-group col-5">
					<label for="tareWeightDate" class="">皮重时间：<i class="red">*</i></label> <input id="tareWeightDate"  class="form-control "
					 name="tareWeightDate" value="{{weightDetail.tareWeightDate}}" required readonly />
				</div>
			</div>
			<div class="form-group col-4" id = "weightPic">
				
				pic
					
			</div>
			</div>
</form>	
<script>


$('.readWeight').on('click', function() {
	/*if(typeof callbackObj == 'undefined'){
        return;
    }*/
	// 判断是否已获取重量
	/*var isWeight=$(this).parents(".sub-single").find("[self-suffix='is_weight']").val();
    if(isWeight=="1"){
    	bs4pop.alert("已读取过重量信息", {
			type: 'error'
		});
        return;
    }*/
    //数据模拟
	let weighman = $("input[name='weighman']:checked").val();
	if(weighman == 1){
		$("#grossWeight").val("1000");
		$("#grossWeightDate").val("2020-07-06 12:34:44");
	}else{
		$("#tareWeight").val("100");
		$("#tareWeightDate").val("2020-07-06 12:34:44");
	}
    
    //获取司磅读数
    window.weightCallback=function(data){
    	if(data.status==-1){
            return;
        }
    	if(data.weightType==1){
    		$(".grossWeight").val(data.grossWeight);
    		$(".grossWeightDate").val(data.grossWeightDate);
    		$(".weightPic1").val(data.image1);
    		$(".weightPic2").val(data.image2);
    		/**
    	     * 司磅照片  [{name:"beforegross",url:"/de/de/666.imag"},{name:"aftergross",url:"/de/de/666.imag"}
    	     * ,{name:"befortare",url:"/de/de/666.imag"},{name:"aftertare",url:"/de/de/666.imag"}]
    	     */
            
        }else{
        	$(".tareWeight").val(data.tareWeight);
    		$(".tareWeightDate").val(data.tareWeightDate);
    		$(".weightPic3").val(data.image3);
    		$(".weightPic4").val(data.image4);
        }
    }
})
</script>

</script>

<script>
//修改子单打开司磅界面 回填数据
function openWeightUpdateHandler(index) {
	let weightDetail = weightItems.get(index+"")
	if(weightDetail == null || weightDetail == ""){
		weightDetail = {};
	}
	let weightItem = $("#saveForm_"+index)
    dia = bs4pop.dialog({
        title: '获取地磅读数',//对话框title
        content: bui.util.HTMLDecode(template("weighmanUpdate", {weightDetail})), //对话框内容，可以是 string、element，$object
        width: '80%',//宽度
        height: '95%',//高度
        btns: [{label: '取消',className: 'btn-secondary',onClick(e){

            }
        }, {label: '确定',className: 'btn-primary',onClick(e){
        	//通过map保存司磅记录,key为index,value为表单信息,方便前端页面组装数据
        	let ob = $("#weighmanForm").serializeObject();
        	weightItems.set(index+"",ob);
        	if(strIsNotEmpty(ob.grossWeight) && strIsNotEmpty(ob.tareWeight)){
        		weightItem.find("[name=weight]").val(ob.grossWeight-ob.tareWeight);
        	}else{
        		weightItem.find("[name=weight]").val(0);
        	}
        	//weightItem.find("[name=weight]").val($("#grossWeight").val());
        	countNumber("weight");
            }
        }]
    });
}
</script>