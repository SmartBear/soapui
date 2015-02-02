package com.eviware.soapui.autoupdate;

import com.eviware.soapui.support.UISupport;

public class SoapUIUpdateProviderStub implements SoapUIUpdateProvider {

    @Override
    public void start() {

    }

    @Override
    public void showUpdateStatus() {
        UISupport.showInfoMessage("This function is not supported for SoapUI which was installed from archive.\nPlease visit the product web site.");
    }
}
