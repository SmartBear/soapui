/*
Example scripts that can be used to test the OAuth 2 access token automation feature – see the feature file
automate-browser-interaction-feature.
 */

/* Script that will perform a login on the Google login page. Obviously, substitute your real Google user name and password
for the values google_user_name and google_password. Enter this in the field labeled Login screen script.
 */
document.getElementById('Email').value = 'google_user_name';
document.getElementById('Passwd').value = 'google_password';
document.getElementById('gaia_loginform').submit();

/* Script that will click on the button on the Google consent screen. Enter this in the field labeled Consent screen
script.
 */
document.getElementById('submit_approve_access').click()