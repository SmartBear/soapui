var skype_injection_path = 'chrome://skype_ff_toolbar_win/content/';
var skype_tool = null;

/*window.addEventListener("click",skype_click_handler,true);
function skype_click_handler(event)
{
	if (skype_tool)
		skype_tool.closemenu();
	//HideSkypeMenuFull();
} */

//CALL BUTTON
var SkypeActiveCallButtonPart = 0;
function SkypeSetCallButtonPart(obj)
{
	if (obj.getAttribute('id') == '__skype_highlight_id_left')
	{
		SkypeActiveCallButtonPart = 0;
	}
	else if (obj.getAttribute('id') == '__skype_highlight_id_right')
	{
		SkypeActiveCallButtonPart = 1;
	}
}

function SkypeSetCallButton(obj, hl, isInternational, isFax)
{
	var cb_part_l = null;
	var cb_part_ml = null;
	var cb_part_mr = null;
	var cb_part_r = null;
	if (obj.getAttribute('rtl') == 'false')
	{
		cb_part_l = obj.firstChild.firstChild;
		cb_part_ml = obj.firstChild.lastChild;
		cb_part_mr = obj.lastChild.firstChild;
		cb_part_r = obj.lastChild.lastChild;

		cb_flag = obj.firstChild.lastChild.firstChild;
		if (cb_flag && cb_flag.isSameNode(obj.firstChild.firstChild.firstChild) == true)
			cb_flag = null;
	}
	else
	{
		cb_part_l = obj.lastChild.lastChild;
		cb_part_ml = obj.lastChild.firstChild;
		cb_part_mr = obj.firstChild.lastChild;
		cb_part_r = obj.firstChild.firstChild;

		cb_flag = obj.lastChild.firstChild.lastChild;
		if (cb_flag && cb_flag.isSameNode(obj.lastChild.lastChild.lastChild) == true)
			cb_flag = null;
	}

	if (hl == 1)
	{
		cb_part_l.style.backgroundImage = "url('chrome://skype_ff_toolbar_win/content/cb_mouseover_l.gif')";
		if (cb_part_l.isSameNode(cb_part_ml) != true)
			cb_part_ml.style.backgroundImage = "url('chrome://skype_ff_toolbar_win/content/cb_mouseover_m.gif')";
		cb_part_mr.style.backgroundImage = "url('chrome://skype_ff_toolbar_win/content/cb_mouseover_m.gif')";


		if (isInternational == "0")
		{
			if (SkypeActiveCallButtonPart == 0)    //left
			{
				cb_part_r.style.backgroundImage = "url('chrome://skype_ff_toolbar_win/content/cb_mouseonflag_r"+(isFax?"_fax":"")+".gif')";
				//shadow
				if (cb_flag)
				{
					cb_flag.style.top = '1px';
					cb_flag.style.left = '1px';
					/*top right bottom left*/
					cb_flag.style.padding = '1px 0px 0px 1px';//'2px 0px 0px 0px';
				}
			}
			else                            //right
			{
				cb_part_r.style.backgroundImage = "url('chrome://skype_ff_toolbar_win/content/cb_mouseover_r"+(isFax?"_fax":"")+".gif')";
				//flag
				if (cb_flag)
				{
					cb_flag.style.top = '0px';
					cb_flag.style.left = '0px';
					cb_flag.style.padding = '0px 1px 1px 0px';//'0px 1px 1px 0px';
					cb_flag.style.margin = '0px 0px 2px 0px;';
				}
			}
		}
		else
		{
			cb_part_r.style.backgroundImage = "url('chrome://skype_ff_toolbar_win/content/cb_mouseover_r"+(isFax?"_fax":"")+".gif')";
			//flag
			if (cb_flag)
			{
				cb_flag.style.top = '0px';
				cb_flag.style.left = '0px';
				cb_flag.style.padding = '0px 1px 1px 0px';
				cb_flag.style.margin = '0px 0px 2px 0px;';
			}
		}
	}
	else
	{
		cb_part_l.style.backgroundImage = "url('chrome://skype_ff_toolbar_win/content/cb_normal_l.gif')";
		if (cb_part_l.isSameNode(cb_part_ml) != true)
			cb_part_ml.style.backgroundImage = "url('chrome://skype_ff_toolbar_win/content/cb_normal_m.gif')";
		cb_part_mr.style.backgroundImage = "url('chrome://skype_ff_toolbar_win/content/cb_normal_m.gif')";
		cb_part_r.style.backgroundImage = "url('chrome://skype_ff_toolbar_win/content/cb_normal_r"+(isFax?"_fax":"")+".gif')";

		//flag
		if (cb_flag)
		{
			cb_flag.style.top = '0px';
			cb_flag.left = '0px';
			cb_flag.style.padding = '0px 1px 1px 0px';
			cb_flag.style.margin = '0px 0px 2px 0px;';
		}
	}
}
var skype_cb_l = '';
var skype_cb_m = '';
var skype_cb_r = '';

