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

package com.eviware.soapui.support.scripting.js;

import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.scripting.SoapUIScriptEngine;
import com.eviware.soapui.support.types.StringToObjectMap;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.ScriptableObject;

/**
 * A Groovy ScriptEngine
 *
 * @author ole.matzura
 */

public class JsScriptEngine implements SoapUIScriptEngine {
    private String scriptText;
    private StringToObjectMap properties = new StringToObjectMap();
    private final ClassLoader parentClassLoader;

    public JsScriptEngine(ClassLoader parentClassLoader) {
        this.parentClassLoader = parentClassLoader;
    }

    public Object run() throws Exception {
        if (StringUtils.isNullOrEmpty(scriptText)) {
            return null;
        }

        Context context = ContextFactory.getGlobal().enterContext();
        context.setApplicationClassLoader(parentClassLoader);

        ScriptableObject scope = context.initStandardObjects();

        try {
            for (String name : properties.keySet()) {
                ScriptableObject.putProperty(scope, name, Context.javaToJS(properties.get(name), scope));
            }

            Script script = context.compileString(scriptText, "Script", 0, null);

            return script.exec(context, scope);
        } finally {
            for (String name : properties.keySet()) {
                scope.delete(name);
            }

            Context.exit();
        }
    }

    public synchronized void setScript(String scriptText) {
        if (scriptText != null && scriptText.equals(this.scriptText)) {
            return;
        }

        this.scriptText = scriptText;
    }

    public void compile() throws Exception {
    }

    public void setVariable(String name, Object value) {
        properties.put(name, value);
    }

    public void clearVariables() {
        properties.clear();
    }

    public void release() {
        clearVariables();
    }
}
