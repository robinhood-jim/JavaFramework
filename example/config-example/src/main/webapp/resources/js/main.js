
var arr=new Array();

tabbar.cells("main").setActive();
tabbar.attachEvent("onTabClose",function(id,pid){
  try{
     if(id=='main'){
     	alert('主页面不能关闭');
     	return false;
     }
     tabbar.tabs(id).close();
     arr.remove(id);
	  return true;
  }catch(e){
  }
});
function addTab(url,tabTitle,tabName,width){
		if(width=='')
			width='100px';
		if(arr.indexOf(tabName)!=-1)
			tabbar.cells(tabName).setActive();
		else{
			url=contextpath+url;
			arr.append(tabName);
			tabbar.addTab(tabName,tabTitle,width);
			tabbar.cells(tabName).attachURL(url);	
			tabbar.tabs(tabName).setActive();
		}
}

function showmsg(mesg){
		$.messager.show({
			title:'操作结果',
			msg: mesg,
			timeout:5000,
			showType:'slide'
		});
}

function closedialog(ret) {
	dhxWins.window("win").close();
		editMode="";
	if(ret=='true') {
		gosearch();
	}	
}
