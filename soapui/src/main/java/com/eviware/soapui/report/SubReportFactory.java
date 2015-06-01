/*
 *  SoapUI Pro, copyright (C) 2007-2012 smartbear.com
 */

package com.eviware.soapui.report;

import com.eviware.soapui.config.ReportTypeConfig;

public interface SubReportFactory {
    public SubReport[] buildSubReports(ModelItemReport modelItem);

    public String getName();

    public String getDescription();

    public String getId();

    public ReportTypeConfig.Enum[] getLevels();
}
