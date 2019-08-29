<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
    <link rel="stylesheet" type="text/css" href="<%=CONTEXT_PATH%>resources/css/main.css"/>
    <script src="<%=CONTEXT_PATH%>component/dhtmlxSuite/codebase/dhtmlx.js"></script>
    <script language="javascript" src="<%= CONTEXT_PATH %>resources/js/jquery.js"></script>

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
<script type="text/javascript" src="<%= CONTEXT_PATH %>resources/js/crud.js"></script>
<script type="text/javascript">
    dhtmlx.message.position = "bottom";
    var contextpath = "<%=CONTEXT_PATH%>";
    var queryUrl = contextpath + "system/role/list";

    var dhxLayout = new dhtmlXLayoutObject(document.body, "2E");
    var cheight = document.body.clientHeight;

    var cwidth = document.body.clientWidth;
    var topPanel = dhxLayout.cells("a");
    var bottomPanel = dhxLayout.cells("b");
    topPanel.setText("");
    topPanel.hideHeader();
    bottomPanel.hideHeader();
    topPanel.setHeight(130);
    var formStructure = [
        {
            type: "block", lable: "项目名", width: cwidth-40, list: [
                {type: "settings", position: "label-left", lableWidth: 130, inputWidth: 130,offsetLeft: 10},
                {
                    type: "fieldset", label: "查询条件", inputWidth: cwidth-80, list: [
                        {type: "hidden", name: "query.order", value: ""},
                        {type: "hidden", name: "query.orderDirection", value: ""},
                        {type: "hidden", name: "query.pageCount", value: ""},
                        {type: "hidden", name: "query.pageNumber", value: "1"},
                        {type: "hidden", name: "query.pageSize", value: "10"},
                        {type: "input", name: "username", label: "角色名:", width: 150},

                    ]
                },{
                    type: "block", inputWidth: cwidth-80,  list: [
                        {type: "settings", offsetTop: 10},
                        {type: "button", name: "cmdOK", width: 40, value: "查询",offsetLeft:cwidth/2-220},
                        {type: "newcolumn",offset:60},
                        {type: "button", name: "cmdReset", width: 40, value: "重置"}
                    ]
                }
            ]
        }

    ];
    var dhxForm = topPanel.attachForm();
    dhxForm.loadStruct(formStructure, "json");
    var myGrid = bottomPanel.attachGrid();
    myGrid.selMultiRows = false;
    topPanel.fixSize(true, false);
    var sb = bottomPanel.attachStatusBar({height: 25});
    sb.setText("<div id='recinfoArea'></div>");
    var dhxToobar = dhxLayout.cells("b").attachToolbar();

    dhxToobar.setIconsPath(contextpath + "component/dhtmlxSuite/comm/imgsmat/");
    dhxToobar.addButton("new", 0, "新增", "new.gif", "new_dis.gif");
    dhxToobar.addButton("edit", 1, "修改", "open.gif", "open_dis.gif");
    dhxToobar.addButton("delete", 2, "删除", "close.gif", "close_dis.gif");
    dhxToobar.addButton("assign", 3, "菜单赋权", "open.gif", "open_dis.gif");
    dhxToobar.attachEvent("onClick", function (id) {
        if (id == 'new') {
            goAdd();
        } else if (id == 'edit') {
            goEdit();
        } else if (id == 'delete') {
            goDelete();
        } else if (id == 'assign') {
            goAssign();
        }
    });
    dhxForm.attachEvent("onButtonClick", function (name, command) {
        if (name == "cmdOK") {
            this.send(contextpath + "system/role/list", function (loader, response) {
                var tobj = eval('(' + response + ')');
                myGrid.clearAll();
                myGrid.parse(tobj, "json");
                sb.setText(constructPaging(tobj.query));
            });
        } else if (name == "cmdReset") {
            this.reset();
        }
    });

    myGrid.setImagePath(contextpath + "component/dhtmlxSuite/codebase/imgs/");
    myGrid.setHeader("#master_checkbox,角色名,类型,状态");//set column names
    myGrid.setInitWidths("30,150,70,100");//set column width in px
    myGrid.setColAlign("center,center,center,center");//set column values align
    myGrid.setColTypes("ch,txt,txt,txt,txt,txt");//set column types
    myGrid.init();//initialize grid

    $.ajax({
        type: "post",
        url: contextpath + "system/role/list",
        dataType: "json",
        success: function (data) {
            var obj = eval(data);
            myGrid.parse(data, "json");
            sb.setText(constructPaging(obj.query));
            var query = obj.query;
            dhxForm.setItemValue("query.pageCount", query.pageCount);
            dhxForm.setItemValue("query.pageNumber", query.pageNumber);
            dhxForm.setItemValue("query.pageSize", query.pageSize);
            dhxForm.setItemValue("query.orderDirection", query.orderDirection);
            dhxForm.setItemValue("query.order", query.order);
        }
    });
