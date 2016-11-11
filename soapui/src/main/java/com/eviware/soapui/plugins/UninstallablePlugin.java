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

package com.eviware.soapui.plugins;

/**
 * A plugin implementing this interface can be uninstalled runtime, i.e. without restarting SoapUI. If no special
 * cleanup is required when uninstalling a plugin (apart from removing all the factories, actions etc. that it has
 * added to SoapUI), the interface can be implemented with a no-op method.
 */
public interface UninstallablePlugin extends Plugin {

    /**
     * Should return true if uninstall was successful and SoapUI does not need to be restarted.
     */

    boolean uninstall() throws Exception;
}
