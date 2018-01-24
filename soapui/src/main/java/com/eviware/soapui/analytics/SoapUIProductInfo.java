package com.eviware.soapui.analytics;

import com.eviware.soapui.SoapUI;
import com.smartbear.analytics.api.LicenseDetails;
import com.smartbear.analytics.api.ProductInfo;

import java.util.Collections;
import java.util.Map;

public class SoapUIProductInfo implements ProductInfo {
    private static final SoapUIProductInfo instance = new SoapUIProductInfo();

    private SoapUIProductInfo() {
    }

    @Override
    public String getVersion() {
        return SoapUI.SOAPUI_VERSION;
    }

    @Override
    public String getName() {
        return SoapUI.PRODUCT_NAME;
    }

    @Override
    public String getLicenseId() {
        return null;
    }

    @Override
    public String getLicenseType() {
        return null;
    }

    @Override
    public boolean isLicenseExpired() {
        return false;
    }

    @Override
    public Map<String, LicenseDetails> getLicensePerTool() {
        return Collections.emptyMap();
    }

    public static SoapUIProductInfo getInstance() {
        return instance;
    }

}