function SkypeSetCallButtonPressed(obj, pr, isInternational, isFax)
{
	var cb_part_l = null;
	var cb_part_ml = null;
	var cb_part_mr = null;
	var cb_part_r = null;
	if (obj.getAttribute('rtl') == 'false')
	{
		cb_part_l = obj.firstChild.firstChild;
		cb_part_ml = obj.firstChild.lastChild;
		cb_part_mr = obj.lastChild.firstChild;
		cb_part_r = obj.lastChild.lastChild;

		cb_flag = obj.firstChild.lastChild.firstChild;
		if (cb_flag && cb_flag.isSameNode(obj.firstChild.firstChild.firstChild) == true)
			cb_flag = null;
	}
	else
	{
		cb_part_l = obj.lastChild.lastChild;
		cb_part_ml = obj.lastChild.firstChild;
		cb_part_mr = obj.firstChild.lastChild;
		cb_part_r = obj.firstChild.firstChild;

		cb_flag = obj.lastChild.firstChild.lastChild;
		if (cb_flag && cb_flag.isSameNode(obj.lastChild.lastChild.lastChild) == true)
			cb_flag = null;
	}
	if (pr == 1)
	{
		skype_cb_l = cb_part_l.style.backgroundImage;//getAttribute('src');
		skype_cb_m = cb_part_mr.style.backgroundImage;
		skype_cb_r = cb_part_r.style.backgroundImage;//getAttribute('src');

		if (isInternational == "0")
		{
			if (SkypeActiveCallButtonPart == 0)    //left
			{
				//obj.firstChild.firstChild.setAttribute('src', 'chrome://skype_ff_toolbar_win/content/cb_down_l.gif');
				cb_part_l.style.backgroundImage = "url('chrome://skype_ff_toolbar_win/content/cb_down_l.gif')";
				if (cb_part_l.isSameNode(cb_part_ml) != true)
					cb_part_ml.style.backgroundImage = "url('chrome://skype_ff_toolbar_win/content/cb_down_m.gif')";
			}
			else                            //right
			{
				//obj.firstChild.firstChild.setAttribute('src', 'chrome://skype_ff_toolbar_win/content/cb_down_l.gif');
				cb_part_l.style.backgroundImage = "url('chrome://skype_ff_toolbar_win/content/cb_down_l.gif')";
				if (cb_part_l.isSameNode(cb_part_ml) != true)
					cb_part_ml.style.backgroundImage = "url('chrome://skype_ff_toolbar_win/content/cb_down_m.gif')";
				cb_part_mr.style.backgroundImage = "url('chrome://skype_ff_toolbar_win/content/cb_down_m.gif')";
				//obj.lastChild.lastChild.setAttribute('src', 'chrome://skype_ff_toolbar_win/content/cb_down_r"+(isFax?"_fax":"")+".gif');
				cb_part_r.style.backgroundImage = "url('chrome://skype_ff_toolbar_win/content/cb_down_r"+(isFax?"_fax":"")+".gif')";
			}
		}
		else
		{
			//obj.firstChild.firstChild.setAttribute('src', 'chrome://skype_ff_toolbar_win/content/cb_down_l.gif');
			cb_part_l.style.backgroundImage = "url('chrome://skype_ff_toolbar_win/content/cb_down_l.gif')";
			if (cb_part_l.isSameNode(cb_part_ml) != true)
				cb_part_ml.style.backgroundImage = "url('chrome://skype_ff_toolbar_win/content/cb_down_m.gif')";
			cb_part_mr.style.backgroundImage = "url('chrome://skype_ff_toolbar_win/content/cb_down_m.gif')";
				//obj.lastChild.lastChild.setAttribute('src', 'chrome://skype_ff_toolbar_win/content/cb_down_r"+(isFax?"_fax":"")+".gif');
			cb_part_r.style.backgroundImage = "url('chrome://skype_ff_toolbar_win/content/cb_down_r"+(isFax?"_fax":"")+".gif')";
		}
	}
	else
	{
		//obj.firstChild.firstChild.setAttribute('src', skype_cb_l);
		cb_part_l.style.backgroundImage = skype_cb_l;
		if (cb_part_l.isSameNode(cb_part_ml) != true)
			cb_part_ml.style.backgroundImage = skype_cb_m;
		cb_part_mr.style.backgroundImage = skype_cb_m;
		//obj.lastChild.lastChild.setAttribute('src', skype_cb_r);
		cb_part_r.style.backgroundImage = skype_cb_r;
	}
}

