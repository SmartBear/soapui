/*
 Example script that can be used to test the OAuth 2 access token automation feature – see the feature file
 automate-browser-interaction-feature.
 */

/* Script that will perform a login on the Google login page. Obviously, substitute your real Google user name and password
 for the values google_user_name and google_password. Enter this in the field labeled Login screen script.
 */
function consent(){
    document.getElementById('submit_approve_access').click();
}

if (document.getElementById('Email')) {
    document.getElementById('Email').value = 'google_user_name';
    document.getElementById('Passwd').value = 'google_password';
    document.getElementById('gaia_loginform').submit();
}
else if (document.getElementById('submit_approve_access')) {
    // Try many times since the button is disabled for a while in the beginning
    window.setInterval(consent, 100);
}
