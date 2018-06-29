package com.eviware.soapui.analytics.providers;

import com.smartbear.analytics.api.AnalyticsProvider;
import com.smartbear.analytics.api.ProductInfo;
import com.smartbear.analytics.impl.SoapUIOSUserProviderFactory;

public class OSUserProviderFactory extends SoapUIOSUserProviderFactory {
    private ProductInfo productInfo;

    public OSUserProviderFactory(ProductInfo productInfo) {
        super(productInfo);
        this.productInfo = productInfo;
    }

    @Override
    public AnalyticsProvider allocateProvider() {
        return new OSUserProvider(productInfo);
    }
}