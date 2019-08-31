<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
 <%
	String path = request.getContextPath();
	String CONTEXT_PATH = request.getScheme() + "://"+ request.getServerName() + ":" + request.getServerPort()+ path + "/";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<meta http-equiv="X-UA-Compatible" content="IE=edge"/>
<link rel="stylesheet" type="text/css" href="<%=CONTEXT_PATH%>component/dhtmlxSuite/skins/skyblue/dhtmlx.css"/>
<link rel="stylesheet" type="text/css" href="<%=CONTEXT_PATH%>component/dhtmlxSuite/codebase/dhtmlx.css" />
<link rel="stylesheet" type="text/css" href="<%=CONTEXT_PATH%>component/dhtmlxSuite/codebase/fonts/font_roboto/roboto.css"/>
<link rel="stylesheet" type="text/css" href="<%=CONTEXT_PATH%>resources/css/main.css"/>
<script src="<%=CONTEXT_PATH%>component/dhtmlxSuite/codebase/dhtmlx.js"></script>
<script language="javascript" src="<%= CONTEXT_PATH %>resources/js/jquery.js"></script>
<script type="text/javascript" src="<%=CONTEXT_PATH%>resources/js/jqueryui.js"></script>
<script language="javascript" src="<%= CONTEXT_PATH %>resources/js/Array.js"></script>
<script language="javascript" src="<%= CONTEXT_PATH %>resources/js/control.js"></script>
<script src="<%=CONTEXT_PATH%>resources/js/validate.js"></script>
<style type="text/css">
		div#layoutObj {
			position: relative;
			margin-top: 10px;
			margin-left: 10px;
			margin-right:10px;
			margin-bottom:10px;
			width: 95%;
			height: 95%;
		}
		 html, body {
        width: 100%;
        height: 100%;
        margin: 10px;
        overflow: hidden;
    }
		
