/* Copyright (c) 2006, Yahoo! Inc. All rights reserved.Code licensed under the BSD License:http://developer.yahoo.net/yui/license.txtversion: 0.12.0 */ if(typeof YAHOO=="undefined"){var YAHOO={};}YAHOO.namespace=function(){var a=arguments,o=null,i,j,d;for(i=0;i<a.length;++i){d=a[i].split(".");o=YAHOO;for(j=(d[0]=="YAHOO")?1:0;j<d.length;++j){o[d[j]]=o[d[j]]||{};o=o[d[j]];}}return o;};YAHOO.log=function(_2,_3,_4){var l=YAHOO.widget.Logger;if(l&&l.log){return l.log(_2,_3,_4);}else{return false;}};YAHOO.extend=function(_6,_7,_8){var F=function(){};F.prototype=_7.prototype;_6.prototype=new F();_6.prototype.constructor=_6;_6.superclass=_7.prototype;if(_7.prototype.constructor==Object.prototype.constructor){_7.prototype.constructor=_7;}if(_8){for(var i in _8){_6.prototype[i]=_8[i];}}};YAHOO.augment=function(r,s){var rp=r.prototype,sp=s.prototype,a=arguments,i,p;if(a[2]){for(i=2;i<a.length;++i){rp[a[i]]=sp[a[i]];}}else{for(p in sp){if(!rp[p]){rp[p]=sp[p];}}}};YAHOO.namespace("util","widget","example");(function(){var Y=YAHOO.util,getStyle,setStyle,id_counter=0,propertyCache={};var ua=navigator.userAgent.toLowerCase(),isOpera=(ua.indexOf('opera')>-1),isSafari=(ua.indexOf('safari')>-1),isGecko=(!isOpera&&!isSafari&&ua.indexOf('gecko')>-1),isIE=(!isOpera&&ua.indexOf('msie')>-1);var patterns={HYPHEN:/(-[a-z])/i};var toCamel=function(property){if(!patterns.HYPHEN.test(property)){return property;}if(propertyCache[property]){return propertyCache[property];}while(patterns.HYPHEN.exec(property)){property=property.replace(RegExp.$1,RegExp.$1.substr(1).toUpperCase());}propertyCache[property]=property;return property;};if(document.defaultView&&document.defaultView.getComputedStyle){getStyle=function(el,property){var value=null;var computed=document.defaultView.getComputedStyle(el,'');if(computed){value=computed[toCamel(property)];}return el.style[property]||value;};}else if(document.documentElement.currentStyle&&isIE){getStyle=function(el,property){switch(toCamel(property)){case'opacity':var val=100;try{val=el.filters['DXImageTransform.Microsoft.Alpha'].opacity;}catch(e){try{val=el.filters('alpha').opacity;}catch(e){}}return val/100;break;default:var value=el.currentStyle?el.currentStyle[property]:null;return(el.style[property]||value);}};}else{getStyle=function(el,property){return el.style[property];};}if(isIE){setStyle=function(el,property,val){switch(property){case'opacity':if(typeof el.style.filter=='string'){el.style.filter='alpha(opacity='+val*100+')';if(!el.currentStyle||!el.currentStyle.hasLayout){el.style.zoom=1;}}break;default:el.style[property]=val;}};}else{setStyle=function(el,property,val){el.style[property]=val;};}YAHOO.util.Dom={get:function(el){if(!el){return null;}if(typeof el!='string'&&!(el instanceof Array)){return el;}if(typeof el=='string'){return document.getElementById(el);}else{var collection=[];for(var i=0,len=el.length;i<len;++i){collection[collection.length]=Y.Dom.get(el[i]);}return collection;}return null;},getStyle:function(el,property){property=toCamel(property);var f=function(element){return getStyle(element,property);};return Y.Dom.batch(el,f,Y.Dom,true);},setStyle:function(el,property,val){property=toCamel(property);var f=function(element){setStyle(element,property,val);};Y.Dom.batch(el,f,Y.Dom,true);},getXY:function(el){var f=function(el){if(el.parentNode===null||el.offsetParent===null||this.getStyle(el,'display')=='none'){return false;}var parentNode=null;var pos=[];var box;if(el.getBoundingClientRect){box=el.getBoundingClientRect();var doc=document;if(!this.inDocument(el)&&parent.document!=document){doc=parent.document;if(!this.isAncestor(doc.documentElement,el)){return false;}}var scrollTop=Math.max(doc.documentElement.scrollTop,doc.body.scrollTop);var scrollLeft=Math.max(doc.documentElement.scrollLeft,doc.body.scrollLeft);return[box.left+scrollLeft,box.top+scrollTop];}else{pos=[el.offsetLeft,el.offsetTop];parentNode=el.offsetParent;if(parentNode!=el){while(parentNode){pos[0]+=parentNode.offsetLeft;pos[1]+=parentNode.offsetTop;parentNode=parentNode.offsetParent;}}if(isSafari&&this.getStyle(el,'position')=='absolute'){pos[0]-=document.body.offsetLeft;pos[1]-=document.body.offsetTop;}}if(el.parentNode){parentNode=el.parentNode;}else{parentNode=null;}while(parentNode&&parentNode.tagName.toUpperCase()!='BODY'&&parentNode.tagName.toUpperCase()!='HTML'){if(Y.Dom.getStyle(parentNode,'display')!='inline'){pos[0]-=parentNode.scrollLeft;pos[1]-=parentNode.scrollTop;}if(parentNode.parentNode){parentNode=parentNode.parentNode;}else{parentNode=null;}}return pos;};return Y.Dom.batch(el,f,Y.Dom,true);},getX:function(el){var f=function(el){return Y.Dom.getXY(el)[0];};return Y.Dom.batch(el,f,Y.Dom,true);},getY:function(el){var f=function(el){return Y.Dom.getXY(el)[1];};return Y.Dom.batch(el,f,Y.Dom,true);},setXY:function(el,pos,noRetry){var f=function(el){var style_pos=this.getStyle(el,'position');if(style_pos=='static'){this.setStyle(el,'position','relative');style_pos='relative';}var pageXY=this.getXY(el);if(pageXY===false){return false;}var delta=[parseInt(this.getStyle(el,'left'),10),parseInt(this.getStyle(el,'top'),10)];if(isNaN(delta[0])){delta[0]=(style_pos=='relative')?0:el.offsetLeft;}if(isNaN(delta[1])){delta[1]=(style_pos=='relative')?0:el.offsetTop;}if(pos[0]!==null){el.style.left=pos[0]-pageXY[0]+delta[0]+'px';}if(pos[1]!==null){el.style.top=pos[1]-pageXY[1]+delta[1]+'px';}var newXY=this.getXY(el);if(!noRetry&&(newXY[0]!=pos[0]||newXY[1]!=pos[1])){this.setXY(el,pos,true);}};Y.Dom.batch(el,f,Y.Dom,true);},setX:function(el,x){Y.Dom.setXY(el,[x,null]);},setY:function(el,y){Y.Dom.setXY(el,[null,y]);},getRegion:function(el){var f=function(el){var region=new Y.Region.getRegion(el);return region;};return Y.Dom.batch(el,f,Y.Dom,true);},getClientWidth:function(){return Y.Dom.getViewportWidth();},getClientHeight:function(){return Y.Dom.getViewportHeight();},getElementsByClassName:function(className,tag,root){var method=function(el){return Y.Dom.hasClass(el,className);};return Y.Dom.getElementsBy(method,tag,root);},hasClass:function(el,className){var re=new RegExp('(?:^|\\s+)'+className+'(?:\\s+|$)');var f=function(el){return re.test(el['className']);};return Y.Dom.batch(el,f,Y.Dom,true);},addClass:function(el,className){var f=function(el){if(this.hasClass(el,className)){return;}el['className']=[el['className'],className].join(' ');};Y.Dom.batch(el,f,Y.Dom,true);},removeClass:function(el,className){var re=new RegExp('(?:^|\\s+)'+className+'(?:\\s+|$)','g');var f=function(el){if(!this.hasClass(el,className)){return;}var c=el['className'];el['className']=c.replace(re,' ');if(this.hasClass(el,className)){this.removeClass(el,className);}};Y.Dom.batch(el,f,Y.Dom,true);},replaceClass:function(el,oldClassName,newClassName){if(oldClassName===newClassName){return false;}var re=new RegExp('(?:^|\\s+)'+oldClassName+'(?:\\s+|$)','g');var f=function(el){if(!this.hasClass(el,oldClassName)){this.addClass(el,newClassName);return;}el['className']=el['className'].replace(re,' '+newClassName+' ');if(this.hasClass(el,oldClassName)){this.replaceClass(el,oldClassName,newClassName);}};Y.Dom.batch(el,f,Y.Dom,true);},generateId:function(el,prefix){prefix=prefix||'yui-gen';el=el||{};var f=function(el){if(el){el=Y.Dom.get(el);}else{el={};}if(!el.id){el.id=prefix+id_counter++;}return el.id;};return Y.Dom.batch(el,f,Y.Dom,true);},isAncestor:function(haystack,needle){haystack=Y.Dom.get(haystack);if(!haystack||!needle){return false;}var f=function(needle){if(haystack.contains&&!isSafari){return haystack.contains(needle);}else if(haystack.compareDocumentPosition){return!!(haystack.compareDocumentPosition(needle)&16);}else{var parent=needle.parentNode;while(parent){if(parent==haystack){return true;}else if(!parent.tagName||parent.tagName.toUpperCase()=='HTML'){return false;}parent=parent.parentNode;}return false;}};return Y.Dom.batch(needle,f,Y.Dom,true);},inDocument:function(el){var f=function(el){return this.isAncestor(document.documentElement,el);};return Y.Dom.batch(el,f,Y.Dom,true);},getElementsBy:function(method,tag,root){tag=tag||'*';root=Y.Dom.get(root)||document;var nodes=[];var elements=root.getElementsByTagName(tag);if(!elements.length&&(tag=='*'&&root.all)){elements=root.all;}for(var i=0,len=elements.length;i<len;++i){if(method(elements[i])){nodes[nodes.length]=elements[i];}}return nodes;},batch:function(el,method,o,override){var id=el;el=Y.Dom.get(el);var scope=(override)?o:window;if(!el||el.tagName||!el.length){if(!el){return false;}return method.call(scope,el,o);}var collection=[];for(var i=0,len=el.length;i<len;++i){if(!el[i]){id=el[i];}collection[collection.length]=method.call(scope,el[i],o);}return collection;},getDocumentHeight:function(){var scrollHeight=(document.compatMode!='CSS1Compat')?document.body.scrollHeight:document.documentElement.scrollHeight;var h=Math.max(scrollHeight,Y.Dom.getViewportHeight());return h;},getDocumentWidth:function(){var scrollWidth=(document.compatMode!='CSS1Compat')?document.body.scrollWidth:document.documentElement.scrollWidth;var w=Math.max(scrollWidth,Y.Dom.getViewportWidth());return w;},getViewportHeight:function(){var height=self.innerHeight;var mode=document.compatMode;if((mode||isIE)&&!isOpera){height=(mode=='CSS1Compat')?document.documentElement.clientHeight:document.body.clientHeight;}return height;},getViewportWidth:function(){var width=self.innerWidth;var mode=document.compatMode;if(mode||isIE){width=(mode=='CSS1Compat')?document.documentElement.clientWidth:document.body.clientWidth;}return width;}};})();YAHOO.util.Region=function(t,r,b,l){this.top=t;this[1]=t;this.right=r;this.bottom=b;this.left=l;this[0]=l;};YAHOO.util.Region.prototype.contains=function(region){return(region.left>=this.left&&region.right<=this.right&&region.top>=this.top&&region.bottom<=this.bottom);};YAHOO.util.Region.prototype.getArea=function(){return((this.bottom-this.top)*(this.right-this.left));};YAHOO.util.Region.prototype.intersect=function(region){var t=Math.max(this.top,region.top);var r=Math.min(this.right,region.right);var b=Math.min(this.bottom,region.bottom);var l=Math.max(this.left,region.left);if(b>=t&&r>=l){return new YAHOO.util.Region(t,r,b,l);}else{return null;}};YAHOO.util.Region.prototype.union=function(region){var t=Math.min(this.top,region.top);var r=Math.max(this.right,region.right);var b=Math.max(this.bottom,region.bottom);var l=Math.min(this.left,region.left);return new YAHOO.util.Region(t,r,b,l);};YAHOO.util.Region.prototype.toString=function(){return("Region {"+"top: "+this.top+", right: "+this.right+", bottom: "+this.bottom+", left: "+this.left+"}");};YAHOO.util.Region.getRegion=function(el){var p=YAHOO.util.Dom.getXY(el);var t=p[1];var r=p[0]+el.offsetWidth;var b=p[1]+el.offsetHeight;var l=p[0];return new YAHOO.util.Region(t,r,b,l);};YAHOO.util.Point=function(x,y){if(x instanceof Array){y=x[1];x=x[0];}this.x=this.right=this.left=this[0]=x;this.y=this.top=this.bottom=this[1]=y;};YAHOO.util.Point.prototype=new YAHOO.util.Region();YAHOO.util.CustomEvent=function(_1,_2,_3,_4){this.type=_1;this.scope=_2||window;this.silent=_3;this.signature=_4||YAHOO.util.CustomEvent.LIST;this.subscribers=[];if(!this.silent){}var _5="_YUICEOnSubscribe";if(_1!==_5){this.subscribeEvent=new YAHOO.util.CustomEvent(_5,this,true);}};YAHOO.util.CustomEvent.LIST=0;YAHOO.util.CustomEvent.FLAT=1;YAHOO.util.CustomEvent.prototype={subscribe:function(fn,_7,_8){if(this.subscribeEvent){this.subscribeEvent.fire(fn,_7,_8);}this.subscribers.push(new YAHOO.util.Subscriber(fn,_7,_8));},unsubscribe:function(fn,_9){var _10=false;for(var i=0,len=this.subscribers.length;i<len;++i){var s=this.subscribers[i];if(s&&s.contains(fn,_9)){this._delete(i);_10=true;}}return _10;},fire:function(){var len=this.subscribers.length;if(!len&&this.silent){return true;}var _14=[],ret=true,i;for(i=0;i<arguments.length;++i){_14.push(arguments[i]);}var _15=_14.length;if(!this.silent){}for(i=0;i<len;++i){var s=this.subscribers[i];if(s){if(!this.silent){}var _16=s.getScope(this.scope);if(this.signature==YAHOO.util.CustomEvent.FLAT){var _17=null;if(_14.length>0){_17=_14[0];}ret=s.fn.call(_16,_17,s.obj);}else{ret=s.fn.call(_16,this.type,_14,s.obj);}if(false===ret){if(!this.silent){}return false;}}}return true;},unsubscribeAll:function(){for(var i=0,len=this.subscribers.length;i<len;++i){this._delete(len-1-i);}},_delete:function(_18){var s=this.subscribers[_18];if(s){delete s.fn;delete s.obj;}this.subscribers.splice(_18,1);},toString:function(){return "CustomEvent: "+"'"+this.type+"', "+"scope: "+this.scope;}};YAHOO.util.Subscriber=function(fn,obj,_20){this.fn=fn;this.obj=obj||null;this.override=_20;};YAHOO.util.Subscriber.prototype.getScope=function(_21){if(this.override){if(this.override===true){return this.obj;}else{return this.override;}}return _21;};YAHOO.util.Subscriber.prototype.contains=function(fn,obj){if(obj){return (this.fn==fn&&this.obj==obj);}else{return (this.fn==fn);}};YAHOO.util.Subscriber.prototype.toString=function(){return "Subscriber { obj: "+(this.obj||"")+", override: "+(this.override||"no")+" }";};if(!YAHOO.util.Event){YAHOO.util.Event=function(){var _22=false;var _23=[];var _24=[];var _25=[];var _26=[];var _27=0;var _28=[];var _29=[];var _30=0;return {POLL_RETRYS:200,POLL_INTERVAL:20,EL:0,TYPE:1,FN:2,WFN:3,OBJ:3,ADJ_SCOPE:4,isSafari:(/Safari|Konqueror|KHTML/gi).test(navigator.userAgent),isIE:(!this.isSafari&&!navigator.userAgent.match(/opera/gi)&&navigator.userAgent.match(/msie/gi)),_interval:null,startInterval:function(){if(!this._interval){var _31=this;var _32=function(){_31._tryPreloadAttach();};this._interval=setInterval(_32,this.POLL_INTERVAL);}},onAvailable:function(_33,_34,_35,_36){_28.push({id:_33,fn:_34,obj:_35,override:_36,checkReady:false});_27=this.POLL_RETRYS;this.startInterval();},onContentReady:function(_37,_38,_39,_40){_28.push({id:_37,fn:_38,obj:_39,override:_40,checkReady:true});_27=this.POLL_RETRYS;this.startInterval();},addListener:function(el,_42,fn,obj,_43){if(!fn||!fn.call){return false;}if(this._isValidCollection(el)){var ok=true;for(var i=0,len=el.length;i<len;++i){ok=this.on(el[i],_42,fn,obj,_43)&&ok;}return ok;}else{if(typeof el=="string"){var oEl=this.getEl(el);if(oEl){el=oEl;}else{this.onAvailable(el,function(){YAHOO.util.Event.on(el,_42,fn,obj,_43);});return true;}}}if(!el){return false;}if("unload"==_42&&obj!==this){_24[_24.length]=[el,_42,fn,obj,_43];return true;}var _46=el;if(_43){if(_43===true){_46=obj;}else{_46=_43;}}var _47=function(e){return fn.call(_46,YAHOO.util.Event.getEvent(e),obj);};var li=[el,_42,fn,_47,_46];var _50=_23.length;_23[_50]=li;if(this.useLegacyEvent(el,_42)){var _51=this.getLegacyIndex(el,_42);if(_51==-1||el!=_25[_51][0]){_51=_25.length;_29[el.id+_42]=_51;_25[_51]=[el,_42,el["on"+_42]];_26[_51]=[];el["on"+_42]=function(e){YAHOO.util.Event.fireLegacyEvent(YAHOO.util.Event.getEvent(e),_51);};}_26[_51].push(li);}else{this._simpleAdd(el,_42,_47,false);}return true;},fireLegacyEvent:function(e,_52){var ok=true;var le=_26[_52];for(var i=0,len=le.length;i<len;++i){var li=le[i];if(li&&li[this.WFN]){var _54=li[this.ADJ_SCOPE];var ret=li[this.WFN].call(_54,e);ok=(ok&&ret);}}return ok;},getLegacyIndex:function(el,_56){var key=this.generateId(el)+_56;if(typeof _29[key]=="undefined"){return -1;}else{return _29[key];}},useLegacyEvent:function(el,_58){if(!el.addEventListener&&!el.attachEvent){return true;}else{if(this.isSafari){if("click"==_58||"dblclick"==_58){return true;}}}return false;},removeListener:function(el,_59,fn){var i,len;if(typeof el=="string"){el=this.getEl(el);}else{if(this._isValidCollection(el)){var ok=true;for(i=0,len=el.length;i<len;++i){ok=(this.removeListener(el[i],_59,fn)&&ok);}return ok;}}if(!fn||!fn.call){return this.purgeElement(el,false,_59);}if("unload"==_59){for(i=0,len=_24.length;i<len;i++){var li=_24[i];if(li&&li[0]==el&&li[1]==_59&&li[2]==fn){_24.splice(i,1);return true;}}return false;}var _60=null;var _61=arguments[3];if("undefined"==typeof _61){_61=this._getCacheIndex(el,_59,fn);}if(_61>=0){_60=_23[_61];}if(!el||!_60){return false;}if(this.useLegacyEvent(el,_59)){var _62=this.getLegacyIndex(el,_59);var _63=_26[_62];if(_63){for(i=0,len=_63.length;i<len;++i){li=_63[i];if(li&&li[this.EL]==el&&li[this.TYPE]==_59&&li[this.FN]==fn){_63.splice(i,1);}}}}else{this._simpleRemove(el,_59,_60[this.WFN],false);}delete _23[_61][this.WFN];delete _23[_61][this.FN];_23.splice(_61,1);return true;},getTarget:function(ev,_65){var t=ev.target||ev.srcElement;return this.resolveTextNode(t);},resolveTextNode:function(_67){if(_67&&3==_67.nodeType){return _67.parentNode;}else{return _67;}},getPageX:function(ev){var x=ev.pageX;if(!x&&0!==x){x=ev.clientX||0;if(this.isIE){x+=this._getScrollLeft();}}return x;},getPageY:function(ev){var y=ev.pageY;if(!y&&0!==y){y=ev.clientY||0;if(this.isIE){y+=this._getScrollTop();}}return y;},getXY:function(ev){return [this.getPageX(ev),this.getPageY(ev)];},getRelatedTarget:function(ev){var t=ev.relatedTarget;if(!t){if(ev.type=="mouseout"){t=ev.toElement;}else{if(ev.type=="mouseover"){t=ev.fromElement;}}}return this.resolveTextNode(t);},getTime:function(ev){if(!ev.time){var t=new Date().getTime();try{ev.time=t;}catch(e){return t;}}return ev.time;},stopEvent:function(ev){this.stopPropagation(ev);this.preventDefault(ev);},stopPropagation:function(ev){if(ev.stopPropagation){ev.stopPropagation();}else{ev.cancelBubble=true;}},preventDefault:function(ev){if(ev.preventDefault){ev.preventDefault();}else{ev.returnValue=false;}},getEvent:function(e){var ev=e||window.event;if(!ev){var c=this.getEvent.caller;while(c){ev=c.arguments[0];if(ev&&Event==ev.constructor){break;}c=c.caller;}}return ev;},getCharCode:function(ev){return ev.charCode||ev.keyCode||0;},_getCacheIndex:function(el,_71,fn){for(var i=0,len=_23.length;i<len;++i){var li=_23[i];if(li&&li[this.FN]==fn&&li[this.EL]==el&&li[this.TYPE]==_71){return i;}}return -1;},generateId:function(el){var id=el.id;if(!id){id="yuievtautoid-"+_30;++_30;el.id=id;}return id;},_isValidCollection:function(o){return (o&&o.length&&typeof o!="string"&&!o.tagName&&!o.alert&&typeof o[0]!="undefined");},elCache:{},getEl:function(id){return document.getElementById(id);},clearCache:function(){},_load:function(e){_22=true;var EU=YAHOO.util.Event;if(this.isIE){EU._simpleRemove(window,"load",EU._load);}},_tryPreloadAttach:function(){if(this.locked){return false;}this.locked=true;var _75=!_22;if(!_75){_75=(_27>0);}var _76=[];for(var i=0,len=_28.length;i<len;++i){var _77=_28[i];if(_77){var el=this.getEl(_77.id);if(el){if(!_77.checkReady||_22||el.nextSibling||(document&&document.body)){var _78=el;if(_77.override){if(_77.override===true){_78=_77.obj;}else{_78=_77.override;}}_77.fn.call(_78,_77.obj);delete _28[i];}}else{_76.push(_77);}}}_27=(_76.length===0)?0:_27-1;if(_75){this.startInterval();}else{clearInterval(this._interval);this._interval=null;}this.locked=false;return true;},purgeElement:function(el,_79,_80){var _81=this.getListeners(el,_80);if(_81){for(var i=0,len=_81.length;i<len;++i){var l=_81[i];this.removeListener(el,l.type,l.fn);}}if(_79&&el&&el.childNodes){for(i=0,len=el.childNodes.length;i<len;++i){this.purgeElement(el.childNodes[i],_79,_80);}}},getListeners:function(el,_83){var _84=[];if(_23&&_23.length>0){for(var i=0,len=_23.length;i<len;++i){var l=_23[i];if(l&&l[this.EL]===el&&(!_83||_83===l[this.TYPE])){_84.push({type:l[this.TYPE],fn:l[this.FN],obj:l[this.OBJ],adjust:l[this.ADJ_SCOPE],index:i});}}}return (_84.length)?_84:null;},_unload:function(e){var EU=YAHOO.util.Event,i,j,l,len,index;for(i=0,len=_24.length;i<len;++i){l=_24[i];if(l){var _85=window;if(l[EU.ADJ_SCOPE]){if(l[EU.ADJ_SCOPE]===true){_85=l[EU.OBJ];}else{_85=l[EU.ADJ_SCOPE];}}l[EU.FN].call(_85,EU.getEvent(e),l[EU.OBJ]);delete _24[i];l=null;_85=null;}}if(_23&&_23.length>0){j=_23.length;while(j){index=j-1;l=_23[index];if(l){EU.removeListener(l[EU.EL],l[EU.TYPE],l[EU.FN],index);}j=j-1;}l=null;EU.clearCache();}for(i=0,len=_25.length;i<len;++i){delete _25[i][0];delete _25[i];}EU._simpleRemove(window,"unload",EU._unload);},_getScrollLeft:function(){return this._getScroll()[1];},_getScrollTop:function(){return this._getScroll()[0];},_getScroll:function(){var dd=document.documentElement,db=document.body;if(dd&&(dd.scrollTop||dd.scrollLeft)){return [dd.scrollTop,dd.scrollLeft];}else{if(db){return [db.scrollTop,db.scrollLeft];}else{return [0,0];}}},_simpleAdd:function(){if(window.addEventListener){return function(el,_87,fn,_88){el.addEventListener(_87,fn,(_88));};}else{if(window.attachEvent){return function(el,_89,fn,_90){el.attachEvent("on"+_89,fn);};}else{return function(){};}}}(),_simpleRemove:function(){if(window.removeEventListener){return function(el,_91,fn,_92){el.removeEventListener(_91,fn,(_92));};}else{if(window.detachEvent){return function(el,_93,fn){el.detachEvent("on"+_93,fn);};}else{return function(){};}}}()};}();(function(){var EU=YAHOO.util.Event;EU.on=EU.addListener;if(document&&document.body){EU._load();}else{EU._simpleAdd(window,"load",EU._load);}EU._simpleAdd(window,"unload",EU._unload);EU._tryPreloadAttach();})();}YAHOO.util.EventProvider=function(){};YAHOO.util.EventProvider.prototype={__yui_events:null,__yui_subscribers:null,subscribe:function(_94,_95,_96,_97){this.__yui_events=this.__yui_events||{};var ce=this.__yui_events[_94];if(ce){ce.subscribe(_95,_96,_97);}else{this.__yui_subscribers=this.__yui_subscribers||{};var _99=this.__yui_subscribers;if(!_99[_94]){_99[_94]=[];}_99[_94].push({fn:_95,obj:_96,override:_97});}},unsubscribe:function(_100,p_fn,_102){this.__yui_events=this.__yui_events||{};var ce=this.__yui_events[_100];if(ce){return ce.unsubscribe(p_fn,_102);}else{return false;}},createEvent:function(_103,_104){this.__yui_events=this.__yui_events||{};var opts=_104||{};var _106=this.__yui_events;if(_106[_103]){}else{var _107=opts.scope||this;var _108=opts.silent||null;var ce=new YAHOO.util.CustomEvent(_103,_107,_108,YAHOO.util.CustomEvent.FLAT);_106[_103]=ce;if(opts.onSubscribeCallback){ce.subscribeEvent.subscribe(opts.onSubscribeCallback);}this.__yui_subscribers=this.__yui_subscribers||{};var qs=this.__yui_subscribers[_103];if(qs){for(var i=0;i<qs.length;++i){ce.subscribe(qs[i].fn,qs[i].obj,qs[i].override);}}}return _106[_103];},fireEvent:function(_110,arg1,arg2,etc){this.__yui_events=this.__yui_events||{};var ce=this.__yui_events[_110];if(ce){var args=[];for(var i=1;i<arguments.length;++i){args.push(arguments[i]);}return ce.fire.apply(ce,args);}else{return null;}},hasEvent:function(type){if(this.__yui_events){if(this.__yui_events[type]){return true;}}return false;}};

