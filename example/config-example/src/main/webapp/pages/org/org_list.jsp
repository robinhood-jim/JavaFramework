<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%
    String path = request.getContextPath();
    String CONTEXT_PATH = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path + "/";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>

    <link rel="stylesheet" type="text/css" href="<%=CONTEXT_PATH%>component/dhtmlxSuite/skins/skyblue/dhtmlx.css"/>
    <link rel="stylesheet" type="text/css" href="<%=CONTEXT_PATH%>component/dhtmlxSuite/codebase/dhtmlx.css"/>
    <link rel="stylesheet" type="text/css"
          href="<%=CONTEXT_PATH%>component/dhtmlxSuite/codebase/fonts/font_roboto/roboto.css"/>
    <script src="<%=CONTEXT_PATH%>component/dhtmlxSuite/codebase/dhtmlx.js"></script>
    <script language="javascript" src="<%= CONTEXT_PATH %>resources/js/jquery.js"></script>
    <link rel="stylesheet" type="text/css" href="<%=CONTEXT_PATH%>resources/css/main.css"/>

    <title></title>
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
<script type="text/javascript">
    dhtmlx.message.position = "bottom";
    var pid;
    var id;
    var ctx = "<%=CONTEXT_PATH%>";
    var queryUrl = ctx + "system/org/list";
    var dhxLayout = new dhtmlXLayoutObject(document.body, "2U");
    //var dhxLayout = new dhtmlXLayoutObject(document.body, "1C");
    var cheight = document.body.clientHeight;
    var cwidth = document.body.clientWidth;

    var leftPanel = dhxLayout.cells("a");
    var rightPanel = dhxLayout.cells("b");
    leftPanel.setWidth(136);
    leftPanel.setText("<spring:message code="title.org" />");
    var dhxTree = leftPanel.attachTree();


    dhxTree.setImagePath("<%=CONTEXT_PATH%>component/dhtmlxSuite/codebase/imgs/dhxtree_material/");


    dhxTree.setSkin('dhx_skyblue');
    dhxTree.setImagePath("<%=CONTEXT_PATH%>component/dhtmlxSuite/codebase/imgs/dhxtree_material/");
    dhxTree.setXMLAutoLoading(ctx + "system/org/tree");
    dhxTree.setDataMode("json");
    //load first level of tree
    dhxTree.loadJSON(ctx + "system/org/tree?id=0");
    myContextMenu = new dhtmlXMenuObject();
    myContextMenu.renderAsContextMenu();
    myContextMenu.setIconsPath(ctx + "component/dhtmlxSuite/comm/imgs/");
    myContextMenu.loadStruct(ctx + "system/org/contextmenu");
    myContextMenu.attachEvent("onClick", onButtonClick);
    dhxTree.enableContextMenu(myContextMenu);
    dhxTree.attachEvent("onClick", function (id) {
        $("#container").attr("src",ctx+"system/user/show?orgId="+id+"&lockorg=1");
    });

    function onButtonClick(menuitemId, type) {
        var sid = dhxTree.contextID;
        pid = sid;
        if (menuitemId == 'new') {
            goAdd();
        } else if (menuitemId == 'newtop') {
            pid = "0";
            goAdd();
        } else if (menuitemId == "open") {
            id = sid;
            goEdit();
        }else if(menuitemId == "delete"){
            id=sid;
            goDelete();
        }
    }

</script>
<div id="mainFrame">
    <iframe id="container" style="overflow-y:auto;overflow-x:hidden!important;width:100%;height:100%" frameborder="no"
            src=""></iframe>
