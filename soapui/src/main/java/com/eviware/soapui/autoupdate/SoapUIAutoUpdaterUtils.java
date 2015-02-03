package com.eviware.soapui.autoupdate;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.support.UISupport;

import java.io.File;

public class SoapUIAutoUpdaterUtils {
    public static SoapUIUpdateProvider getProvider (){
        if (new File(".." + File.separator + ".install4j").exists() && !UISupport.isHeadless()){
            return new Install4jSoapUIUpdateProvider(SoapUI.SOAPUI_VERSION, SoapUI.getTestMonitor());
        } else {
            return new SoapUIUpdateProviderStub();
        }
    }
}