</style>
</head>
<body>
<script type="text/javascript">
	dhtmlx.message.defPosition="bottom";
	 var ctx = "<%=CONTEXT_PATH%>";
	 var queryUrl=ctx+"system/menu/list";
	 
	 var dhxLayout = new dhtmlXLayoutObject(document.body, "1C");
	 var dhxToobar = dhxLayout.cells("a").attachToolbar(); 
	 dhxLayout.cells("a").setText("");
	 dhxLayout.cells("a").hideHeader();
	 dhxToobar.setIconsPath(ctx+"component/dhtmlxSuite/comm/imgs/");
	 dhxToobar.addButton("new", 0, '<spring:message code="btn.add" />', "new.gif", "new_dis.gif");
	 dhxToobar.addButton("edit", 1, '<spring:message code="btn.modi" />',"open.gif", "open_dis.gif");
	 dhxToobar.addButton("delete", 2, '<spring:message code="btn.delete" />',"close.gif", "close_dis.gif");
	 dhxToobar.addButton("assign", 3, '<spring:message code="btn.add" />',"open.gif", "open_dis.gif");
	 var dhxTree=dhxLayout.cells("a").attachTree();
	 dhxTree.setImagePath(ctx+"component/dhtmlxSuite/codebase/imgs/dhxtree_material/");
	 dhxTree.enableTreeLines(true);
     dhxTree.setImageArrays("plus", "plus2.gif", "plus3.gif", "plus4.gif", "plus.gif", "plus5.gif");
     dhxTree.setImageArrays("minus", "minus2.gif", "minus3.gif", "minus4.gif", "minus.gif", "minus5.gif");
     dhxTree.setStdImages("leaf.gif", "folderOpen.gif", "folderClosed.gif");
     dhxTree.setDataMode("json");

     dhxTree.load(ctx+"system/menu/list","json");
     dhxToobar.attachEvent("onClick", function(id){
		 if(id=='new'){
			 goAdd();
		 }else if(id=='edit'){
			 goEdit();
		 }else if(id=='delete'){
			 goDelete();
		 }else if(id=='assign'){
			 goAssign();
		 }
	 });
     

     var editFormContent=[
							{type:"settings", position:"label-left",lableWidth: 100,inputWidth: 120},
							{type: "fieldset", label: "菜单信息",offsetLeft:10, inputWidth: 520, lableWidth: 100,list:[
							{type: "hidden", name:"id", value:""},
							{type: "hidden", name:"parentId", value:""},
							{type:"input", name:"resName", label:"菜单名:",validate:"NotEmpty"},
							{type:"input", name:"resCode", label:"编码:",validate:"NotEmpty"},
							{type:"newcolumn",offset:20},
							{type:"input", name:"actionUrl", label:"URL:",validate:"NotEmpty"},
							{type:"input", name:"orderNo", label:"序号:",validate:"NotEmpty"}
							]},
							{type: "block", inputWidth: 510, list: [
							                       					{type: "settings", offsetTop: 10},
							                       					{type:"button", name:"cmdOK",width:30, value:"确定",offsetLeft: 150},
							                       					{type: "newcolumn"},
							                       					 {type:"button", name:"cmdCancel",width:30, value:"取消"}
							                       				]}
	                   ];
     function addInit(form){	 
		 form.attachEvent("onButtonClick", function(name, command){
			 form.validate();
			  if(name=="cmdOK"){
				  	  var pid=this.getItemValue("parentId");
			          this.send(ctx+"system/menu/save",function(loader, response){
			        	  var tobj= eval('(' + response + ')'); 
			        	  if(tobj.success=='true'){
			        		  dhtmlx.message({
									text: <spring:message code="message.saveSuccess" />,
									expire: 10
								});
			        		  var menu=tobj.menu;
			        		  dhxTree.insertNewChild(pid,menu.id,menu.resName,0,0,0,0,"CHILD");
			        	  	 closedialog(true);
			        	  }else{
			        		  openMsgDialog("<spring:message code="message.SaveFailed" />","<spring:message code="message.errorMsg" />"+tobj.message,300,200);
			        	  }
			          });      
			    }else if(name=='cmdCancel'){
			    	closedialog(false);
			    }
			});
	}
     function goAdd(){
    	 var id=dhxTree.getSelectedItemId();
    	 if(id==''){
    		 openMsgDialog("添加菜单","请选择上级菜单",300,200);
    	 }else{
			var form=openWindowForAdd("添加菜单",editFormContent,550,250,addInit);
			form.setItemValue("parentId",id);
    	 }
	}
     function editInit(form){
 		 form.attachEvent("onButtonClick", function(name, command){
 			  if(name=="cmdOK"){
 			          this.send(ctx+"system/menu/update",function(loader, response){
 			        	  var tobj= eval('(' + response + ')'); 
 			        	  if(tobj.success=='true'){
 			        		  dhtmlx.message({
 									text: "修改成功",
 									expire: 10
 								});
 			        	  	 closedialog(true);
 			        	 	 reload();
 			        	  }else{
 			        		  openMsgDialog("修改用户失败","错误信息:"+tobj.message,300,200);
 			        	  }
 			          });      
 			    }else if(name=='cmdCancel'){
 			    	closedialog(false);
 			    }
 			});
 	}
     function goEdit(){
    	 var id=dhxTree.getSelectedItemId();
 		if(id==''){
 			openMsgDialog("编辑菜单","请选择菜单",300,150);
 		}else if(id.indexOf(",")!=-1){
 			openMsgDialog("编辑菜单","只能修改一个菜单",300,150);
 		}else{
 			var form=openWindowForEdit("修改菜单",editFormContent,550,250,editInit);
 			//form.load(ctx+"system/sysuser/edit?id="+list);
 			$.ajax({
 				type:"get",
 				url:ctx+"system/menu/edit?id="+id,
 				dataType: "json",
 			    success:function(data){
 				var obj=eval(data);
 				form.setItemValue("id",obj.id);
 				form.setItemValue("resName",obj.resName);
 				form.setItemValue("resCode",obj.resCode);
 				form.setItemValue("actionUrl",obj.actionUrl);
 				form.setItemValue("orderNo",obj.orderNo);
 			}
 			});
 		}
 	}
     function goAssign(){
    	 var id=dhxTree.getSelectedItemId();
  		if(id==''){
  			openMsgDialog("菜单赋权","请选择菜单",300,150);
  		}else if(id.indexOf(",")!=-1){
  			openMsgDialog("菜单赋权","只能给一个菜单赋权",300,150);
  		}else{
  			var form=openWindow("菜单角色赋权",ctx+"system/menu/showrole?id="+id,600,450);
  		}
     }     
	 
</script>
<script language="javascript" src="<%= CONTEXT_PATH %>resources/js/crud.js"></script>
</body>
</html>