YAHOO.namespace("Search.currentClick");
YAHOO.namespace("Search.ajaxBrowserSupport");
YAHOO.namespace("Search.ULTDelims");
YAHOO.namespace("Search.ua");
YAHOO.namespace("Search.isOpera");
YAHOO.namespace("Search.isSafari");
YAHOO.namespace("Search.isGecko");
YAHOO.namespace("Search.isIE");
YAHOO.namespace("Search.isIE7");
YAHOO.namespace("Search.ysbxchd");
YAHOO.namespace("Search.ultTracking");
YAHOO.namespace("Search.noBeaconClass");

var YS = YAHOO.Search;
YS.noBeaconClass = "nobeac";
YS.ajaxBrowserSupport = false;
YS.ignoreClick = '';
YS.ULTDelims = {'/':'P', ';':'1', '?':'P', '&':'1'};
YS.ysbxchd=false;
YS.ultTracking=false;
YS.ua = navigator.userAgent.toLowerCase();
YS.isOpera = (YS.ua.indexOf('opera') > -1);
YS.isSafari = (YS.ua.indexOf('safari') > -1);
YS.isGecko = (!YS.isOpera && !YS.isSafari && YS.ua.indexOf('gecko') > -1);
YS.isIE = (!YS.isOpera && YS.ua.indexOf('msie') > -1);
YS.isIE7 = false;
if (YS.isIE) {
    var re = new RegExp("MSIE ([0-9]{1,}[\.0-9]{0,})");
    if (re.exec(navigator.userAgent) !== null) {
        YS.isIE7 = (parseInt(RegExp.$1, 10) == 7);
    }
}
 
