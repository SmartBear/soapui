<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>soapUI - Testing the Web Services</title>
<link href="css/software.css" rel="stylesheet" type="text/css" />
<style type="text/css">
<!--
-->
</style>

</head>

<body>

<div id="page">
<!--header-->
<div id="header">

<div id="top-stripe"><span class="left">
<script type="text/javascript">
//starter
today=new Date(); 
date=today.getDate(); 
year=1900 + today.getYear(); //cheating here
day = today.getDay(); 
month=today.getMonth()+1; 

// Day in text
var dayName=new Array(7)
dayName[0]="Sunday";
dayName[1]="Monday";
dayName[2]="Tuesday";
dayName[3]="Wednesday";
dayName[4]="Thursday";
dayName[5]="Friday";
dayName[6]="Saturday";

// SuffiXToDate
if (date==1) suffix=("st");
else if (date==2) suffix=("nd");
else if (date==3) suffix=("rd");
else if (date==21) suffix=("st");
else if (date==22) suffix=("nd");
else if (date==23) suffix=("rd");
else if (date==31) suffix=("st");
else suffix=("th");

// Month in text
if (month==1) monthName=("January");
else if (month==2) monthName=("February");
else if (month==3) monthName=("March");
else if (month==4) monthName=("April");
else if (month==5) monthName=("May");
else if (month==6) monthName=("June");
else if (month==7) monthName=("July");
else if (month==8) monthName=("August");
else if (month==9) monthName=("September");
else if (month==10) monthName=("October");
else if (month==11) monthName=("November");
else monthName=("December");

// Write date
document.write(dayName[day] + ", " + monthName + " " + date + suffix + ", " + year);
</script>
</span>
<span class="right">&copy; 2010 <a href="http://www.eviware.com" target="_blank">Eviware AB</a></span></div>

<div id="try-buy">
<a onclick="javascript:urchinTracker('/appindexsoapUITopTry')" href="http://www.eviware.com/trial" title="Try SoapUI"><img src="images/try.png" alt="Try SoapUI" /></a><a onclick="javascript: pageTracker._trackPageview('/appindexsoapUITopBuy')" href="http://www.eviware.com/store" title="Buy SoapUI"><img src="images/buy.png" alt="Buy SoapUI" /></a></div>



</div>
<!--end of header-->

<!--content-->
<div id="content">

<!--column-3-->
<div id="column-3">

<div id="header-rightbox">
<div class="title"><img src="images/news-title.png" alt="News" /></div>
<h2>soapUI Award Winners</h2>
<p>soapUI has won the award as the second best Performance test tool.</p>
<div class="links"><a onclick="javascript: pageTracker._trackPageview('/appindexsoapUIMinorNewsReadMore')" "href="http://www.soapui.org/new_and_noteworthy_3_0.html">Read more</a></div>
<div class="clearfix"></div>
</div>

<div class="ad-type-3">
<div class="title"><img src="images/feature-focus-title.png" alt="News" /></div>
<h2>JMS Testing</h2>
<p>Test your Enterprise Messaging such as MQ's with our new JMS TestStep.</p>
<div class="links"><a onclick="javascript: pageTracker._trackPageview('/appindexsoapUIJMSTutorial')" href="http://www.soapui.org/tutorials/jms/jms-tutorial.html">Read more</a></div>
<div class="clearfix"></div>
</div>
<div class="ad-type-3">
<div class="title"><img src="images/training.png" alt="News" /></div>
<h2>Get Trained!<br />
</h2>
<p>Whether new to soapUI or experienced, Think88´s training classes will increase your productivity making you a more effective tester</p>
<p>Free upgrade to soapUI Pro for 5 or more students <strong>Think88.</strong> </p>
<div class="links"><a onclick="javascript: pageTracker._trackPageview('/appindexsoapUITrainingReadMore')" href="http://www.think88.com/soap.html">Read more</a></div>
<div class="clearfix"></div>
</div>
<div class="ad-type-3">
<div class="title"><img src="images/feature-focus-title.png" alt="News" /></div>
<h2>AMF Support</h2>
<p>For the Flash or Flex developer we now release AMF Testing for RIA Quality Assurance.</p>
<div class="links"><a onclick="javascript: pageTracker._trackPageview('/appindexamftutorial')" href="http://www.soapui.org/tutorials/amf/amf-tutorial.html">Read more</a></div>
<div class="clearfix"></div>
</div>
</div><!--end of column-3-->

