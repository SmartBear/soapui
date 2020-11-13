/*
 * SoapUI, Copyright (C) 2004-2019 SmartBear Software
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

package com.eviware.soapui.impl.wsdl.actions.iface.tools.support;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.actions.SoapUIPreferencesAction;
import com.eviware.soapui.impl.support.AbstractInterface;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.support.PathUtils;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.settings.ProjectSettings;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.DefaultActionList;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormDialogBuilder;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormTextField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FilenameFilter;

/**
 * Abstract base class for Tool Actions
 *
 * @author Ole.Matzura
 */

public abstract class AbstractToolsAction<T extends ModelItem> extends AbstractSoapUIAction<T> {
    @SuppressWarnings("unused")
    private static final Logger log = LogManager.getLogger(AbstractToolsAction.class);

    protected static final String WSDL = "WSDL";
    protected static final String CACHED_WSDL = "Use cached WSDL";
    protected static final String JAVA_ARGS = "Java Args";
    protected static final String TOOL_ARGS = "Tool Args";
    protected static final String SOAPUISETTINGSPASSWORD = "user-settings.xml Password";

    private XFormDialog dialog;
    protected String valuesSettingID;
    private XFormField useCached;
    private T modelItem;

    // Configure behavior of this action:
    private boolean fixedWSDL = false;
    private Action toolsSettingsAction = new ShowIntegratedToolsSettingsAction();
    ;

    public AbstractToolsAction(String name, String description) {
        super(name, description);
    }

    public XFormDialog getDialog() {
        return dialog;
    }

    public String getValuesSettingID() {
        return valuesSettingID;
    }

    public void setValuesSettingID(String valuesSettingID) {
        this.valuesSettingID = valuesSettingID;
    }

    /**
     * Set this to true to not let the user edit the WSDL.
     *
     * @param b
     */
    public void setFixedWSDL(boolean b) {
        this.fixedWSDL = b;
    }

    public T getModelItem() {
        return modelItem;
    }

    public void perform(T target, Object param) {
        this.valuesSettingID = this.getClass().getName() + "@values";
        if (target == null) {
            this.valuesSettingID += "-global";
        } else {
            this.valuesSettingID += "-local";
        }

        modelItem = target;

        // Could reuse the dialog in Swing, but not in Eclipse.
        // if( dialog == null )
        dialog = buildDialog((T) target);

        if (dialog == null) {
            try {
                generate(initValues((T) target, param), UISupport.getToolHost(), (T) target);
            } catch (Exception e1) {
                UISupport.showErrorMessage(e1);
            }
        } else {
            StringToStringMap values = initValues((T) target, param);

            dialog.setValues(values);
            dialog.setVisible(true);
        }
    }

    /**
     * Perform an
     *
     * @param target
     * @param param
     */
    public void performHeadless(T target, Object param) {
        this.valuesSettingID = this.getClass().getName() + "@values";
        if (target == null) {
            this.valuesSettingID += "-global";
        } else {
            this.valuesSettingID += "-local";
        }

        modelItem = target;

        try {
            generate(initValues((T) target, param), UISupport.getToolHost(), (T) target);
        } catch (Exception e1) {
            UISupport.showErrorMessage(e1);
        }
    }

    protected StringToStringMap initValues(T modelItem, Object param) {
        String settingValues = modelItem == null ? SoapUI.getSettings().getString(valuesSettingID, null) : modelItem
                .getSettings().getString(valuesSettingID, null);

        StringToStringMap result = settingValues == null ? new StringToStringMap() : StringToStringMap
                .fromXml(settingValues);

        if (modelItem instanceof WsdlInterface) {
            initWSDL(result, (WsdlInterface) modelItem);
        }

        if (dialog != null && modelItem != null) {
            String projectRoot = modelItem.getSettings().getString(ProjectSettings.PROJECT_ROOT, null);
            if (projectRoot != null) {
                dialog.setFormFieldProperty(ProjectSettings.PROJECT_ROOT, projectRoot);
            }
        }

        return result;
    }

    protected XFormDialog buildDialog(T modelItem) {
        return null;
    }

    protected void addWSDLFields(XForm mainForm, T modelItem) {
        if (!fixedWSDL) {
            XFormTextField tf = mainForm.addTextField(WSDL, "url to wsdl", XForm.FieldType.URL);

            if (modelItem instanceof Interface) {
                useCached = mainForm.addCheckBox(CACHED_WSDL, null);
                useCached.addComponentEnabler(tf, "false");
            }
        } else {
            if (modelItem instanceof Interface) {
                useCached = mainForm.addCheckBox(CACHED_WSDL, null);
            }
        }
    }

    protected void initWSDL(StringToStringMap values, WsdlInterface iface) {
        boolean cached = iface.isCached();
        if (useCached != null) {
            useCached.setEnabled(cached);
        }

        if (!values.containsKey(CACHED_WSDL)) {
            values.put(CACHED_WSDL, Boolean.toString(cached));
        }

        if (values.getBoolean(CACHED_WSDL) || !values.hasValue(WSDL)) {
            values.put(WSDL, PathUtils.expandPath(iface.getDefinition(), iface));
        }
    }

    protected abstract void generate(StringToStringMap values, ToolHost toolHost, T modelItem) throws Exception;

    public void run(ToolHost toolHost, T modelItem, Object param) throws Exception {
        generate(initValues(modelItem, param), toolHost, modelItem);
    }

