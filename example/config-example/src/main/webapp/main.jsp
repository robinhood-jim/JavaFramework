<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<%
	String path = request.getContextPath();
	String CONTEXT_PATH = request.getScheme() + "://"+ request.getServerName() + ":" + request.getServerPort()+ path + "/";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><spring:message code="mainPage" /></title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<meta http-equiv="X-UA-Compatible" content="IE=edge"/>
<link rel="stylesheet" type="text/css" href="<%=CONTEXT_PATH%>component/dhtmlxSuite/skins/skyblue/dhtmlx.css"/>
<link rel="stylesheet" type="text/css" href="<%=CONTEXT_PATH%>component/dhtmlxSuite/codebase/dhtmlx.css" />
<link rel="stylesheet" type="text/css" href="<%=CONTEXT_PATH%>component/dhtmlxSuite/codebase/fonts/font_roboto/roboto.css"/>
<link rel="stylesheet" type="text/css" href="<%=CONTEXT_PATH%>resources/css/main.css"/>
<script src="<%=CONTEXT_PATH%>component/dhtmlxSuite/codebase/dhtmlx.js"></script>

<script language="javascript" src="<%= CONTEXT_PATH %>resources/js/jquery.js"></script>
<script language="javascript" src="<%= CONTEXT_PATH %>resources/js/Array.js"></script>
<script language="javascript" src="<%= CONTEXT_PATH %>resources/js/control.js"></script>
<style type="text/css">
html, body {
	width: 100%;
	height: 100%;
	border:none;
	overflow: hidden;
}
#sysName {
	background:url(<%=CONTEXT_PATH%>resources/images/headerBg.jpg) no-repeat -1px top;
	height:64px;
}
#topBanner {
	background:url(<%=CONTEXT_PATH%>resources/images/headerBg.jpg) repeat-x center top;
	height:64px;
	position:relative;
}
#header {
	width:100%;
	zoom:1;
}
#hInput {
	position:absolute;
	right:0;
	top:0;
	background: url(<%=CONTEXT_PATH%>resources/images/headerRBg.jpg) no-repeat right top;
	height:64px;
	width:366px;
	text-align:right;
	line-height:64px;
}
#logSate {
	position:absolute;
	left:1px;
	bottom:0px;
	padding:2px 0 0 42px;
	color:#ecfaff;
	background:url(<%=CONTEXT_PATH%>resources/images/loginIFBg.jpg) no-repeat left top;
	width:100%;
}


#manuloper {
	background: url("<%=CONTEXT_PATH%>resources/images/icon48.png") 2px 48px ;
	height: 48px;
	width: 48px;
}
.ToolBar {
    background: none repeat scroll 0 0 #FFFFFF;
    height: 300px;
    overflow: hidden;
}
.ToolBar ul {
    float: left;
    height: 48px;
    width: 130px;
}
.ToolBar li {
    float: left;
    height: 48px;
    text-align: center;
    width: 60px;
}
ul, ol {
    margin: 0 0 10px 25px;
    padding: 0;
}
ol, ul {
    list-style: none outside none;
}
</style>
<script type="text/javascript">
	 var contextpath = "<%=CONTEXT_PATH%>";	
	 var imgPath="<%=CONTEXT_PATH%>component/dhtmlxSuite/dhtmlx/imgs/icon/";
	 var mxBasePath="<%=CONTEXT_PATH%>resources";
	 var tabbar;
	 var editor;
   function changetitle(title){
   	  var t="&lt;font style='font-size:12px; font-weight:bold'&gt;"+title+"&lt;/font&gt;";
      dhxToolbar.removeItem("title");
   	  dhxToolbar.addText("title", 0,t);
   }
   function showmain(){
   		window.location.href='<%=CONTEXT_PATH%>main.jsp';
   }
   function logout(){
   	if(confirm('<spring:message code="confirmLogOut" />')==true)
   		window.location.href='<%=CONTEXT_PATH%>user/logout';
   }

</script>

</head>
<body onload="">
<div id="topBanner">
    <h1 id="sysName"><spring:message code="appName" /></h1>
    <div id="header">
        <div id="logSate"><spring:message code="currentUser" />：<span>${cookie['userName']}</span></div>
        <div id="hInput">
            <button name="topPage" id="topHome"  type="button" onClick="" class="headerBtn"><spring:message code="btn.main" /></button>
            <button id="topHelp" type="button" onClick=""  class="headerBtn"><spring:message code="btn.help" /></button>
            <button id="topExit" type="button" onClick="logout()"  class="headerBtn"><spring:message code="btn.logOut" /></button>
        </div>
    </div>
</div>
<script type="text/javascript">
	 var dhxLayout = new dhtmlXLayoutObject(document.body, "3T");
	 var topPanel=dhxLayout.cells("a");
	 var leftPanel=dhxLayout.cells("b");
	 var centerPanel=dhxLayout.cells("c");
	 topPanel.setHeight(66);
	 topPanel.setText("");
	 topPanel.hideHeader();
	 topPanel.attachObject("topBanner");
	 topPanel.fixSize(true,true);
	 
	
	 leftPanel.setWidth(136);
	 leftPanel.setText("");
	 var dhxAccord = leftPanel.attachAccordion();
    
</script>


<div id="canvas"  ></div>

<script type="text/javascript">
	dhxAccord.addItem("a1", "<spring:message code="mainMenu" />");
	var myTree=dhxAccord.cells("a1").attachTree();
	myTree.setImagePath(contextpath+"component/dhtmlxSuite/codebase/imgs/dhxtree_material/");
	myTree.setXMLAutoLoading(contextpath+"menu/list");
	myTree.setDataMode("json");
	myTree.load(contextpath+"menu/list?id=0","json");
	myTree.setOnClickHandler(processClick);
	function processClick(id){
		var url=myTree.getUserData(id,"url");
		if(url!=undefined && url!=''){
			 addTab(url, myTree.getItemText(id),id, "100px");
		}
	}

</script>
<div id="mainFrame" >
    <iframe id="container" style="overflow-y:auto;overflow-x:hidden!important;width:100%;height:100%" frameborder="no" src="" ></iframe>
</div>
<div id="mainTabbar" style="overflow-y:auto;overflow-x:hidden!important;width:100%;height:100%">
</div>


<script type="text/javascript">
	 centerPanel.hideHeader();
	 centerPanel.attachObject("mainFrame");
   tabbar = centerPanel.attachTabbar();
   tabbar.addTab("main", "memo", "100px");
   tabbar.cells("main").setActive();
   tabbar.enableTabCloseButton(true);
  
   
</script>
<script type="text/javascript" src="<%=CONTEXT_PATH%>resources/js/main.js"> </script>


</body>
</html>