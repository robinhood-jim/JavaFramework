<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
    <script src="<%=CONTEXT_PATH%>component/dhtmlxSuite/codebase/dhtmlx.js"></script>
    <script language="javascript" src="<%= CONTEXT_PATH %>resources/js/jquery.js"></script>
    <link rel="stylesheet" type="text/css" href="<%=CONTEXT_PATH%>resources/css/main.css"/>

    <title></title>
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
    var ctx = "<%=CONTEXT_PATH%>";
    var imgPath="<%=CONTEXT_PATH%>component/dhtmlxSuite/dhtmlx/imgs/icon/";
    var dhxLayout = new dhtmlXLayoutObject(document.body, "2E");
    var topPanel=dhxLayout.cells("a");
    var bottomPanel=dhxLayout.cells("b");

</script>

</body>
</html>
