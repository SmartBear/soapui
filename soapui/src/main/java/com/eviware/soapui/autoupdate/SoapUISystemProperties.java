package com.eviware.soapui.autoupdate;

/**
 * Created by avdeev on 19.08.2014.
 */
public interface SoapUISystemProperties {
    public final static String VERSION = "soapui.version";
    public final static String READY_API_UPDATE_URL = "http://dl.eviware.com/version-update/soapui-updates.xml";//http://resources.ej-technologies.com/install4j/help/doc/indexRedirect.html?http&&&resources.ej-technologies.com/install4j/help/doc/steps/installerGui/autoUpdateOptions.html
    public final static String READY_API_UPDATE_ADDITIONAL_URL = "http://dl.eviware.com/version-update/soapui-updates-additional.xml";
    public final static String READY_API_UPDATER_APP_ID = "4969";
}