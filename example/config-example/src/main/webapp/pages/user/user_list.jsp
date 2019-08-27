<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<%
    String path = request.getContextPath();
    String CONTEXT_PATH = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path + "/";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title><spring:message code="userContr" /> </title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="cache-control" content="no-cache">
    <meta http-equiv="expires" content="0">
    <link rel="stylesheet" type="text/css" href="<%=CONTEXT_PATH%>component/dhtmlxSuite/skins/skyblue/dhtmlx.css"/>
    <link rel="stylesheet" type="text/css" href="<%=CONTEXT_PATH%>component/dhtmlxSuite/codebase/dhtmlx.css"/>
    <link rel="stylesheet" type="text/css"
          href="<%=CONTEXT_PATH%>component/dhtmlxSuite/codebase/fonts/font_roboto/roboto.css"/>
    <link rel="stylesheet" type="text/css" href="<%=CONTEXT_PATH%>resources/css/main.css"/>
    <script src="<%=CONTEXT_PATH%>component/dhtmlxSuite/codebase/dhtmlx.js"></script>
    <script language="javascript" src="<%= CONTEXT_PATH %>resources/js/jquery.js"></script>
    <script language="javascript" src="<%= CONTEXT_PATH %>resources/js/Array.js"></script>
    <script language="javascript" src="<%= CONTEXT_PATH %>resources/js/control.js"></script>
    <script src="<%=CONTEXT_PATH%>resources/js/validate.js"></script>
    <style type="text/css">

        html, body {
            width: 100%;
            height: 100%;
            border: none;
            overflow: hidden;
        }

    </style>
    <script type="text/javascript">
        $(document).ready(function () {

        });
    </script>

</head>

