package com.eviware.soapui.analytics.providers;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.analytics.AnalyticsProvider;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by aleshin on 5/15/2014.
 */
public abstract class BaseAnalyticsProvider implements AnalyticsProvider {

    @Override
    public void trackError(Throwable error) {
    }

    public final String getOsName() {
        return System.getProperty("os.name", "n/a");
    }

    public final String getOsVersion() {
        return System.getProperty("os.version", "n/a");
    }

    public final String getJavaVersion() {
        return System.getProperty("java.version", "n/a");
    }

    public final String getUserLanguage() {
        return System.getProperty("user.language", "n/a");
    }

    public final String getUserCountry() {
        return System.getProperty("user.country", "n/a");
    }

    public final String getStrScreenSize() {
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        return String.format("%dx%d", (int) size.getWidth(), (int) size.getHeight());
    }

    public final Dimension getScreenSize() {
        return Toolkit.getDefaultToolkit().getScreenSize();
    }

    public String getSoapUIVersion() {
        return SoapUI.SOAPUI_VERSION;
    }

    public String getLicenseType() {
        return "Open Source";
    }

    public String getLicenseDescription() {
        return "No License";
    }

    public String getInstanceId() {
        return "";
    }

    protected static String throwableToString(Throwable e) {
        StringWriter output = new StringWriter();
        e.printStackTrace(new PrintWriter(output));
        String stackTraceWithoutLineBreaks = output.toString().replaceAll("(\r|\n)+", " / ");
        return stackTraceWithoutLineBreaks.replaceAll("\\s+/\\s+", " / ");
    }

}
