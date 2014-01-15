Feature: Internal browser

	Scenario: The starter page is showing up without error when open soapUI
		Given the soapUI OS is installed in windows-32-bit
		When user open the SoapUI OS
		Then ensure the starter page is showing up without error