</div>
<script type="text/javascript">

    rightPanel.hideHeader();
    var myGrid = rightPanel.attachObject("mainFrame");
    $("#container").height(cheight-20)

    //add edit form
    var editFormContent = [
        {type: "settings", position: "label-left", lableWidth: 100, inputWidth: 120},
        {
            type: "fieldset", label: "部门信息", offsetLeft: 10, inputWidth: 500, lableWidth: 100, list: [
                {type: "hidden", name: "id", value: ""},
                {type: "hidden", name: "upOrgId", value: ""},
                {type: "input", name: "orgName", label: "机构名:", validate: "NotEmpty"},
                {type: "input", name: "orgCode", label: "机构编码 :", validate: "NotEmpty"},
                {
                    type: "select", name: "orgStatus", label: "状态:", options: [
                        {text: "生效", value: "1"},
                        {text: "失效", value: "0"}
                    ]
                },
                {type: "newcolumn", offset: 20},
                {
                    type: "select", name: "orgType", label: "机构类型:", options: [
                        {text: "一级机构", value: "1"},
                        {text: "二级机构", value: "2"}
                    ]
                },
                {type: "input", name: "upOrg", label: "上级机构:", disabled: "true"},
            ]
        },
        {
            type: "block", inputWidth: 170, offsetLeft: 160, list: [
                {type: "button", name: "cmdOK", width: 40, value: "确定"},
                {type: "newcolumn", offset: 20},
                {type: "button", name: "cmdCancel", width: 40, value: "取消"}
            ]
        }
    ];

    function goAdd() {
        openWindowForAdd("添加工程", editFormContent, 520, 270, addInit);
    }

    function goEdit() {
        var list = id;
        if (list == '') {
            openMsgDialog("编辑机构", "请选择机构", 300, 150);
        } else if (list.indexOf(",") != -1) {
            openMsgDialog("编辑用户", "只能修改一个用户", 300, 150);
        } else {
            var form = openWindowForEdit("修改机构", editFormContent, 530, 270, editInit);
            //form.load(ctx+"system/org/edit?id="+list);
            $.ajax({
                type: "get",
                url: ctx + "system/org/edit/" + list,
                dataType: "json",
                success: function (data) {
                    var obj = eval(data);
                    form.setItemValue("id", obj.id);
                    form.setItemValue("orgName", obj.model.orgName);
                    form.setItemValue("orgCode", obj.model.orgCode);
                    form.setItemValue("upOrgId", obj.model.upOrgId);
                    form.setItemValue("orgType", obj.model.orgType);
                    form.setItemValue("orgStatus", obj.model.orgStauts);
                }
            });
        }
    }

    function addInit(form) {
        $.ajax({
            type: "get",
            url: ctx + "system/org/getuporg?pid=" + pid,
            dataType: "json",
            success: function (data) {
                var obj = eval(data);
                form.setItemValue("upOrgId", obj.id);
                form.setItemValue("upOrg", obj.text);
            }
        });
        form.attachEvent("onButtonClick", function (name, command) {
            form.validate();
            if (name == "cmdOK") {
                this.send(ctx + "system/org/save", function (loader, response) {
                    var tobj = eval('(' + response + ')');
                    if (tobj.success == true) {
                        dhtmlx.message({
                            text: tobj.message,
                            expire: -1
                        });
                        closedialog(true);
                        reload();
                    } else {
                        openMsgDialog("保存失败", "错误信息:" + tobj.message, 300, 200);
                    }
                });
            } else if (name == 'cmdCancel') {
                closedialog(false);
            }
        });
    }

    function editInit(form) {

        form.attachEvent("onButtonClick", function (name, command) {
            if (name == "cmdOK") {
                this.send(ctx + "system/org/update", function (loader, response) {
                    var tobj = eval('(' + response + ')');
                    if (tobj.success == 'true') {
                        dhtmlx.message({
                            text: "修改成功",
                            expire: -1
                        });
                        closedialog(true);
                        reload();
                    } else {
                        openMsgDialog("修改用户失败", "错误信息:" + tobj.message, 300, 200);
                    }
                });
            } else if (name == 'cmdCancel') {
                closedialog(false);
            }
        });
    }

    function goAdd() {
        openWindowForAdd("添加用户", editFormContent, 550, 250, addInit);
    }

    function goAssign() {
        var list = myGrid.getCheckedRows(0);
        if (list == '') {
            openMsgDialog("用户赋权", "请选择用户", 300, 150);
        } else if (list.indexOf(",") != -1) {
            openMsgDialog("用户赋权", "只能修改一个用户", 300, 150);
        } else {
            var form = openWindow("用户菜单赋权", ctx + "system/sysuser/showright?userId=" + list, 550, 400);
        }
    }

    function goDelete() {

        if (id == '') {
            openMsgDialog("删除角色", "请选择角色", 300, 200);
        } else {
            dhtmlx.confirm({
                title: "删除",
                type: "confirm-warning",
                text: "确定要删除对应的记录?",
                callback: function (result) {
                    if (result) {
                        $.ajax({
                            type: "get",
                            url: ctx + "system/org/delete?ids=" + id,
                            dataType: "json",
                            success: function (data) {
                                var obj = eval(data);
                                if (obj.success == 'true') {
                                    dhtmlx.message({
                                        text: "删除成功",
                                        expire: 10
                                    });
                                    reload();
                                } else {
                                    openMsgDialog("删除用户失败", "错误信息:" + obj.message, 300, 200);
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    function reload() {
        dhxTree.deleteChildItems(0);
        dhxTree.loadJSON(ctx + "system/org/tree?id=0");
    }
</script>
<script type="text/javascript" src="<%= CONTEXT_PATH %>resources/js/crud.js"></script>
</body>
</html>