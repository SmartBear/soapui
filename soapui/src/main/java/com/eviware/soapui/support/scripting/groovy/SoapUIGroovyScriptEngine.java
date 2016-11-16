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

package com.eviware.soapui.support.scripting.groovy;

import com.eviware.soapui.SoapUIExtensionClassLoader;
import com.eviware.soapui.SoapUIExtensionClassLoader.SoapUIClassLoaderState;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.scripting.SoapUIScriptEngine;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.codehaus.groovy.control.CompilerConfiguration;

/**
 * A Groovy ScriptEngine
 *
 * @author ole.matzura
 */

public class SoapUIGroovyScriptEngine implements SoapUIScriptEngine {
    private GroovyClassLoader classLoader;
    private GroovyShell shell;
    private Binding binding;
    private Script script;
    private String scriptText;
    protected ScriptSaver saver = new ScriptSaver();

    public SoapUIGroovyScriptEngine(ClassLoader parentClassLoader) {
        classLoader = new GroovyClassLoader(parentClassLoader);
        binding = new Binding();
        CompilerConfiguration config = new CompilerConfiguration();
        config.setDebug(true);
        config.setVerbose(true);
        shell = new GroovyShell(classLoader, binding, config);
    }

    protected class ScriptSaver {
        private String text = null;
        private boolean locked = false;

        public synchronized void save(String scriptText) {
            if (locked) {
                text = scriptText;
            } else {
                synchronizedSetScript(scriptText);
            }
        }

        public synchronized void lockSave() {
            locked = true;
        }

        public synchronized void unlockSave() {
            if (text != null) {
                synchronizedSetScript(text);
                text = null;
            }
            locked = false;
        }
    }

    public synchronized Object run() throws Exception {
        saver.lockSave();
        SoapUIClassLoaderState state = SoapUIExtensionClassLoader.ensure();
        try {

            if (StringUtils.isNullOrEmpty(scriptText)) {
                return null;
            }

            if (script == null) {
                compile();
            }

            Object result = script.run();

            return result;
        } finally {
            state.restore();
            saver.unlockSave();
        }
    }

    protected synchronized void synchronizedSetScript(String scriptText) {
        if (scriptText != null && scriptText.equals(this.scriptText)) {
            return;
        }

        if (script != null) {
            script.setBinding(null);
            script = null;

            if (shell != null) {
                shell.resetLoadedClasses();
            }

            classLoader.clearCache();
        }

        this.scriptText = scriptText;
    }

    public synchronized void setScript(String scriptText) {
        if (scriptText != null && !scriptText.equals(this.scriptText)) {
            saver.save(scriptText);
        }
    }

    protected synchronized void reset() {
        saver.lockSave();

        script = null;

        saver.unlockSave();
    }

    public synchronized void compile() throws Exception {
        if (script == null) {
            SoapUIClassLoaderState state = SoapUIExtensionClassLoader.ensure();
            try {
                script = shell.parse(scriptText);
                script.setBinding(binding);
            } finally {
                state.restore();
            }
        }
    }

    public synchronized void setVariable(String name, Object value) {
        binding.setVariable(name, value);
    }

    public synchronized void clearVariables() {
        if (binding != null) {
            binding.getVariables().clear();
        }
    }

    public synchronized void release() {
        script = null;

        if (binding != null) {
            binding.getVariables().clear();
            binding = null;
        }

        if (shell != null) {
            shell.resetLoadedClasses();
            shell = null;
        }
    }

    protected Binding getBinding() {
        return binding;
    }

    protected GroovyClassLoader getClassLoader() {
        return classLoader;
    }

    protected Script getScript() {
        return script;
    }

    protected String getScriptText() {
        return scriptText;
    }

    protected GroovyShell getShell() {
        return shell;
    }
}
