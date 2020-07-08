package com.eviware.soapui.impl.actions;

import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.support.action.SoapUIAction;

/**
 * Represents a way of creating a SoapUI project from a definition, such as WSDL, WADL or Swagger.
 */
public interface ImportMethod extends Labeled {

    SoapUIAction<WorkspaceImpl> getImportAction();
}