//COMMANDS
function SkypeToolBarInit(tb)
{
	skype_tool = tb;
}

function doRunCMDSkype(event, link, id, name)
{
	if (skype_tool)
	{
		if (link == 'chdial')
		{
			var obj=event.originalTarget;
			if (!obj)
				obj=event;

			/*var brd=SkypeGetBounds(obj);
		   var docelem = document.documentElement;
		   if ((docelem.scrollTop == 0 && document.body.scrollTop != 0) || (docelem.scrollLeft == 0 && document.body.scrollLeft != 0))
			   docelem=document.body;
   
		   scrollY=docelem.scrollTop;
		   scrollX=docelem.scrollLeft;
   
		   var my=brd.top+16, mx=brd.left;/*brd.height*/

			//var oDoc = obj.ownerDocument;

			/*var obj2 = document.getBoxObjectFor(element);
   
		   var html_box = new XPCNativeWrapper(obj2).wrappedJSObject;
		   if (!html_box)
			 html_box = obj2;
   
		   var screenX = html_box.screenX;
		   var screenY = html_box.screenY;
			   */

			SkypeFlagColor='rgb(184, 203, 255)';
			skype_tool.chprefix(id, document, obj, screenY);
		}
		else if (link == 'copy')
			skype_tool.copy_num(id, document);
		else if (link == 'sms')
			skype_tool.sms(id);
		else if (link == 'add')
			skype_tool.add(id, name);
		else
			skype_tool.call(id);
	}
}

function runCMDSkype(link)
{
	HideSkypeFull();
	doRunCMDSkype(null, link, skype_curid, skype_cur_name);
}

//FLAG TOOLTIP
function HideSkypeFull()
{
	//   skype_active=false;
	document.getElementById('skype_dc').style.visibility = "hidden";
}

function ShowSkype(event, title)
{
	//  skype_active=true;
	DoShowSkype(event.pageX, event.pageY + 20, title);
}

function DoShowSkype(cX, cY, title)
{
	var menu = document.getElementById('skype_dc');
	menu.firstChild.nodeValue = title;
	menu.style.visibility = "visible";

	menu.style.left = cX + 'px';
	menu.style.top = cY + 'px';

}
var SkypeFlagColor = 'rgb(184, 203, 255)';
function doSkypeFlag(obj, brd)
{
	SkypeFlagColor = brd;
}

//MENU
var skype_curid = 0,skype_active = false,skype_showseed = 0,skype_ctm = 0,skype_cur_name = '';
var skype_curbutton = null;
function SkypeCheckCallButton(obj)
{
	var res = false;
	if (skype_curbutton && skype_curbutton.isSameNode(obj) == true)
		res = true;

	skype_curbutton = obj;
	return res;
}

function HideSkypeMenu()
{                       //skype_tool.sd('HideSkypeMenu skype_active='+skype_active);
	if (!skype_active)
		HideSkypeMenuFull();
	else
		setTimeout("HideSkypeMenu()", 1000);
}

function HideSkypeMenu2(event)
{
	skype_showseed = 0;
	if (!skype_active)
		HideSkypeMenuFull();
	else
	{
		skype_active = false;
		setTimeout("HideSkypeMenu()", 1000);
	}
}

function HideSkypeMenuFull()
{                                 //skype_tool.sd('HideSkypeMenuFull skype_active='+skype_active);
	/*   skype_active=false;
	document.getElementById('skype_menu').style.visibility="hidden";*/
}

function CheckSkype()
{
	skype_active = true;
}

