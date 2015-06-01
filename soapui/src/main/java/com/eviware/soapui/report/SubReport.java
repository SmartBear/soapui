/*
 *  SoapUI Pro, copyright (C) 2007-2012 smartbear.com
 */

package com.eviware.soapui.report;

public interface SubReport {
    public String getNameInReport();

    public void release();

    public void prepare();

    public String getReportClassName();
}
