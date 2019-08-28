<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    String path = request.getContextPath();
    String CONTEXT_PATH = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path + "/";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title><spring:message code="title.userRight"/></title>
    <link rel="stylesheet" type="text/css" href="<%=CONTEXT_PATH%>component/dhtmlxSuite/codebase/dhtmlx.css"/>
    <script src="<%=CONTEXT_PATH%>component/dhtmlxSuite/codebase/dhtmlx.js"></script>
    <link rel="stylesheet" type="text/css" href="<%=CONTEXT_PATH%>resources/css/main.css"/>
    <script language="javascript" src="<%= CONTEXT_PATH %>resources/js/jquery.js"></script>
    <script language="javascript" src="<%= CONTEXT_PATH %>resources/js/Array.js"></script>
    <style type="text/css">
        div#layoutObj {
            position: relative;
            margin-top: 10px;
            margin-left: 10px;
            margin-right: 10px;
            margin-bottom: 10px;
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
    dhtmlx.message.defPosition = "bottom";
    var contextpath = "<%=CONTEXT_PATH%>";
    var queryUrl = contextpath + "system/user/showright";
    var userId =<c:out value="${userId}" />
    var cwidth = document.body.clientWidth;
    var dhxLayout = new dhtmlXLayoutObject(document.body, "1C");
    var dhxToobar = dhxLayout.cells("a").attachToolbar();
    dhxLayout.cells("a").setText("<spring:message code="title.adjustUserRightTip" />");
    dhxToobar.setIconsPath(contextpath + "component/dhtmlxSuite/comm/imgs/");
    dhxToobar.addButton("save", 0, "<spring:message code="btn.save" />", "new.gif", "new_dis.gif");
    dhxToobar.addButton("cancel", 2, "<spring:message code="btn.cancel" />", "close.gif", "close_dis.gif");
    var dhxTree = dhxLayout.cells("a").attachTree();
    dhxTree.setImagePath("<%=CONTEXT_PATH%>component/dhtmlxSuite/codebase/imgs/dhxtree_material/");

    dhxTree.enableCheckBoxes(true);

    dhxToobar.attachEvent("onClick", function (id) {
        if (id == 'save') {
            goAssign();
        }
    });

    $.ajax({
        type: "post",
        data: {"userId": userId},
        url: contextpath + "system/user/listright",
        dataType: "json",
        success: function (data) {
            var obj = eval(data);
            dhxTree.loadJSONObject(obj);
        }
    });

    function goAssign() {
        var id = dhxTree.getAllChecked();
        if (id == '') {
            openMsgDialog("<spring:message code="title.adjustUserRight"/>", "<spring:message code="message.selectMenu" />");
        } else {
            $.ajax({
                type: "post",
                data: {"ids": id, "userId": userId},
                url: contextpath + "system/user/assignright",
                dataType: "json",
                success: function (data) {
                    var obj = eval(data);
                    if (obj.success == 'true') {
                        dhtmlx.message({
                            text: "<spring:message code="message.updateSuccess" />",
                            expire: 10
                        });
                        parent.closedialog();
                    } else {
                        openMsgDialog("<spring:message code="message.updateFailed" />", "<spring:message code="message.ErrorMsg"/>" + obj.message);
                    }
                }
            });
        }
    }


</script>

</body>
</html>