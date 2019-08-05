function checkLength(o,n,min,max) {
   if(o.value.length > max || o.value.length < min) {
	    msg=msg+n+("长度必须介于"+min+"和"+max+"之间。\r\n");
			return false;
	 } 
	 else {
      return true;
   }
}

function checkRegexp(o,regexp,n) {
   if(regexp.test(o.value)) {
      return true;
   } 
   else {
	    msg=msg+n+"\r\n";
      return false;
	 }
}
