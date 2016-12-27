/*
 * SoapUI, Copyright (C) 2004-2016 SmartBear Software 
 *
 * Licensed under the EUPL, Version 1.1 or - as soon as they will be approved by the European Commission - subsequent 
 * versions of the EUPL (the "Licence"); 
 * You may not use this work except in compliance with the Licence. 
 * You may obtain a copy of the Licence at: 
 * 
 * http://ec.europa.eu/idabc/eupl 
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is 
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the Licence for the specific language governing permissions and limitations 
 * under the Licence. 
 */

package com.eviware.soapui.report;

import com.eviware.soapui.config.ReportTypeConfig;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.TestPropertyHolder;
import com.eviware.soapui.support.types.StringToObjectMap;

import java.util.Collection;

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
