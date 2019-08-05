
	 var dhxWins = new dhtmlXWindows();
    //dhxWins.enableAutoViewport(true);
    //dhxWins.setImagePath(contextpath+"dhtmlxSuite/dhtmlxWindows/codebase/imgs/");
    
    var winName="win";  
    var editMode;       
    dhtmlx.message.position="bottom";
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