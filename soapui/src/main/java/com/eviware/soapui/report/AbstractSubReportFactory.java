/*
 *  SoapUI Pro, copyright (C) 2007-2012 smartbear.com
 */

package com.eviware.soapui.report;

import com.eviware.soapui.config.ReportTypeConfig;
import com.eviware.soapui.plugins.SoapUIFactory;
import com.eviware.soapui.report.ModelItemReport;
import com.eviware.soapui.report.SubReport;
import com.eviware.soapui.report.SubReportFactory;

public abstract class AbstractSubReportFactory implements SubReportFactory, SoapUIFactory {
    public final ReportTypeConfig.Enum[] levels;
    private final String description;
    private final String name;
    private final String id;

    protected AbstractSubReportFactory(String name, String description, String id, ReportTypeConfig.Enum[] levels) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.levels = levels;
    }

    public AbstractSubReportFactory(String name, String description, String id) {
        this(name, description, id, new ReportTypeConfig.Enum[]{ReportTypeConfig.PROJECT, ReportTypeConfig.TESTSUITE,
                ReportTypeConfig.TESTCASE, ReportTypeConfig.LOADTEST});
    }

    public AbstractSubReportFactory(String name, String description, String id, ReportTypeConfig.Enum reportLevel) {
        this(name, description, id, new ReportTypeConfig.Enum[]{reportLevel});
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public SubReport[] buildSubReports(ModelItemReport modelItem) {
        SubReport subReport = buildSubReport(modelItem);
        return subReport == null ? new SubReport[0] : new SubReport[]{subReport};
    }

    public SubReport buildSubReport(ModelItemReport modelItem) {
        return null;
    }

    public ReportTypeConfig.Enum[] getLevels() {
        return levels;
    }

    public String getId() {
        return id;
    }

    @Override
    public Class<?> getFactoryType() {
        return SubReportFactory.class;
    }
}
