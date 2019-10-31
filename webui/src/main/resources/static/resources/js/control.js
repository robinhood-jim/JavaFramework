var winName="win1";
function showUser(id){
	var editFormContent=[
							{type:"settings", position:"label-left",lableWidth: 100,inputWidth: 120},
							{type: "fieldset", label: "用户信息",offsetLeft:10, inputWidth: 520, lableWidth: 100,list:[
							{type: "hidden", name:"id", value:""},
							{type:"input", name:"userName", label:"用户名:",validate:"NotEmpty"},
							{type:"select", name:"accountType", label:"账号类型:",options:[
									{text: "系统帐户", value: "1"},
									{text: "一般账户", value: "2"},
							]},
							{type:"select", name:"orgId", label:"账号类型:"},
							{type:"newcolumn",offset:20},
							{type:"input", name:"userAccount", label:"账号名:",validate:"NotEmpty"},
							{type:"select", name:"deptId", label:"组织机构:"}
							]},
							{type: "block", inputWidth: 510, list: [
							                       					{type: "settings", offsetTop: 10},
							                       					{type:"button", name:"cmdOK",width:30, value:"确定",offsetLeft: 150},
							                       					{type: "newcolumn"},
							                       					 {type:"button", name:"cmdCancel",width:30, value:"取消"}
							                       				]}
	                   ];
	 openWindowForm("用户",editFormContent,550,350,null);
	 
}
 function openWindowForm(title,formStructure,w,h,func){
   	 var w = dhxWins.createWindow(winName, 0, 0, w, h);
      w.setText(title);
      w.keepInViewport(true);
      w.setModal(true);
      w.centerOnScreen();
      w.button("minmax1").hide();
      w.button("minmax2").hide();
      w.button("park").hide();
      var form=w.attachForm();
      form.loadStruct(formStructure,"json");
      if(func!=undefined)
      	eval(func(form));
      return w;    
   }