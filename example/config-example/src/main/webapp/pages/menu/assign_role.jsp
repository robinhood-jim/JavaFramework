<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
 <%
	String path = request.getContextPath();
	String CONTEXT_PATH = request.getScheme() + "://"+ request.getServerName() + ":" + request.getServerPort()+ path + "/";
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8;nocache">
<meta HTTP-EQUIV="Cache-Control" CONTENT="no-cache, must-revalidate"> 
<meta HTTP-EQUIV="expires" CONTENT="0"> 
<title>角色赋权</title>
<link rel="stylesheet" type="text/css" href="<%=CONTEXT_PATH%>component/dhtmlxSuite/skins/skyblue/dhtmlx.css"/>
<link rel="stylesheet" type="text/css" href="<%=CONTEXT_PATH%>component/dhtmlxSuite/codebase/dhtmlx.css" />
<link rel="stylesheet" type="text/css" href="<%=CONTEXT_PATH%>component/dhtmlxSuite/codebase/fonts/font_roboto/roboto.css"/>
<link rel="stylesheet" type="text/css" href="<%=CONTEXT_PATH%>resources/css/main.css"/>
<script src="<%=CONTEXT_PATH%>component/dhtmlxSuite/codebase/dhtmlx.js"></script>
<script language="javascript" src="<%= CONTEXT_PATH %>resources/js/jquery.js"></script>
<script type="text/javascript" src="<%=CONTEXT_PATH%>resources/js/jqueryui.js"></script>
<script language="javascript" src="<%= CONTEXT_PATH %>resources/js/Array.js"></script>
<script language="javascript" src="<%= CONTEXT_PATH %>resources/js/control.js"></script>
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
<script language="javascript" src="<%= CONTEXT_PATH %>resources/js/crud.js"></script>
<script type="text/javascript">
	dhtmlx.message.defPosition="bottom";
	 var contextpath = "<%=CONTEXT_PATH%>";
	 var queryUrl=contextpath+"system/menu/list";
	 var cheight=window.screen.screenHeight;
	 var cwidth=window.screen.screenWidth;
	 var dhxLayout = new dhtmlXLayoutObject(document.body, "1C");
	
	 var topPanel=dhxLayout.cells("a");

	 topPanel.setText("角色赋权");
	 topPanel.hideHeader();
	 var formStructure =[
								{type:"settings", position:"label-left",lableWidth: 80,inputWidth: 120},
								{type: "fieldset",label:"赋权", inputWidth: "auto",width: 530,blockOffset :10,offsetLeft:20,offsetTop:5,list:[
								{type: "hidden", name:"id", value:""},
								{type: "hidden", name:"resId", value:""},
								{type: "hidden", name:"selRoleIds", value:""},
								{type:"multiselect",inputHeight:250, name:"aviableroleIds", label:"待选角色:"},
								{type:"newcolumn",offset:20},
								{type: "block", inputWidth: "auto", blockOffset: 0,list:[
								                         								{type:"button", name:"cmdAdd",width:30, value:">>"},
								                         	 							{type:"button", name:"cmdRemove",width:30, value:"<<"}
								    ]
								  },
								{type:"newcolumn",offset:20},
								{type:"multiselect", name:"roleIds", label:"赋权角色:",inputHeight:250},
								]},
								 {type: "block", inputWidth: "auto", list: [
									                       					{type: "settings", offsetTop: 10},
									                       					{type:"button", name:"cmdOK",width:30, value:"确定",offsetLeft: 150},
									                       					{type: "newcolumn"},
									                       					 {type:"button", name:"cmdCancel",width:30, value:"取消"}
									                       				]}
		                   ];
	var dhxForm=topPanel.attachForm();
	 dhxForm.loadStruct(formStructure,"json");

	 topPanel.fixSize(true,false);
   
     var resId=<c:out value="${resId}" />
     dhxForm.setItemValue("resId",resId);
     var select1=dhxForm.getOptions("roleIds");
     <c:forEach items="${roleList}" var="role">
	 	addSelect(select1,'${role.name}','${role.id}')
	 </c:forEach>
	 var select2=dhxForm.getOptions("aviableroleIds");
	 <c:forEach items="${avaliableList}" var="role">
	 	addSelect(select2,'${role.name}','${role.id}')
	 </c:forEach>
     function addSelect(selector,name,id){
    	 	selector.add(new Option(name,id))
    }
     dhxForm.attachEvent("onButtonClick", function(name, command){
    	 	if(name=="cmdAdd"){
    	 		changeContactState("aviableroleIds","roleIds")
    	 	}else if(name=="cmdRemove"){
    	 		changeContactState("roleIds","aviableroleIds")
    	 	}
     		else if(name=="cmdOK"){
     			  var ids=dhxForm.getOptions("roleIds");
     			  var idArr="";
     			  for(i=0;i<ids.length;i++){
     				  var selid=ids[i].value;
     				  idArr+=selid+",";
     			  }
     			  var selids=idArr.substr(0,idArr.length-1);
     			 	alert(selids);
     			  dhxForm.setItemValue("selRoleIds",selids);
     			  if(selids !=undefined && selids!=""){
		          this.send(contextpath+"system/menu/assignrole",function(loader, response){
		        	  var tobj= eval('(' + response + ')'); 
		        	  if(tobj.success=='true'){
		        		  	dhtmlx.message({
								text: "赋权成功",
								expire: 10
							});
		        		  parent.closedialog();
		        	  }else{
		        		  openMsgDialog("菜单赋权失败","错误信息:"+obj.message,300,200);
		        	  }
		          }); 
     			  }else{
     				 openMsgDialog("菜单赋权","请选择授权角色",300,200);
     			  }
		    }else if(name=="cmdCancel"){
		    	parent.closedialog();
		    }
		});
     function changeContactState(ida,idb) {	
			var sa = dhxForm.getSelect(ida);
			var sb = dhxForm.getSelect(idb);		
			var t = dhxForm.getItemValue(ida);
			if (t.length == 0) return;
			eval("var k={"+t.join(":true,")+":true};");
			var w = 0;
			var ind = -1;
			while (w < sa.options.length) {
				if (k[sa.options[w].value]) {
					sb.options.add(new Option(sa.options[w].text,sa.options[w].value));
					sa.options.remove(w);
					ind = w;
				} else {
					w++;
				}
			}
			
			if (sa.options.length > 0 && ind >= 0) {
				if (sa.options.length > 0) sa.options[t.length>1?0:Math.min(ind,sa.options.length-1)].selected = true;
			}
			
		}

     function addInit(form){
		 form.attachEvent("onButtonClick", function(name, command){
			 form.validate();
			  if(name=="cmdOK"){
				  	  var pid=this.getItemValue("parentId");
			          this.send(contextpath+"system/menu/save",function(loader, response){
			        	  var tobj= eval('(' + response + ')'); 
			        	  if(tobj.success=='true'){
			        		  dhtmlx.message({
									text: "保存成功",
									expire: 10
								});
			        		  var menu=tobj.menu;
			        		  dhxTree.insertNewChild(pid,menu.id,menu.resName,0,0,0,0,"CHILD");
			        	  	 closedialog(true);
			        	  }else{
			        		  openMsgDialog("保存用户失败","错误信息:"+tobj.message,300,200);
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
   
	 
</script>


</body>
</html>