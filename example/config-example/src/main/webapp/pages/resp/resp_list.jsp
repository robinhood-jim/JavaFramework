<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%
    String path = request.getContextPath();
    String CONTEXT_PATH = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path + "/";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>数据库浏览</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta http-equiv="x-ua-compatible" content="ie=7"/>
    <link rel="stylesheet" type="text/css" href="<%=CONTEXT_PATH%>component/dhtmlxSuite/skins/skyblue/dhtmlx.css"/>
    <link rel="stylesheet" type="text/css" href="<%=CONTEXT_PATH%>component/dhtmlxSuite/codebase/dhtmlx.css"/>
    <script src="<%=CONTEXT_PATH%>component/dhtmlxSuite/codebase/dhtmlx.js"></script>
    <script type="text/javascript" src="<%=CONTEXT_PATH%>resources/js/jquery.js"></script>
    <script language="javascript" src="<%= CONTEXT_PATH %>resources/js/Array.js"></script>
    <script type="text/javascript" src="<%=CONTEXT_PATH%>resources/js/jsLib.js"></script>

    <style type="text/css">
        html, body {
            width: 100%;
            height: 100%;
            border: none;
            overflow: hidden;
        }

        /* --------------以下为新增css---------------- */


    </style>
    <script type="text/javascript">
        var contextpath = "<%=CONTEXT_PATH%>";
        var imgPath = "<%=CONTEXT_PATH%>dhtmlxSuite/dhtmlx/imgs/icon/";

        function changetitle(title) {
            var t = "&lt;font style='font-size:12px; font-weight:bold'&gt;" + title + "&lt;/font&gt;";
            dhxToolbar.removeItem("title");
            dhxToolbar.addText("title", 0, t);
        }

    </script>
</head>
<body onload="">

<script type="text/javascript">
    var ctx = "<%=CONTEXT_PATH%>";
    var dhxLayout = new dhtmlXLayoutObject(document.body, "2U");
    var leftPanel = dhxLayout.cells("a");
    leftPanel.hideHeader();
    var rightPanel = dhxLayout.cells("b");
    leftPanel.setWidth(136);
    var cheight = document.body.clientHeight;
    var cwidth = document.body.clientWidth;

    var dhxView = leftPanel.attachDataView({type:{
            template:"#name#</br> 共#rcount#用户",
            height:40
        }});
    var projid = '${param.projId}';
    //dhxView.setImagePath("<%=CONTEXT_PATH%>component/dhtmlxSuite/codebase/imgs/dhxtree_material/");

    dhxView.load(ctx + "system/responsiblity/listAll", "json");

    dhxView.attachEvent("onItemClick", function (id,ev,html) {
        $("#container").attr("src",ctx+"system/responsiblity/showuser?respId="+id);
    });
</script>


<div id="mainFrame">
    <iframe id="container" style="overflow-y:auto;overflow-x:hidden!important;width:100%;height:100%" frameborder="no"
            src=""></iframe>
</div>


<script type="text/javascript">
    rightPanel.hideHeader();
    rightPanel.attachObject("mainFrame");
    $("#container").height(cheight-20)


</script>
</body>
</html>