    /**
     * To be overridden..
     */

    public void onClose(T modelItem) {
        if (dialog == null) {
            return;
        }

        if (modelItem == null) {
            SoapUI.getSettings().setString(valuesSettingID, dialog.getValues().toXml());
        } else {
            modelItem.getSettings().setString(valuesSettingID, dialog.getValues().toXml());
        }
    }

    protected String getWsdlUrl(StringToStringMap values, T modelItem) {
        String wsdl = values.get(WSDL);
        boolean useCached = values.getBoolean(CACHED_WSDL);

        if (modelItem instanceof AbstractInterface) {
            AbstractInterface<?> iface = (AbstractInterface<?>) modelItem;

            boolean hasDefinition = StringUtils.hasContent(iface.getDefinition());
            if (wsdl == null && !useCached && hasDefinition) {
                return PathUtils.expandPath(iface.getDefinition(), iface);
            }

            if (!hasDefinition || (useCached && iface.getDefinitionContext().isCached())) {
                try {
                    File tempFile = File.createTempFile("tempdir", null);
                    String path = tempFile.getAbsolutePath();
                    tempFile.delete();
                    wsdl = iface.getDefinitionContext().export(path);

                    // CachedWsdlLoader loader = (CachedWsdlLoader)
                    // iface.createWsdlLoader();
                    // wsdl = loader.saveDefinition(path);
                } catch (Exception e) {
                    SoapUI.logError(e);
                }
            }
        }

        return wsdl;
    }

    protected String buildClasspath(File jarDir) {
        String[] jars = jarDir.list(new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });

        StringBuilder classpath = new StringBuilder();

        for (int c = 0; c < jars.length; c++) {
            if (c > 0) {
                classpath.append(File.pathSeparatorChar);
            }

            classpath.append(jars[c]);
        }
        return classpath.toString();
    }

    protected ActionList buildDefaultActions(String helpUrl, T modelItem) {
        ActionList actions = new DefaultActionList("Actions");

        if (helpUrl != null) {
            actions.addAction(new ShowOnlineHelpAction(helpUrl));
            actions.addSeparator();
        }

        Action runAction = createRunOption(modelItem);
        actions.addAction(runAction);
        actions.setDefaultAction(runAction);
        actions.addAction(new CloseAction(modelItem));

        if (toolsSettingsAction != null) {
            actions.addAction(toolsSettingsAction);
        }

        return actions;
    }

    public Action getToolsSettingsAction() {
        return toolsSettingsAction;
    }

    public void setToolsSettingsAction(Action toolsSettingsAction) {
        this.toolsSettingsAction = toolsSettingsAction;
    }

    protected Action createRunOption(T modelItem) {
        return new GenerateAction(modelItem);
    }

    protected String getDefinition(T modelItem) {
        if (modelItem == null) {
            return "";
        }
        WsdlInterface iface = (WsdlInterface) modelItem;
        String definition = PathUtils.expandPath(iface.getDefinition(), iface);
        if (definition.startsWith("file:")) {
            definition = definition.substring(5);
        }

        return definition;
    }

    protected void addJavaArgs(StringToStringMap values, ArgumentBuilder builder) {
        String[] javaArgs = Tools.tokenizeArgs(values.get(JAVA_ARGS));
        if (javaArgs != null) {
            builder.addArgs(javaArgs);
        }
    }

    protected void addToolArgs(StringToStringMap values, ArgumentBuilder builder) {
        String[] toolArgs = Tools.tokenizeArgs(values.get(TOOL_ARGS));
        if (toolArgs != null) {
            builder.addArgs(toolArgs);
        }
    }

    protected XForm buildArgsForm(XFormDialogBuilder builder, boolean addJavaArgs, String toolName) {
        XForm argsForm = builder.createForm("Custom Args");
        if (addJavaArgs) {
            argsForm.addTextField(JAVA_ARGS, "additional arguments to java", XForm.FieldType.TEXT);
        }

        argsForm.addTextField(TOOL_ARGS, "additional arguments to " + toolName, XForm.FieldType.TEXT);
        return argsForm;
    }

    public static final class ShowIntegratedToolsSettingsAction extends AbstractAction {
        public ShowIntegratedToolsSettingsAction() {
            super("Tools");
        }

        public void actionPerformed(ActionEvent e) {
            SoapUIPreferencesAction.getInstance().show(SoapUIPreferencesAction.INTEGRATED_TOOLS);
        }
    }

    protected final class CloseAction extends AbstractAction {
        private final T modelItem;

        public CloseAction(T modelItem) {
            super("Close");
            this.modelItem = modelItem;
        }

        public void actionPerformed(ActionEvent e) {
            closeDialog(modelItem);
        }
    }

    public void closeDialog(T modelItem) {
        onClose(modelItem);
        if (dialog != null) {
            dialog.setVisible(false);
        }
    }

    protected final class GenerateAction extends AbstractAction {
        private final T modelItem;

        public GenerateAction(T modelItem) {
            super("Generate");
            this.modelItem = modelItem;
        }

        public void actionPerformed(ActionEvent e) {
            try {
                if (dialog.validate()) {
                    generate(dialog.getValues(), UISupport.getToolHost(), modelItem);
                }
            } catch (Exception e1) {
                UISupport.showErrorMessage(e1);
            }
        }
    }
}
