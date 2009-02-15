package com.eviware.soapui.actions;

import com.eviware.soapui.impl.settings.SettingsImpl;
import com.eviware.soapui.settings.HttpSettings;
import com.eviware.soapui.support.components.SimpleForm;
import com.eviware.soapui.support.types.StringToStringMap;

import junit.framework.TestCase;

public class AnnotatedSettingsPrefsTestCase extends TestCase
{
   public void testHttpVersion() throws Exception
   {
      AnnotatedSettingsPrefs prefs = new AnnotatedSettingsPrefs( HttpSettings.class, "HTTP Settings" );
      SettingsImpl settings = new SettingsImpl();
      StringToStringMap values = prefs.getValues(settings);
      assertEquals(HttpSettings.HTTP_VERSION_1_1, values.get("HTTP Version"));
      
      prefs.setFormValues( settings );
      SimpleForm form = prefs.getForm();
      assertEquals(HttpSettings.HTTP_VERSION_1_1, form.getComponentValue("HTTP Version"));
   }
}
