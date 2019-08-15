<%@ page language="java" contentType="text/html;charset=UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%
	String path = request.getContextPath();
	String CONTEXT_PATH = request.getScheme() + "://"
			+ request.getServerName() + ":" + request.getServerPort()
			+ path + "/";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" >
<html>
<head>
<title><spring:message code="loginPage" /> </title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta http-equiv="X-UA-Compatible" content="IE=edge" />
<link rel="stylesheet" type="text/css" href="<%=CONTEXT_PATH%>component/dhtmlxSuite/codebase/dhtmlx.css" />
<link rel="stylesheet" type="text/css" href="<%=CONTEXT_PATH%>component/dhtmlxSuite/skins/terrace/dhtmlx.css"/>
<script src="<%=CONTEXT_PATH%>component/dhtmlxSuite/codebase/dhtmlx.js"></script>
<script src="<%=CONTEXT_PATH%>resources/js/jquery.js"></script>

<style type="text/css">
table {
	border-collapse: collapse;
	border-spacing: 0;
}

fieldset, img {
	border: 0;
}

address, caption, cite, code, dfn, em, strong, th, var {
	font-style: normal;
	font-weight: normal;
}

h1 {
	font-size: 22px;
	color: #000;
}
/* ---------------------------------UP Reset------------------------------------------- */
body {
	font: 100% Arial, sans-serif;
	text-align: center;
}

verfiycode {
	width: 110px;
	height: 30px;
	text-align: left;
	overflow: hidden;
}

div#winVP {
	position: relative;
	height: 500px;
	border: 1px solid #dfdfdf;
	margin: 10px;
}
</style>


<script language="javascript" type="text/javascript">  
var msg="";
var ctx='<%=CONTEXT_PATH%>';
var dhxWins, w1, myForm,mySlider;
function openMsgDialog(title, msg, width, height, func) {
    dhtmlx.message({
        title : title,
        type : "alert-warning",
        text : msg,
        callback : function() {
            if (func != null)
                eval(func)
        }
    });
}
function checkInput(userName,password){
    var bValid = true;

    bValid = bValid & checkLength(userName,"<spring:message code="login.userName" />",4,30);
    bValid = bValid & checkRegexp(userName,/^[a-z]([0-9a-z_])+$/i,"<spring:message code="login.userNameCheck" />");
    bValid = bValid & checkLength(password,"<spring:message code="login.password" />",6,30);
    return bValid;
}

function goValidate(myForm){
    var sliderval=mySlider.getValue();
    var uName = document.getElementById("userName");
    var password =document.getElementById("password");
    if(checkInput(uName,password)){
        if(sliderval!=99){
            openMsgDialog("<spring:message code="login.failed" />","<spring:message code="login.dragScrollBar" />",300,200);
            return false;
        }
        login(myForm);
    }
    else {
        openMsgDialog("<spring:message code="login.failed" />","<spring:message code="message.errorMsg" />"+msg,300,200);
    }
}

function login(myForm){
    $.post(ctx+'user/login',
        {accountName:$("#userName").val(),password:$("#password").val()}
        ,function(retval){
            var retjson=eval(retval);
            if(retjson.success==true){
                window.location.href=ctx+'main/index';
            }else{
                openMsgDialog("<spring:message code="login.failed" />","<spring:message code="message.errorMsg" />"+retjson.message,300,200);
			}
        });
}
function showmsg(){
    var msg='<%=request.getAttribute("errMsg")%>';
    if(msg!='null'){
        openMsgDialog("<spring:message code="login.failed" />","<spring:message code="message.errorMsg" />"+msg,300,200);
    }
    doOnLoad();
}


function doOnLoad() {
    mySlider = new dhtmlXSlider("sliderbar", 150);
    mySlider.setSkin('dhx_terrace')
}

function checkLength(o,n,min,max) {
    if(o.value.length > max || o.value.length < min) {
        msg=msg+n+("<spring:message code="login.lengthCheck" />"+min+"<spring:message code="login.lengthCheckAnd" />"+max+"<spring:message code="login.lengthcheckEnd" />"+"\r\n");
        return false;
    }
    else {
        return true;
    }
}

function checkRegexp(o,regexp,n,dmesg) {
    if(regexp.test(o.value)) {
        return true;
    }
    else {
        msg=msg+n+dmesg+"\r\n";
        return false;
    }
}