<div id="header-leftbox">
<div class="title"><img src="images/news-title.png" alt="News" /></div>
<h2>soapUI 3.5 - The Protocol Release is out</h2>
<img class="picture" title="New release" src="images/release.jpg" />
<p> Try The Protocol Release,<br />
With complete functionality for testing<br />
your Enterprise Messages, RIA's and Databases.<br /> 
</p>
<div class="links"><a onclick="javascript: pageTracker._trackPageview('/appindexsoapUIMainNewsReadMore')" href="http://www.soapui.org/new_and_noteworthy_3_5.html">Read more</a><a onclick="javascript: pageTracker._trackPageview('/appindexsoapUIMainNewsDownload')" href="http://sourceforge.net/projects/soapui/files/">Download here</a></div>
<div class="clearleft"></div>
</div>


<!--column-1-->
<div id="column-1">
<div id="did-you-know">
<div class="top"></div>
<div class="middle">
<h2>Did you know...</h2>
<p> soapUI Pro builds on the functionality of soapUI and makes it more useful for the enterprise customer. soapUI Pro will improve your productivity and make Web Service testing more fun.</p>
<ol>
<li>Get a <span class="YellowText">Form Based </span>or <span class="style2">Outlined view</span> of your Requests and Responses.</li>
<li>Create <span class="YellowText">Data Driven Tests</span> with the DataSource TestStep. </li>
<li>Determine how well Tested Your Web Services are with <span class="YellowText">Coverage</span></li>
<li>Save data from your tests with the <span class="YellowText">DataSink TestStep</span>.</li>
<li>Distribute Test Results with soapUI's powerful <span class="YellowText">Reporting</span>.</li>
<li>Do point and click testing with <span class="YellowText">XPath Selection</span>.</li>
<li>Get<span class="YellowText"> World Class Support</span>!</li>
<li>Try the new <span class="YellowText"> SQL Query Builder</span></li>
</ol>
</div>
<div class="bottom">
<a onclick="javascript: pageTracker._trackPageview('/appindexsoapUIDYKTry')" href="http://www.eviware.com/trial">Download Free Trial</a>
</div>
</div>
<div class="ad-type-1"><a onclick="javascript: pageTracker._trackPageview('/appindexsoapUIDownDocs')" href="http://www.soapui.org"><img src="images/documentation.png" alt="Documentation" /></a></div>
<div class="ad-type-1"><a onclick="javascript: pageTracker._trackPageview('/appindexsoapUIDownHelp')" href="http://www.eviware.com/forums"><img src="images/help.png" alt="Help" /></a></div>
</div><!--end of column-1-->

<!--column-2-->
<div id="column-2">
<div class="ad-type-2">
<div class="ad-type-2-inner">
<div class="title"><img src="images/feature-focus-title.png" alt="News" /></div>
<img class="type-2-picture" src="Images/qb-ilu-1.png" alt="T-Shirt" />
<p>Create complex Database Queries without the need for SQL skills with our Query Builder <em>(soapUI Pro)</em>.</p>
<div class="links"></div>
</div>
</div>
<div class="ad-type-2">
<div class="ad-type-2-inner button">
<div class="title"><img src="images/feature-focus-title.png" alt="News" /></div>
<img class="type-2-picture" src="Images/JDBC.png" alt="10tips" />
<p>Our new JDBC DataStep enables complete end-to-end testing of your SOA's. </p>
<a class="learnmore" title="Learn more" onclick="javascript: pageTracker._trackPageview('/appindexsoapUIJDBCTutorial')" href="http://www.soapui.org/tutorials/jdbc/jdbc-tutorial.html"><img src="images/blank.gif" alt="blank" /></a></div>
</div>
</div><!--end of column-2-->


<div class="clearfix"></div>
</div>
<!--end of content-->


</div>
<script type="text/javascript">
var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
</script>
<script type="text/javascript">
try {
var pageTracker = _gat._getTracker("UA-92447-6");
pageTracker._trackPageview();
} catch(err) {}</script>
</body>
</html>