var d=document;
var w=window;
var yut=YAHOO.util;
var ytrkSettings = {'beac': '', 'wait': 250};
var ysearch_destUrl = '';

var AJAXconn = null;
if(w.XMLHttpRequest) {
        YS.ajaxBrowserSupport = true;
        try {
            AJAXconn = new XMLHttpRequest();
        } catch(e) {
            AJAXconn = false;
        }
} else if(w.ActiveXObject) {
    YS.ajaxBrowserSupport = true;
    try {
        AJAXconn = new ActiveXObject("Msxml2.XMLHTTP");
    } catch(e) {
        try {
            AJAXconn = new ActiveXObject("Microsoft.XMLHTTP");
        } catch(e) {
            AJAXconn = false;
        }
    }
}

var oTog=d.getElementById('yschtg');
if (oTog) {
  var aLnx=oTog.getElementsByTagName('a');
  for (var i=0;i<aLnx.length-1;i++) {
    var curTab = aLnx[i]; 
    if (curTab.id != 'vsearchmore') {
      yut.Event.addListener(curTab, 'click', passP);
    }
  }
}

if(w.attachEvent){
  w.attachEvent('onload',sbinit);
}else if(w.addEventListener){
  w.addEventListener("load",sbinit , false);
  w.addEventListener("pageshow", sbback, false);
}
if (YS.clickTrackType === 'beacon') {
    yut.Event.addListener(w, 'load', ytrkClean);
}
function setFocus() {
   if (d.getElementById) {d.getElementById('yschsp').focus();}
}  
function passP(e) {
    var o=(e.target)?((e.target.nodeType==3)?e.target.parentNode:e.target):e.srcElement;
    if (o.tagName=='SPAN'||o.tagName=='EM') {o=o.parentNode;}
    if (w.RegExp&&w.encodeURIComponent) {
        var sP=encodeURIComponent(d.getElementById('yschsp').value);
        var pattern = /^(.+?)(%3f|%26|\?|&)(p=|keywords_all=)(.*?)(%26|$|&)(.*)$/i;
        var matches = pattern.exec(o.href); 
        if (matches !== null) {    
            matches[0] = ""; 
            matches[4] = sP;

            var rdspat = new RegExp(YS.tabRedirectSeparator,"i");
            var rdsmatch = rdspat.exec(o.href);
            if (YS.ultTracking === true && rdsmatch !== null) {
                matches[4] = escape(matches[4]); //add escape() on top of encodeURIComponent() - ult hack 
                // Hack for IE, because it is double encoding the %20 to %2520 #1143718
                matches[4] = matches[4].replace(/%25/g, '%');

            }
            o.href = matches.join("");
            if (o.id == 'yschtabjobs' && !sP) {
                var pat=/jobseeker\/jobsearch\/search_results.html/i;
                o.href = o.href.replace(pat,"");
            }
        } 
    }
}
function sbback(e) {
    if (e.persisted) {
        YS.ysbxchd = false;
        sbinit();
    }
}
function sbinit(){
    if(YS.ysbxchd === false){
        var forms = d.forms['s'];
        if (forms.length && forms[0].tagName.toLowerCase() == 'form') { for (var i=0; i<forms.length; i++) {forms[i].reset();} }
        else {forms.reset();}
        if (d.forms['sB']) {
            d.forms['sB'].reset();
        }
        if (d.forms['sb']) {
            d.forms['sb'].reset();
        } 
    }
}
function chd(){
  YS.ysbxchd = true;
}
function addHandlers() {
   if (oRoot=d.getElementById(YS.ad_div_id)) {
      if (!oRoot.getElementsByTagName('ul')) {return false;}
      var aSpns=oRoot.getElementsByTagName('ul');
      for (i=0;i<aSpns.length;i++) {
         if (aSpns[i].parentNode.className==='yschspns' || aSpns[i].parentNode.id==='yschsec') {
             var aSpnsAds=aSpns[i].getElementsByTagName('li');
             for (j=0;j<aSpnsAds.length;j++) {
                var elem = aSpnsAds[j];
                if (aSpns[i].parentNode.id === 'yschsec') {
                    elem=elem.getElementsByTagName('a')[0];
                }
                else {
                    yut.Event.addListener(elem, 'click', rtEvt);
                }
                yut.Event.addListener(elem, 'mouseover', rtEvt);
                yut.Event.addListener(elem, 'mouseout', rtEvt);
             }
         }
      }
   }
}
function showStatus(poEl) { 
    while (poEl.tagName !='LI') {poEl=poEl.parentNode;}
    var sStatus = '';
    if (poEl.className!='yschprom') {sStatus=YS.goto_text +poEl.getElementsByTagName('em')[0].innerHTML;}
    else {sStatus=poEl.getElementsByTagName('a')[0].innerHTML;}
    return w.status=sStatus;
}
function changeLoc(e,poEl,obj) {
    if (poEl.tagName=='A') return true;
    while (poEl.tagName !='LI') {
        poEl=poEl.parentNode;
        if (poEl.tagName=='A') return true;
    }
    var oLnk=poEl.getElementsByTagName('a')[0];
    if (YS.clickTrackType == 'beacon') {
        if (!obj.ytrk) {obj.ytrk = {};}
        if (!obj.ytrk.beac) {obj.ytrk.beac = {};}
        obj.ytrk.data = oLnk.ytrk.data;
        obj.ytrk.forceDest = true;
        obj.ytrk.prvtDef = true;
        ytrkAjaxBeacon(e,obj);
    }
    else if (YS.clickTrackType == 'redirect') {
        sUrl=oLnk.href.replace(/#/g,'%23'); // override IE decoding    
        if (oLnk.target=='_blank') {w.open(sUrl);}
        else {location.href=sUrl;}
    }
    return false;
}
function rtEvt(e) {
    var oTrg=(e.target)?((e.target.nodeType==3)?e.target.parentNode:e.target):e.srcElement;
    switch(e.type) {
        case 'mouseover': return showStatus(oTrg); break;
        case 'mouseout': return w.status=''; break;
        case 'click': return changeLoc(e,oTrg, this); break;
    }
}
function sendBeac(psUrl) {
    if(psUrl != '') {
        var oBeac=new Image();
        oBeac.src=psUrl;
    }
}
function getClientHeight() {
   if (self.innerHeight) {return self.innerHeight;}
   else if (d.documentElement&&d.documentElement.clientHeight) {return d.documentElement.clientHeight;}
   else if (d.body.clientHeight) {return d.body.clientHeight;}
}
function getOffsetTop(poEl) {
    var iOffset=0;
    while (poEl!=d.body && poEl!=null) {
        iOffset+=poEl.offsetTop;
        poEl=poEl.offsetParent;
    }
    return iOffset;
}
function getScrollHeight() {
    if (self.pageYOffset) {return self.pageYOffset;}
    else if (d.documentElement&&d.documentElement.scrollTop) {return d.documentElement.scrollTop;}
    else if (d.body.scrollTop) {return d.body.scrollTop;}
    else {return false;}
}
function getAtf(url,query,tid,rt_logx_data,signature) {
    if ( !d.getElementById || 
      !d.getElementById('yschweb') || 
      !d.getElementById('yschweb').getElementsByTagName('LI')) 
      {return false;}
    var iClntHt=getClientHeight();
    var iWbAtf=0;
    var aResults=d.getElementById('yschweb').getElementsByTagName('LI');
         
    var sAw = screen.availWidth, sAh = screen.availHeight;
    var sTw = screen.width, sTh = screen.height;
    var wIw,wIh;
    if (self.innerHeight){wIw = self.innerWidth;wIh = self.innerHeight;}
    else if (d.documentElement && d.documentElement.clientHeight){wIw = d.documentElement.clientWidth; wIh = d.documentElement.clientHeight;}
    else if (d.body){wIw = d.body.clientWidth; wIh = d.body.clientHeight;} 
             
    for (var i=0;i<aResults.length;i++) {
        var iOffsetTop=getOffsetTop(aResults[i])+3;
        if (parseInt(iOffsetTop+aResults[i].offsetHeight)<iClntHt) iWbAtf++;
        else if (iOffsetTop<iClntHt)
            iWbAtf=iWbAtf+Math.round( ( (iClntHt-iOffsetTop) / aResults[i].offsetHeight) *100 ) /100;
    }
    if (getScrollHeight()==0) { 
        sendBeac(url + '?k=' + query + tid + '&fld='+iClntHt+'&saw='+sAw+'&sah='+sAh+'&stw='+sTw+'&sth='+sTh+'&wiw='+wIw+'&wih='+wIh+'&twb='+getOffsetTop(d.getElementById('yschweb'))+'&nwb='+Math.round(iWbAtf*100)/100+'&htmt='+(ts_ft-ts_hd)+'&onlt='+(ts_ol-ts_hd)+'&rtb='+ rt_logx_data + '&s=' + signature);
    }
}
function ytrkBeacLoaded() {
    if (AJAXconn.readyState >= 2) {
        o = YS.currentClick;
        if (o && o.ytrk.beac.loaded === false) {
            o.ytrk.beac.loaded = true;
            if (o.ytrk.timerId) {
                clearTimeout(o.ytrk.timerId);
                if (ysearch_destUrl) {top.location = ysearch_destUrl;}
            }
            else if (o.ytrk.forceDest === true) {
                if (ysearch_destUrl) {
                    if (o.ytrk.prvtDef === false) {w.open(ysearch_destUrl);}
                    else {top.location = ysearch_destUrl;}
                }
            }
        }
    }
}
function ytrkMouseDown(e,o) {
    if ((e.button == 1 && YS.isGecko ) || (e.button == 4 && YS.isIE7)) {
        var obj = (o)?o:this;
        var p = obj.ytrk.data; 
        var v = AJAXconn;
        this.ytrk.beac.loaded = false;
        this.ytrk.mouseBtn = e.button;
        if(v && YS.ajaxBrowserSupport) {
            var url = ytrkSettings.beac + p.cookie + '/.rand='+Math.random(); 
            YS.currentClick = this;
            v.open("GET", url, true);
            v.onreadystatechange = ytrkBeacLoaded;
            v.send("");
        }    
    } 
}
function ytrkAjaxBeacon(e,o) {
    var obj = (o)?o:this;
    var p = obj.ytrk.data; 
    if (e.which==3 || e.button==2) {return true;}
    if (obj.ytrk.mouseBtn) {if (obj.ytrk.mouseBtn==4 && YS.isIE7) {obj.ytrk.mouseBtn=null;return true;}}
    if (p.target == "_blank" || e.altKey || e.shiftKey || e.ctrlKey || e.metaKey) {
        obj.ytrk.prvtDef = false;
        obj.href = p.clean;
    }
    else {
        yut.Event.preventDefault(e);
    }
    ysearch_destUrl = p.clean;

    var url = ytrkSettings.beac + p.cookie + '/.rand='+Math.random();
    obj.ytrk.beac.loaded = false;
    var v = AJAXconn;
    if(v && YS.ajaxBrowserSupport) {
        YS.currentClick = obj;
        v.open("GET", url, true);
        v.onreadystatechange = ytrkBeacLoaded;
        v.send("");
    }
    else {
        obj.ytrk.beac.loaded = true;
    }
    if (obj.ytrk.prvtDef === true && obj.ytrk.beac.loaded === false) {
        obj.ytrk.timerId = setTimeout("top.location='"+ysearch_destUrl+"'; YS.currentClick.ytrk.timerId = false;",ytrkSettings.wait);
    }
}
function ytrkClean() {
    if (YAHOO.Search.ajaxBrowserSupport===false) {
        return; 
    }
    var g = yut.Dom.getStyle;
    var s = yut.Dom.setStyle;
    var ctr = 0;
    for (var i = 0; i < d.links.length; i++) {
        var el = d.links[i];
        var data = el.href;
        if (yut.Dom.hasClass(el, YS.noBeaconClass)===false) {
            data = strip(el.href);
        } 
        if (!data.ult) {
            continue;
        }
        data.target = el.target;
        el.href = data.clean;

        if (!el.ytrk) {el.ytrk = {};}
        if (!el.ytrk.beac) {el.ytrk.beac = {};}

        el.ytrk.data = data;
        el.ytrk.forceDest=false;
        el.ytrk.prvtDef=true;

        yut.Event.addListener(el, 'click', ytrkAjaxBeacon);
        yut.Event.addListener(el, 'mousedown', ytrkMouseDown);
    }
}
function strip_rd (u, data) {
    var idx = u.indexOf('/**');
    var idx1 = u.indexOf('fr2=tab-');
    if (idx != -1 && idx1 == -1) {
        data.clean = u.substr(idx + 3);
        if (YS.isIE && !YS.isIE7) {
            data.url = data.url.substr(0,idx+3) + encodeURIComponent(data.url.substr(idx+3));
        } 
        else {
            data.clean = decodeURIComponent(data.clean);
        }
    }
    else {
        data.ult = 0;
    }
    return data;
}
function strip(u) {
    var delims = YS.ULTDelims;
    var data = {url:u, clean:'', cookie:''};
    var idx = 0;
    while (u.indexOf('_yl',idx)!= -1) {
        var start = u.indexOf('_yl',idx);
        data.clean += u.slice(idx, start - 1);
        idx = start + 3;
        if (delims[u.charAt(start - 1)] && u.charAt(start + 4) === '=') {
            data.ult = 1;
            var key = "_yl" + u.charAt(start + 3);
            var value = "";
            for (start=start+5; start<u.length && !delims[u.charAt(start)];start++) {
                value += u.charAt(start);
            }
            if (data.cookie != '') {
                data.cookie +="/";
            }
            if (key != '_ylv') {
                data.cookie += "." + key.slice(1) + "=" + value;
            }
 
            if (delims[u.charAt(start)] == 'P') {
                data.clean += u.charAt(start);
            }
            idx = start + 1;
        }
        else {
            data.clean += u.slice(start - 1,idx);
        } 
    }
    if (data.ult) {
        data.clean += u.substr(idx);
        strip_rd(u, data);
    }
    return data;
}
