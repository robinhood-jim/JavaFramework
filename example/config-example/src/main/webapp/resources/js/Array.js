$.extend(Array.prototype, {
	element : new Array(),
	length : 0,
	append : function(obj, nodup) {
		if (!(nodup && this.contains(obj))) {
			this[this.length] = obj;
		}
	},
	indexOf : function(obj) {
		var result = -1;
		for (var i = 0; i < this.length; i++) {
			if (this[i] == obj) {
				result = i;
				break;
			}
		}
		return result;
	},
	remove : function(obj) {
		var pos = this.indexOf(obj);
		if (pos != -1) {
			element.remove
		}
	},
	removeAt : function(index) {
		this.splice(index, 1);
	},
	removeByKeyValue : function(key,value) {
		var index=this.getKeyPos(key,value);
		if(index!=-1)
			this.splice(index, 1);
	},
	insertAt : function(index, obj) {
		this.splice(index, 0, obj);
	},
	clear : function() {
		this.length = 0;
	},
	remove : function(obj) {
		var index = this.indexOf(obj);
		if (index >= 0)
			this.removeAt(index);
	},
	toString : function(obj) {
		var len = this.length;
		var retStr = '';
		for (var i = 0; i < this.length; i++) {
			if (i != this.length - 1) {
				retStr += this[i] + ",";
			} else
				retStr += this[i];
		}
		return retStr;
	},getKeyObject:function(inputKey,keyval){
		var result;
		 var pos=this.getKeyPos(inputKey,keyval);
		 if(pos!=-1)
		 	result=this[pos];
		 return result;
	},
	getKeyValue:function(inputKey,keyval,targetKey){
		 var result="";
		 var pos=this.getKeyPos(inputKey,keyval);
		 if(pos!=-1)
		 	result=eval("this["+pos+"]."+targetKey);
		 return result;
	},getKeyPos:function(inputKey,value){
		var result=-1;
		for(var i=0;i<this.length;i++){
			var id=this[i].id;
			var val=eval("this["+i+"]."+inputKey);
			if(val!=undefined && val==value)
			{
				result=i;
			}
		}
		 return result;
	},updateKey:function(key,keyval,targetKey,value){
		var pos=this.getKeyPos(key,keyval);
		if(pos!=-1){
			eval("this["+pos+"]."+targetKey+"="+value);
		}
	},putValue:function(key,value){
		this[key]=value;
	}

});