</script>

<script type="text/javascript">

    var editFormContent = [
        {type: "settings", position: "label-left", lableWidth: 120, inputWidth: 120},
        {
            type: "fieldset", label: "角色信息", offsetLeft: 10,offsetRight: 10, inputWidth: 495, lableWidth: 100, list: [
                {type: "hidden", name: "id", value: ""},
                {type: "input", name: "roleName", label: "角色名:", validate: "NotEmpty"},
                {
                    type: "select", name: "roleType", label: "角色类型:", options: [
                        {text: "系统角色", value: "1"},
                        {text: "一般角色", value: "2"},
                    ]
                },
                {
                    type: "select", name: "roleStatus", label: "状态:", options: [
                        {text: "有效", value: "1"},
                        {text: "无效", value: "0"},
                    ]
                }
            ]
        },
        {
            type: "block", inputWidth: 490, list: [
                {type: "settings", offsetTop: 10},
                {type: "button", name: "cmdOK", value: "确定", offsetLeft: 175},
                {type: "newcolumn",offset:30},
                {type: "button", name: "cmdCancel", value: "取消"}
            ]
        }
    ];

    function addInit(form) {

        form.attachEvent("onButtonClick", function (name, command) {
            form.validate();
            if (name == "cmdOK") {
                this.send(contextpath + "system/role/save", function (loader, response) {
                    var tobj = eval('(' + response + ')');
                    if (tobj.success == 'true') {
                        dhtmlx.message({
                            text: "保存成功",
                            expire: -1
                        });
                        closedialog(true);
                        reload();
                    } else {
                        openMsgDialog("保存角色失败", "错误信息:" + tobj.message, 300, 200);
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
                this.send(contextpath + "system/role/update", function (loader, response) {
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
        openWindowForAdd("添加用户", editFormContent, 530, 270, addInit);
    }

    function goAssign() {
        var list = myGrid.getCheckedRows(0);
        if (list == '') {
            openMsgDialog("用户赋权", "请选择用户", 300, 150);
        } else if (list.indexOf(",") != -1) {
            openMsgDialog("用户赋权", "只能修改一个用户", 300, 150);
        } else {
            var form = openWindow("用户菜单赋权", contextpath + "system/user/showright?userId=" + list, 550, 400);
        }
    }

    function goDelete() {
        var list = myGrid.getCheckedRows(0);
        if (list == '') {
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
                            url: contextpath + "system/role/delete?ids=" + list,
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

    function goEdit() {
        var list = myGrid.getCheckedRows(0);
        if (list == '') {
            openMsgDialog("编辑角色", "请选择角色", 300, 150);
        } else if (list.indexOf(",") != -1) {
            openMsgDialog("编辑角色", "只能修改一个角色", 300, 150);
        } else {
            var form = openWindowForEdit("修改角色", editFormContent, 530, 270, editInit);
            //form.load(contextpath+"system/sysuser/edit?id="+list);
            $.ajax({
                type: "get",
                url: contextpath + "system/role/edit?id=" + list,
                dataType: "json",
                success: function (data) {
                    var obj = eval(data);
                    form.setItemValue("id", obj.id);
                    form.setItemValue("roleName", obj.roleName);
                    form.setItemValue("roleType", obj.roleType);
                    form.setItemValue("roleStatus", obj.roleStatus);
                    form.setItemValue("accountType", obj.accountType);
                    form.setItemValue("orgId", obj.orgId);
                }
            });
        }
    }

    function reload() {
        dhxForm.send(contextpath + "system/role/list", function (loader, response) {
            var tobj = eval('(' + response + ')');
            myGrid.clearAll();
            myGrid.parse(tobj, "json");
            var barstr = tobj.query.pageToolBar;
            sb.setText(barstr);
        });
    }
</script>

</body>
</html>