function ShowSkypeMenu(event, call_msg, id, callto, isMobile, name, x, y)
{
	try {
		/*  	skype_active=true;       						//skype_tool.sd('ShowSkypeMenu skype_curid='+skype_curid+'   callto='+callto+'  id='+id);
		if(document.getElementById('skype_menu').style.visibility!="hidden" && skype_curid==callto)//id
		  return;
		skype_showseed=Math.random();
		clearTimeout(skype_ctm);
	  var obj=event.originalTarget;
	  if (!obj)
		  obj=event;
  //  	var brd=SkypeGetBounds(obj);
		//var pcx=brd.left, pcy=brd.top+16;/*brd.height;* /   //alert(pcx+'--'+pcy);
  
	  scrolls=SkypeGetDivScroll(obj);
  
		var pcx = x+scrolls.left, pcy = y+scrolls.top;
  
		skype_ctm=setTimeout('DoShowSkypeMenu(\''+call_msg+'\','+skype_showseed+',"'+callto+'",'+pcx+','+pcy+','+isMobile+',"'+name+'")',0);*/
	} catch(e) {
	}
}

function DoShowSkypeMenu(call_msg, seed, callto, pcx, pcy, isMobile, name)
{
	try {                                                        //skype_tool.sd('DoShowSkypeMenu seed='+seed+'   skype_showseed='+skype_showseed);
		/*  	if(seed!=skype_showseed)
		  return;
		skype_showseed=0;
		skype_curid=callto;
	  skype_cur_name=name;
		var menu=document.getElementById('skype_menu');
  
		var my=pcy, mx=pcx;
  
	  var menubox=document.getBoxObjectFor(menu);
	  //var docelem = document.documentElement;
									//alert(menubox.height);
									//alert('doc h='+document.height + ' y='+my+' menu h='+menubox.height);
	  var docH=document.height;
	  if (docH < document.documentElement.clientHeight) docH=document.documentElement.clientHeight;
	  if (docH < document.documentElement.scrollHeight) docH=document.documentElement.scrollHeight;
	  if (docH < document.body.clientHeight) docH=document.body.clientHeight;
	  if (docH < document.body.scrollHeight) docH=document.body.scrollHeight;
	  if ((my + menubox.height) > docH)
		  my=my - 16 - menubox.height;
	  if (my < 0) my=0;
  //    if (document.height != 0 && (my + menubox.height) > document.height && (my + menubox.height) > document.documentElement.scrollHeight)//docelem.clientHeight)
		  //my=document.height - menubox.height;
  
	  var docW=document.width;
	  if (docW < document.documentElement.clientWidth) docW=document.documentElement.clientWidth;
	  if (docW < document.documentElement.scrollWidth) docW=document.documentElement.scrollWidth;
	  if (docW < document.body.clientWidth) docW=document.body.clientWidth;
	  if (docW < document.body.scrollWidth) docW=document.body.scrollWidth;
	  if ((mx + menubox.width) > docW)
		  mx=document.width - menubox.width;
	  if (mx < 0) mx=0;
  //	if (document.width != 0 && (mx + menubox.width) > document.width && (mx + menubox.width) > document.documentElement.scrollWidth)//docelem.clientWidth)
  
		menu.style.left=mx+'px';
		menu.style.top=my+'px';
  
	  skype_tool.doFixMenu(menu, isMobile, document);
  
	  menu.style.visibility="visible";  */
	} catch(e) {
	}
}

function SkypeSetBgColor(obj, color)
{
	try {
		obj.lastChild.style.backgroundColor = color;
	} catch(e) {
	}
}

//UTIL
function SkypeGetBounds(element)
{
	try {
		var left = element.left;
		var top = element.top;
		while (!(element.tagName.toLowerCase() == 'span' && element.getAttribute('id') == '__skype_highlight_id'))
		{
			element = element.parentNode;
		}
		left = element.offsetLeft;
		top = element.offsetTop;
		for (var parent = element.offsetParent; parent; parent = parent.offsetParent)
		{
			left += parent.offsetLeft;
			top += parent.offsetTop;
			if (parent.tagName.toLowerCase() == 'div')
			{
				left -= parent.scrollLeft;
				top -= parent.scrollTop;
			}
		}


		return {left: left, top: top, width: element.offsetWidth, height: element.offsetHeight};
	} catch(e) {
	}
}

function SkypeGetDivScroll(element)
{
	try {
		var left = 0;
		var top = 0;
		while (!(element.tagName.toLowerCase() == 'span' && element.getAttribute('id') == '__skype_highlight_id'))
		{
			element = element.parentNode;
		}

		for (var parent = element.offsetParent; parent; parent = parent.offsetParent)
		{
			if (parent.tagName.toLowerCase() == 'div')
			{
				left -= parent.scrollLeft;
				top -= parent.scrollTop;
			}
		}


		return {left: left, top: top};
	} catch(e) {
	}
}