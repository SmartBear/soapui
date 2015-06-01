package com.eviware.soapui.plugins.auto.factories;

import com.eviware.soapui.config.ReportTypeConfig;
import com.eviware.soapui.plugins.SoapUIFactory;
import com.eviware.soapui.plugins.auto.PluginSubReport;
import com.eviware.soapui.report.SubReport;
import com.eviware.soapui.report.AbstractSubReportFactory;

/**
 * Created by ole on 15/06/14.
 */
public class AutoSubReportFactory extends AbstractSubReportFactory implements SoapUIFactory {
    public AutoSubReportFactory(PluginSubReport annotation, Class<SubReport> subReportClass) {
        super(annotation.name(), annotation.description(), annotation.id(), ReportTypeConfig.Enum.forString(annotation.level()));
    }

}
