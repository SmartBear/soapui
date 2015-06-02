/*
 *  SoapUI Pro, copyright (C) 2007-2012 smartbear.com
 */

package com.eviware.soapui.report;

import java.util.Collection;

import com.eviware.soapui.config.ReportTypeConfig;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.TestPropertyHolder;
import com.eviware.soapui.support.types.StringToObjectMap;

public interface ModelItemReport {
    public ModelItem getModelItem();

    public void addSubReport(SubReport subReport);

    public void removeSubReport(SubReport subReport);

    public SubReport[] getSubReports();

    public SubReport getSubReportByName(String name);

    public ReportTypeConfig.Enum getLevel();

    public void prepare();

    public void release();

    public boolean hasSubReport(String id);

    public <T2> Collection<T2> getSubReportsByType(Class<T2> clazz);

    public void onGenerate(StringToObjectMap params);

    public TestPropertyHolder getModelItemReportParameters();
}