</script>
</head>
<body onload="showmsg();">
	<div id="winVP" class=" dhxwins_vp_material" style="text-align: left !important;">
		<div class="dhxwin_active"
			style="z-index: 105; left: 315px; top: 156px; width: 700px; height: 300px;">
			<div class="dhxwin_hdr" style="z-index: 0;">
				<div class="dhxwin_icon"></div>
				<div class="dhxwin_text"
					style="padding-left: 19px; padding-right: 42px;">
					<div class="dhxwin_text_inside"><spring:message code="login.LoginSys" /></div>
				</div>
				<div class="dhxwin_btns"></div>
			</div>
			<div class="dhxwin_brd"
				style="left: 0px; top: 48px; width: 696px; height: 250px;"></div>
			<div class="dhx_cell_wins"
				style="left: 2px; top: 48px; width: 696px; height: 250px;">
				<div class="dhx_cell_cont_wins"
					style="left: 0px; top: 0px; width: 696px; height: 250px;">
					<div
						style="width: 100%; height: 100%; position: relative; overflow: auto; font-size: 14px;"
						class="dhxform_obj_material">
						<div class="dhxform_base">
							<div style="padding-left: 10px ! important;"
								class="dhxform_item_label_left">
								<div class="dhxform_txt_label2 topmost" style="width: 130px;"></div>
							</div>
							<div style="padding-left: 20px;" class="dhxform_base_nested">
								<div class="dhxform_base">
									<div
										style="padding-left: 10px ! important; padding-top: 10px ! important;"
										class="dhxform_item_label_left">
										<div class="dhxform_label dhxform_label_align_left"
											style="width: 130px;">
											<label for="dhxId_FcTANDRGB7zZ"><spring:message code="login.userName" /></label>
										</div>
										<div class="dhxform_control">
											<input type="text" class="dhxform_textarea" id="userName" value="admin"
												 style="width: 116px;">
										</div>
									</div>
								</div>
							</div>
							<div style="padding-left: 10px ! important;"
								class="dhxform_item_label_left">
								<div class="dhxform_txt_label2" style="width: 130px;"></div>
							</div>
							<div style="padding-left: 20px;" class="dhxform_base_nested">
								<div class="dhxform_base">
									<div style="padding-left: 10px ! important;"
										class="dhxform_item_label_left">
										<div class="dhxform_label dhxform_label_align_left"
											style="width: 130px;">
											<label for="dhxId_URg1VClk65Et"><spring:message code="login.password" /></label>
										</div>
										<div class="dhxform_control">
											<input type="password" class="dhxform_textarea" value="123456"
												id="password" style="width: 116px;">
										</div>
									</div>
								</div>
							</div>
							<div style="padding-left: 10px ! important;"
								class="dhxform_item_label_left">
								<div class="dhxform_txt_label2" style="width: 130px;"></div>
							</div>
							<div style="padding-left: 20px;" class="dhxform_base_nested">
								<div class="dhxform_base">
									<div style="padding-left: 10px ! important;"
										class="dhxform_item_label_left">
										<div class="dhxform_label dhxform_label_align_left"
											style="width: 130px;">
											<label for="dhxId_aeHjTnRsVFKb"><spring:message code="login.ScrollMsg" /></label>
										</div>
										
									</div>
								</div>
								<div class="dhxform_base">
									<div style="padding-left: 10px ! important;"
										class="dhxform_item_label_left">
										<div class="dhxform_label dhxform_label_align_left"
											style="display: none; width: 160px;">
											<label for="dhxId_nkE61Jk8BrwW"></label>
										</div>
										<div class="dhxform_control">
											<div class="dhxform_image" 
												style="width: 160px; height: 28px;">
												<div id="sliderbar" />
														
													</div>
											</div>
										</div>
									</div>
								</div>
							</div>
							<div style="padding-left: 10px ! important;"
								class="dhxform_item_label_left">
								<div class="dhxform_txt_label2" style="width: 130px;"></div>
							</div>
							<div style="padding-left: 20px;" class="dhxform_base_nested">
								<div class="dhxform_base">
									<div style="padding-left: 200px ! important;"
										class="dhxform_item_label_left">
										<div dir="ltr" tabindex="0" role="link" class="dhxform_btn" onclick="goValidate()">
											<div class="dhxform_btn_txt" ><spring:message code="btn.login" /></div>
											<div disabled="true" class="dhxform_btn_filler"></div>
										</div>
									</div>
								</div>
								<div class="dhxform_base" style="margin-left: 60px ! important;">
									<div style="padding-left: 10px ! important;"
										class="dhxform_item_label_left">
										<div dir="ltr" tabindex="0" role="link" class="dhxform_btn">
											<div class="dhxform_btn_txt"><spring:message code="btn.cancel" /></div>
											<div disabled="true" class="dhxform_btn_filler"></div>
										</div>
									</div>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
			<div class="dhxwin_fr_cover"
				style="left: 2px; top: 48px; width: 696px; height: 250px;">
				<iframe frameborder="0" border="0" class="dhxwin_fr_cover_inner"></iframe>
				<div class="dhxwin_fr_cover_inner"></div>
			</div>
		</div>

</body>
</html>