<body onload="">
<script type="text/javascript" src="<%=CONTEXT_PATH%>resources/js/crud.js"></script>
<script type="text/javascript">
    var ctx = "<%=CONTEXT_PATH%>";
    var imgPath = "<%=CONTEXT_PATH%>component/dhtmlxSuite/dhtmlx/imgs/icon/";
    var dhxLayout = new dhtmlXLayoutObject(document.body, "2E");
    var topPanel = dhxLayout.cells("a");
    var bottomPanel = dhxLayout.cells("b");
    var width=document.body.clientWidth;

    topPanel.setHeight(130);
    topPanel.setText("");

    var myForm = topPanel.attachForm();
    //myForm.setSkin("dhx_skyblue");
    var cwidth = document.body.clientWidth;
    var queryUrl = ctx + "system/user/list";
    var userFrm = [
        {
            type: "block", lable: "<spring:message code="sysUser.info" />", width: width-40, list: [
                {type: "settings", position: "label-left", labelWidth: 120, inputWidth: 120, offsetLeft: 10},
                {
                    type: "fieldset", labelAlign: "left", label: "<spring:message code="sysUser.info" />", inputWidth: width-80, list: [
                        {type: "hidden", name: "query.order", value: ""},
                        {type: "hidden", name: "query.orderDirection", value: ""},
                        {type: "hidden", name: "query.pageCount", value: ""},
                        {type: "hidden", name: "query.pageNumber", value: "1"},
                        {type: "hidden", name: "query.pageSize", value: "10"},
                        {type: "input", label: "<spring:message code="sysUser.accountName" />", name: "userName", offsetTop: 10},
                        {type: "newcolumn", offset: 20},
                        {type: "select", label: "<spring:message code="sysUser.Dept" />", name: "deptId",connector:ctx + "system/dept/listjson?allowNull=true"},
                        {type: "newcolumn", offset: 20},
                        {type: "select", label: "<spring:message code="sysUser.Org" />", name: "orgId",connector:ctx + "system/org/listjson?allowNull=true"},

                    ]
                }, {
                    type: "block", inputWidth: width-80, list: [
                        {type: "settings", offsetTop: 5},
                        {type: "button", name: "submit", value: "<spring:message code="btn.submit" />",offsetLeft:width/2-220},
                        {type: "newcolumn"},
                        {type: "button", name: "reset", value: "<spring:message code="btn.clear" />"}
                    ]
                }
            ]
        }
    ];
    topPanel.hideHeader();
    bottomPanel.hideHeader();
    topPanel.fixSize(true, true);
    myForm.loadStruct(userFrm);

    myForm.attachEvent("onButtonClick", function (name) {
        if (name == 'submit') {
            this.send(ctx + "system/user/list", function (loader, response) {
                var tobj = eval('(' + response + ')');
                myGrid.clearAll();
                myGrid.parse(tobj, "json");
                var barstr = tobj.query.pageToolBar;
                statusbar.setText(constructPaging(tobj.query));
            });
        } else if (name == 'reset' ) {
            this.reset();
        }
    });
    var statusbar = bottomPanel.attachStatusBar({height: 36});
    statusbar.setText("<div id='recinfoArea'></div>");

    var myGrid = bottomPanel.attachGrid();
    myGrid.setImagePath(ctx + "component/dhtmlxSuite/codebase/imgs/");
    myGrid.setHeader("#master_checkbox,img:[" + ctx + "resources/images/icon/men.gif]<b><spring:message code="sysUser.accountName" /></b>,<spring:message code="sysUser.accountType" />,<spring:message code="sysUser.Dept" />,<spring:message code="sysUser.Org" />", null, ["text-align:center", "text-align:center", "text-align:center", "text-align:center", "text-align:center"]);//the headers of columns
    myGrid.setInitWidths("35,150,200,200,200");          //the widths of columns
    myGrid.setColAlign("center,center,center,center,center");       //the alignment of columns
    myGrid.setColTypes("ch,ro,ro,ro,ro");                //the types of columns
    myGrid.setColSorting("int,str,str,str,str");          //the sorting types
    myGrid.enableAutoWidth(true);
    myGrid.enableAutoHeight(true);
    myGrid.init();

    var dhxToobar = bottomPanel.attachToolbar();

    dhxToobar.setIconsPath(ctx + "component/dhtmlxSuite/comm/imgsmat/");
    dhxToobar.addButton("new", 0, "<spring:message code="btn.add" />", "new.gif", "new_dis.gif");
    dhxToobar.addButton("edit", 1, "<spring:message code="btn.modi" />", "open.gif", "open_dis.gif");
    dhxToobar.addButton("delete", 2, "<spring:message code="btn.delete" />", "close.gif", "close_dis.gif");
    dhxToobar.addButton("changepwd", 3, "<spring:message code="btn.changepwd" />", "open.gif", "open_dis.gif");
    dhxToobar.addButton("active", 4, "<spring:message code="btn.active" />", "open.gif", "open_dis.gif");
    dhxToobar.addButton("assign", 5, "<spring:message code="btn.assingRight" />", "open.gif", "open_dis.gif");
    dhxToobar.attachEvent("onClick", function (id) {
        if (id == 'new') {
            goAdd();
        } else if (id == 'edit') {
            goEdit();
        } else if (id == 'delete') {
            goDelete();
        } else if (id == 'assign') {
            goAssign();
        }else if(id =='changepwd'){
            goChangePwd();
        }else if(id=='active'){
            goActive();
        }
    });

    function goSearch() {
        myForm.send(ctx + "system/user/list", function (loader, response) {
            var tobj = eval('(' + response + ')');
            myGrid.clearAll();
            myGrid.parse(tobj, "json");
            statusbar.setText(constructPaging(tobj.query));
        });

    }


    var editFormContent = [
        {type: "settings", position: "label-left", lableWidth: 120, inputWidth: 120},
        {
            type: "fieldset", label: "<spring:message code="sysUser.info" />", offsetLeft: 10,offsetRight: 20, inputWidth: 495, lableWidth: 100, list: [
                {type: "hidden", name: "id", value: ""},
                {type: "input", name: "userName", label: "<spring:message code="sysUser.userName" />:", validate: "NotEmpty"},
                {
                    type: "select", name: "accountType", label: "<spring:message code="sysUser.accountType" />:", validate: "NotEmpty",connector: ctx + "system/codecombo?codeSetNo=USERTYPE"
                },
                {type: "select", name: "orgId", label: "<spring:message code="sysUser.Org" />:", validate: "NotEmpty",connector:ctx + "system/org/listjson?allowNull=false"},
                {type: "newcolumn", offset: 20},
                {type: "input", name: "userAccount", label: "<spring:message code="sysUser.accountName" />:", validate: "NotEmpty"},
                {type: "select", name: "deptId", label: "<spring:message code="sysUser.Dept" />:", validate: "NotEmpty",connector:ctx + "system/dept/listjson?allowNull=false"}
            ]
        },
        {
            type: "block", inputWidth: 490, list: [
                {type: "settings", offsetTop: 10},
                {type: "button", name: "cmdOK", value: "<spring:message code="btn.confirm" />", offsetLeft: 175},
                {type: "newcolumn"},
                {type: "button", name: "cmdCancel", value: "<spring:message code="btn.cancel" />"}
            ]
        }
    ];

    function addInit(form) {
        form.attachEvent("onButtonClick", function (name, command) {
            form.validate();
            if (name == "cmdOK") {
                this.send(ctx + "system/user/save", function (loader, response) {
                    var tobj = eval('(' + response + ')');
                    if (tobj.success == true) {
                        dhtmlx.message({
                            text: "<spring:message code="message.saveSuccess" />",
                            expire: -1
                        });
                        closedialog(true);
                        reload();
                    } else {
                        openMsgDialog("<spring:message code="message.SaveFailed" />", "<spring:message code="message.errorMsg" />:" + tobj.message);
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
                this.send(contextpath + "system/user/update", function (loader, response) {
                    var tobj = eval('(' + response + ')');
                    if (tobj.success == true) {
                        dhtmlx.message({
                            text: "<spring:message code="message.updateSuccess" />",
                            expire: -1
                        });
                        closedialog(true);
                        reload();
                    } else {
                        openMsgDialog("<spring:message code="message.updateFailed" />", "<spring:message code="message.errorMsg" />:" + tobj.message);
                    }
                });
            } else if (name == 'cmdCancel') {
                closedialog(false);
            }
        });
    }

    function goAdd() {
        openWindowForAdd("<spring:message code="title.AddUser" />", editFormContent, 520, 270, addInit);
    }

    function goAssign() {
        var list = myGrid.getCheckedRows(0);
        if (list == '') {
            openMsgDialog("<spring:message code="sysUser.AssignRight" />", "<spring:message code="message.alertSelectAtLeastOneRow" />");
        } else if (list.indexOf(",") != -1) {
            openMsgDialog("<spring:message code="sysUser.AssignRight" />", "<spring:message code="message.alertSelectMutilRow" />");
        } else {
            var form = openWindow("<spring:message code="sysUser.AssignRight" />", ctx + "system/user/showright?userId=" + list, 550, 400);
        }
    }

    function goDelete() {
        var list = myGrid.getCheckedRows(0);
        if (list == '') {
            openMsgDialog("<spring:message code="title.delUser" />", "<spring:message code="message.alertSelectAtLeastOneRow" />", 300, 200);
        } else {
            dhtmlx.confirm({
                title: "<spring:message code="title.del" />",
                type: "confirm-warning",
                text: "<spring:message code="message.confirmDelete" />",
                callback: function (result) {
                    if (result) {
                        $.ajax({
                            type: "get",
                            url: ctx + "system/user/delete?ids=" + list,
                            dataType: "json",
                            success: function (data) {
                                var obj = eval(data);
                                if (obj.success == true) {
                                    dhtmlx.message({
                                        text: "<spring:message code="message.deleteSuccess" />",
                                        expire: 10
                                    });
                                    reload();
                                } else {
                                    openMsgDialog("<spring:message code="message.deleteFailed" />", "<spring:message code="message.errorMsg" />:" + obj.message, 300, 200);
                                }
                            }
                        });
                    }
                }
            });
        }
    }
    function goActive() {
        var list = myGrid.getCheckedRows(0);
        if (list == '') {
            openMsgDialog("<spring:message code="title.activeUser" />", "<spring:message code="message.alertSelectAtLeastOneRow" />", 300, 200);
        }
        else if (list.indexOf(",") != -1) {
            openMsgDialog("<spring:message code="title.activeUser" />", "<spring:message code="message.alertSelectMutilRow" />", 300, 150);
        }
        else {
            dhtmlx.confirm({
                title: "<spring:message code="title.activeUser" />",
                type: "confirm-warning",
                text: "<spring:message code="message.confirm" />",
                callback: function (result) {
                    if (result) {
                        $.ajax({
                            type: "get",
                            url: ctx + "system/user/active?id=" + list,
                            dataType: "json",
                            success: function (data) {
                                var obj = eval(data);
                                if (obj.success == true) {
                                    dhtmlx.message({
                                        text: "<spring:message code="message.saveSuccess" />",
                                        expire: 10
                                    });
                                    reload();
                                } else {
                                    openMsgDialog("<spring:message code="message.SaveFailed" />", "<spring:message code="message.errorMsg" />:" + obj.message, 300, 200);
                                }
                            }
                        });
                    }
                }
            });
        }
    }
    function goChangePwd(){

        var haspasswd=true;
        var list = myGrid.getCheckedRows(0);
        if (list == '') {
            openMsgDialog("<spring:message code="title.editUser" />", "<spring:message code="message.alertSelectAtLeastOneRow" />", 300, 150);
        } else if (list.indexOf(",") != -1) {
            openMsgDialog("<spring:message code="title.editUser" />", "<spring:message code="message.alertSelectMutilRow" />", 300, 150);
        } else {
            $.ajax({
                type: "get",
                url: ctx + "system/user/edit?id=" + list,
                dataType: "json",
                success: function (data) {
                    var obj = eval(data);
                    if (obj.success == true) {
                        if(obj.model.userPassword==null || obj.model.userPassword==''){
                            haspasswd=false;
                        }
                    }
                    var content=constructChangePwdForm(list,haspasswd)
                    openWindowForAdd("<spring:message code="title.changePwd" />", content, 520, 250, changePwd);
                }
            });

        }

    }
    function constructChangePwdForm(id,haspasswd) {
        var content='[{type: "settings", position: "label-left", lableWidth: 120, inputWidth: 120},{'
            +'type: "fieldset", label: "<spring:message code="sysUser.info" />", offsetLeft: 10,offsetRight: 20, inputWidth: 495, lableWidth: 100, list: ['
        +'{type: "hidden", name: "id", value: "'+id+'"},';
        if(haspasswd)
            content+='{type: "input", name: "orgPwd", label: "<spring:message code="sysUser.orgPwd" />:", validate: "NotEmpty"},';

        content+='{type: "input", name: "newPwd", label: "<spring:message code="sysUser.newPwd" />:", validate: "NotEmpty"},'
        +'{type: "input", name: "confirmPwd", label: "<spring:message code="sysUser.confirmPwd" />:", validate: "NotEmpty"},'
    +']},{'
        +'type: "block", inputWidth: 490, list: ['
        +'{type: "settings", offsetTop: 10},'
        +'{type: "button", name: "cmdOK", value: "<spring:message code="btn.confirm" />", offsetLeft: 175},'
        +'{type: "newcolumn"},'
        +'{type: "button", name: "cmdCancel", value: "<spring:message code="btn.cancel" />"}'
    +']}]';
        return content;
    }
    function changePwd(form) {
        form.attachEvent("onButtonClick", function (name, command) {
            if (name == "cmdOK") {
                if(this.getItemValue("newPwd")!=null && this.getItemValue("confirmPwd")!=null && this.getItemValue("newPwd")==this.getItemValue("confirmPwd")) {
                    this.send(ctx + "system/user/changepwd", function (loader, response) {
                        var tobj = eval('(' + response + ')');
                        if (tobj.success == true) {
                            dhtmlx.message({
                                text: "<spring:message code="message.saveSuccess" />",
                                expire: -1
                            });
                            closedialog(true);
                            reload();
                        } else {
                            openMsgDialog("<spring:message code="message.SaveFailed" />", "<spring:message code="message.errorMsg" />:" + tobj.message);
                        }

                    });
                }else{
                    openMsgDialog("<spring:message code="title.changePwd" />", "<spring:message code="message.passwordNotMatch" />", 300, 200);
                }
            }
        });
    }

    function goEdit() {
        var list = myGrid.getCheckedRows(0);
        if (list == '') {
            openMsgDialog("<spring:message code="title.editUser" />", "<spring:message code="message.alertSelectAtLeastOneRow" />", 300, 150);
        } else if (list.indexOf(",") != -1) {
            openMsgDialog("<spring:message code="title.editUser" />", "<spring:message code="message.alertSelectMutilRow" />", 300, 150);
        } else {
            var form = openWindowForEdit("<spring:message code="title.editUser" />", editFormContent, 530, 270, editInit);
            $.ajax({
                type: "get",
                url: ctx + "system/user/edit?id=" + list,
                dataType: "json",
                success: function (data) {
                    var obj = eval(data);
                    form.setItemValue("id", obj.model.id);
                    form.setItemValue("userName", obj.model.userName);
                    form.setItemValue("userAccount", obj.model.userAccount);
                    form.setItemValue("deptId", obj.model.deptId);
                    form.setItemValue("accountType", obj.model.accountType);
                    form.setItemValue("orgId", obj.model.orgId);
                }
            });
        }
    }
</script>

</body>
</html>