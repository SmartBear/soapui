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
import com.eviware.soapui.plugins.SoapUIFactory;

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
