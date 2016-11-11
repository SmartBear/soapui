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

package com.eviware.soapui.impl.settings;

import com.eviware.soapui.model.settings.SettingsListener;

import java.lang.ref.WeakReference;

/**
 * Weak listener to settingChanged
 *
 * @author ole.matzura
 */

public final class WeakSettingsListener implements SettingsListener {
    private final WeakReference<SettingsListener> listenerReference;

    public WeakSettingsListener(SettingsListener listener) {
        listenerReference = new WeakReference<SettingsListener>(listener);
    }

    public void settingChanged(String name, String newValue, String oldValue) {
        if (listenerReference.get() != null) {
            listenerReference.get().settingChanged(name, newValue, oldValue);
        }
    }

    @Override
    public void settingsReloaded() {
        if (listenerReference.get() != null) {
            listenerReference.get().settingsReloaded();
        }

    }
}
