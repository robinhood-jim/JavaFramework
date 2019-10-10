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
    var dhxLayout = new dhtmlXLayoutObject(document.body, "2U");
    var leftPanel = dhxLayout.cells("a");
    leftPanel.setText("Schema");
    var rightPanel = dhxLayout.cells("b");
    leftPanel.setWidth(136);
    leftPanel.setText("");

    var dhxTree = leftPanel.attachTree();
    var projid = '${param.projId}';
    dhxTree.setImagePath("<%=CONTEXT_PATH%>component/dhtmlxSuite/codebase/imgs/dhxtree_material/");
    dhxTree.setXMLAutoLoading(contextpath + "system/datamapping/listtable?projId=" + projid);
    dhxTree.load(contextpath + "system/datamapping/listschema?projId=" + projid + "&id=0", "xml");

    dhxTree.attachEvent("onClick", function (id) {
        var txt = dhxTree.getItemText(id);
        var parentid = dhxTree.getUserData(id, "parentid");
        if (parentid != undefined && !parentid == '') {
            var url = "system/datamapping/showfields?projId=" + projid + "&schema=" + parentid + "&table=" + id;
            if (url != '' && url != undefined && url != '#') {
                addTab(url, txt, id);
            }
        }
    });
</script>


<div id="mainFrame">
    <iframe id="container" style="overflow-y:auto;overflow-x:hidden!important;width:100%;height:100%" frameborder="no"
            src=""></iframe>
</div>


<script type="text/javascript">
    rightPanel.hideHeader();
    rightPanel.attachObject("mainFrame");
    tabbar = rightPanel.attachTabbar();

    tabbar.addTab("main", "使用说明", "100px");
    tabbar.cells("main").setActive();
    tabbar.enableTabCloseButton(true);
    var arr = new Array();
    tabbar.attachEvent("onTabClose", function (id, pid) {
        try {
            if (id == 'main') {
                alert('第一个页面不能关闭');
                return false;
            }
            arr.remove(id);
            return true;
        } catch (e) {
        }
    });

    function addTab(url, tabTitle, tabName, width) {
        if (width == '')
            width = '100px';
        if (arr.indexOf(tabName) != -1)
            tabbar.setTabActive(tabName);
        else {
            url = contextpath + url;
            arr.append(tabName);
            tabbar.addTab(tabName, tabTitle, width);
            tabbar.cells(tabName).attachURL(url);
            tabbar.tabs(tabName).setActive();
        }
    }


</script>
</body>
</html>