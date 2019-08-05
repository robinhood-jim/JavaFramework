var dhxWins = new dhtmlXWindows();

var winName = "win";
var editMode;

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