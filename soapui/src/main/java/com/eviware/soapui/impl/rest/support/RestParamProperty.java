/*
 * Copyright 2004-2014 SmartBear Software
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

package com.eviware.soapui.impl.rest.support;

import java.beans.PropertyChangeListener;

import com.eviware.soapui.model.testsuite.RenameableTestProperty;

public interface RestParamProperty extends RenameableTestProperty, RestParameter {
    public abstract void addPropertyChangeListener(PropertyChangeListener listener);

    public abstract void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);

    public abstract void removePropertyChangeListener(PropertyChangeListener listener);

    public abstract void removePropertyChangeListener(String propertyName, PropertyChangeListener listener);

    public abstract boolean isDisableUrlEncoding();

    public abstract void setDisableUrlEncoding(boolean encode);
}
