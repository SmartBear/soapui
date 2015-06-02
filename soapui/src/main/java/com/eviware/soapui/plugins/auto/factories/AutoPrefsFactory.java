package com.eviware.soapui.plugins.auto.factories;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.actions.Prefs;
import com.eviware.soapui.actions.PrefsFactory;
import com.eviware.soapui.plugins.PluginProxies;
import com.eviware.soapui.plugins.auto.PluginPrefs;

/**
 * Created by ole on 15/06/14.
 */
public class AutoPrefsFactory extends AbstractSoapUIFactory<Prefs> implements PrefsFactory {
    private Class<Prefs> prefsClass;

    public AutoPrefsFactory(PluginPrefs annotation, Class<Prefs> prefsClass) {
        super(PrefsFactory.class);
        this.prefsClass = prefsClass;
    }

    @Override
    public Prefs createPrefs() {
        try {
            Prefs prefsInstance = createByReflection(prefsClass);
            return PluginProxies.proxyIfApplicable(prefsInstance);
        } catch (Exception e) {
            SoapUI.logError(e);
        }

        return null;
    }
}
