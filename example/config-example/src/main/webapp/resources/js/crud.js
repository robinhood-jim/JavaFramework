var dhxWins = new dhtmlXWindows();

var winName = "win";
var editMode;
dhtmlx.message.position="bottom";

function openWindow(title, urlink, w, h) {
	var w = dhxWins.createWindow(winName, 0, 0, w, h);
	w.setText(title);
	w.keepInViewport(true);
	w.setModal(true);
	w.centerOnScreen();
	w.button("minmax1").hide();
	w.button("minmax2").hide();
	w.button("park").hide();
	w.attachURL(urlink);
	w.denyResize();
    w.denyMove();
	return w;
}
function openWindowForAdd(title, formStructure, w, h, func) {
	var w = dhxWins.createWindow(winName, 0, 0, w, h);
	w.setText(title);
	w.keepInViewport(true);
	w.setModal(true);
	w.centerOnScreen();
	w.button("minmax1").hide();
	w.button("minmax2").hide();
	w.button("park").hide();
	w.denyResize();
    w.denyMove();
	var form = w.attachForm();
	form.loadStruct(formStructure, "json");
	form.enableLiveValidation(true);
	if (func != undefined)
		eval(func(form));
	return form;
}
function openWindowForEdit(title, formStructure, w, h, func) {
	var w = dhxWins.createWindow(winName, 0, 0, w, h);
	w.setText(title);
	w.keepInViewport(true);
	w.setModal(true);
	w.centerOnScreen();
	w.button("minmax1").hide();
	w.button("minmax2").hide();
	w.button("park").hide();
	w.denyResize();
    w.denyMove();
	var form = w.attachForm();
	form.loadStruct(formStructure, "json");
	form.enableLiveValidation(true);
	if (func != undefined)
		eval(func(form));
	return form;
}

function closedialog(ret) {
	dhxWins.window(winName).close();
	editMode = "";
}

function openMsgDialog(title, msg, func) {
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
function goFristPage() {
	var pageCount = myForm.getItemValue("query.pageCount");
	if (pageCount == 0) {
		return;
	}
	myForm.setItemValue("query.pageNumber", "1");
	gosearch();
}
function goPreviousPage() {
	var i = 0;
	var rc = myForm.getItemValue("query.recordCount");
	if (rc == 0)
		return;
	var pn = myForm.getItemValue("query.pageNumber");
	i = pn;
	i--;
	myForm.setItemValue("query.pageNumber", i)
	gosearch();
}
function goLastPage() {
	var i = 0;
	var rc = myForm.getItemValue("query.recordCount");
	if (rc == 0)
		return;
	var lp = myForm.getItemValue("query.pageCount");
	var pn = myForm.getItemValue("query.pageNumber");
	myForm.setItemValue("query.pageNumber", lp)
	gosearch();
}
function goNextPage() {
	var i = 0;
	var rc = myForm.getItemValue("query.recordCount");
	if (rc == 0)
		return;
	var pn = myForm.getItemValue("query.pageNumber");
	i = pn;
	i++;
	myForm.setItemValue("query.pageNumber", i)
	gosearch();
}
function goPage() {
	var i = 0;
	myForm.setItemValue("query.pageNumber", $("#jumpNum").val())
	gosearch();
}
function setpagesize() {
	var evt = window.event || arguments.callee.caller.arguments[0];
	var ikeyCode = evt.keyCode || evt.which;
	if (ikeyCode == 13) {
		var pageSize = $("#pageSize").val();
		myForm.setItemValue("query.pageSize", pageSize);
		myForm.setItemValue("query.pageNumber", "1");
		gosearch();
	}
}
function gosearch() {
	reloadGrid();
}
function reload() {
    reloadGrid();
}
function reloadGrid() {
	myForm.send(queryUrl, function(loader, response) {
		var tobj = eval('(' + response + ')');
		myGrid.clearAll();
		myGrid.parse(tobj, "json");
		var barstr = tobj.query.pageToolBar;
		statusbar.setText(barstr);
	});

}
function initCombo(combo,url){
	if(combo!=undefined){
		$.post(url,function(retval){
			combo.load(retval);
		 });
	}
}
function openWindow(title,urlink,w,h){
    var w = dhxWins.createWindow(winName, 0, 0, w, h);
    w.setText(title);
    w.keepInViewport(true);
    w.setModal(true);
    w.centerOnScreen();
    w.button("minmax1").hide();
    w.button("minmax2").hide();
    w.button("park").hide();
    w.attachURL(urlink);
    w.denyResize();
    w.denyMove();
    return w;
}
function openWindowInPage(title,w,h,objname){
    var w = dhxWins.createWindow(winName, 0, 0, w, h);
    w.setText(title);
    w.keepInViewport(true);
    w.setModal(true);
    w.centerOnScreen();
    w.button("minmax1").hide();
    w.button("minmax2").hide();
    w.button("park").hide();
    w.attachObject(objname);
    w.button("close").disable();
    w.denyResize();
    w.denyMove();
    return w;
}
function openSingleWindow(title,divhtml,width,height){
    if(height==null || height=='')
        height=100;
    if(width==null || width=='')
        width=300;
    var win2 = dhxWins.createWindow(winName, 0, 0, width, height);
    win2.setText(title);
    win2.keepInViewport(true);
    win2.setModal(true);
    win2.centerOnScreen();
    win2.button("minmax1").hide();
    win2.button("minmax2").hide();
    win2.button("park").hide();
    win2.attachHTMLString(divhtml);
    win2.denyResize();
    win2.denyMove();
    return win2;
}
function openMsg(title,message){
    dhtmlx.alert({
        title:title,
        ok:"OK",
        text:message
    });
}
function openAlert(title,message){
    dhtmlx.alert({
        title:title,
        type:"alert-warning",
        ok:"OK",
        text:message
    });
}
function openConfrim(title,message,fun) {
    dhtmlx.confirm({
        title:title,
        type:"confirm",
        text: message,
        callback: function(result){
            if(result==true){
                var fun1=eval(fun);
                fun1.apply();
                return true;
            } else
                return false;
        }
    });
}

function openMsgPopUp(title,msg,width,height,func){
    dhtmlx.message({
        title: title,
        type: "alert-warning",
        text: msg,
        callback: function() {if(func!=null)eval(func)}
    });
}