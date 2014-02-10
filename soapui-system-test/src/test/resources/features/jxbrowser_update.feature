Feature: As a soapUI user all things keep consistent in all installers after updating jxbrowser to the lateset version 3.3

# Should test the following distirbutions: windows 32-bit, windows 64-bit, linux, MAC and stand-alone binary files #
	Scenario 1: The starter page is showing up without error when open the soapUI <distribution> in corresponding <OS> 
		Given the soapUI <distribution> is installed in corresponding <OS>
		When user open the SoapUI
		Then ensure the starter page is showing up without error
	Scenario 2: The link and button in starter page can be redirected in soapUI <distribution> in corresponding <OS>
		Given the SoapUI <distribution> is installed in corresponding <OS>
		And user started the SoapUI
		When user click the link and button in start page
		Then ensure it can redirect to the corresponding pages of the clicked link or button
	Scenario 3: The web recorder can record and replay in soapUI <distribution> in corresponding <OS>
		Given The SoapUI <distribution> is installed in <OS>
		And user started the SoapUI
		When user create a new soapUI project with web TestCase to visit www.soapUI.org
		And start to record two steps
		And set the SLA assertion 2000 to the two steps
		And stop the record
		And replay the web test case
		And save the project as soapUI-Web-Test
		Then ensure there is a web TestCase with two passed http TestStep 
	Scenario 4: The TestOnDemand can test and show the result in soapUI <distribution> in corresponding <OS>
		Given the SoapUI <distribution> is installed in <OS>
		And user started the SoapUI
		And user open the a web TestCase with two passed http TestStep
		When user open the test on demand tab
		And start a test
		And click view test results button in the page
		Then the test status and test results will show in the window