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

package com.eviware.x.form.support;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XForm.FieldType;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormDialogBuilder;
import com.eviware.x.form.XFormFactory;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormTextField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.impl.swing.ActionFormFieldComponent;
import com.eviware.x.impl.swing.JComponentFormField;
import com.eviware.x.impl.swing.JLabelFormField;
import com.eviware.x.impl.swing.JMultilineLabelTextField;
import com.eviware.x.impl.swing.JPasswordFieldFormField;
import com.eviware.x.impl.swing.JStringListFormField;
import com.eviware.x.impl.swing.JTableFormField;
import com.jgoodies.forms.layout.FormLayout;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Field;

/**
 * Builds XFormDialogs from AForm/AField annotated classes/interfaces
 *
 * @author ole.matzura
 */

public class ADialogBuilder {
    public static XFormDialog buildDialog(Class<? extends Object> formClass) {
        return buildDialog(formClass, null, null);
    }

    public static XFormDialog buildDialog(Class<? extends Object> formClass, ActionList actions) {
        return buildDialog(formClass, actions, null);
    }

    public static XFormDialog buildDialog(Class<? extends Object> formClass, ActionList actions, FormLayout layout) {
        AForm formAnnotation = formClass.getAnnotation(AForm.class);
        if (formAnnotation == null) {
            throw new RuntimeException("formClass is not annotated correctly..");
        }

        MessageSupport messages = MessageSupport.getMessages(formClass);

        XFormDialogBuilder builder = XFormFactory.createDialogBuilder(messages.get(formAnnotation.name()));
        XForm form = createForm(builder, layout);

        for (Field field : formClass.getFields()) {
            AField fieldAnnotation = field.getAnnotation(AField.class);
            if (fieldAnnotation != null) {
                try {
                    addFormField(form, field, fieldAnnotation, messages);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        ActionList defaultActions = StringUtils.isBlank(formAnnotation.helpUrl()) ? builder.buildOkCancelActions() : builder
                .buildOkCancelHelpActions(formAnnotation.helpUrl());

        if (actions == null) {
            actions = defaultActions;
        } else {
            actions.addActions(defaultActions);
        }

        XFormDialog dialog = builder.buildDialog(actions, messages.get(formAnnotation.description()),
                UISupport.createImageIcon(formAnnotation.icon()));

        return dialog;
    }

    private static XForm createForm(XFormDialogBuilder builder, FormLayout layout) {
        if (layout == null) {
            return builder.createForm("Basic");
        } else {
            return builder.createForm("Basic", layout);
        }
    }

    /**
     * Allow to use custom Ok, Cancel buttons...
     * <p/>
     * This means user have to add control for closing dialog.
     *
     * @param formClass
     * @param actions
     * @param useDefaultOkCancel
     * @return
     */
    public static XFormDialog buildDialog(Class<? extends Object> formClass, ActionList actions,
                                          boolean useDefaultOkCancel) {

        if (useDefaultOkCancel) {
            return buildDialog(formClass, actions);
        }
        AForm formAnnotation = formClass.getAnnotation(AForm.class);
        if (formAnnotation == null) {
            throw new RuntimeException("formClass is not annotated correctly..");
        }

        MessageSupport messages = MessageSupport.getMessages(formClass);

        XFormDialogBuilder builder = XFormFactory.createDialogBuilder(messages.get(formAnnotation.name()));
        XForm form = createForm(builder, null);

        for (Field field : formClass.getFields()) {
            AField fieldAnnotation = field.getAnnotation(AField.class);
            if (fieldAnnotation != null) {
                try {
                    addFormField(form, field, fieldAnnotation, messages);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        ActionList defaultActions = StringUtils.isBlank(formAnnotation.helpUrl()) ? null : builder.buildHelpActions(formAnnotation
                .helpUrl());

        if (actions == null) {
            actions = defaultActions;
        } else {
            // since there is only one action do it like this
            actions.insertAction(defaultActions.getActionAt(0), 0);
        }
        XFormDialog dialog = builder.buildDialog(actions, messages.get(formAnnotation.description()),
                UISupport.createImageIcon(formAnnotation.icon()));

        return dialog;
    }

    public static XFormDialog buildTabbedDialog(Class<? extends Object> tabbedFormClass, ActionList actions) {
        AForm formAnnotation = tabbedFormClass.getAnnotation(AForm.class);
        if (formAnnotation == null) {
            throw new RuntimeException("formClass is not annotated correctly..");
        }

        MessageSupport messages = MessageSupport.getMessages(tabbedFormClass);
        XFormDialogBuilder builder = XFormFactory.createDialogBuilder(formAnnotation.name());

        for (Field field : tabbedFormClass.getFields()) {
            APage pageAnnotation = field.getAnnotation(APage.class);
            if (pageAnnotation != null) {
                buildForm(builder, pageAnnotation.name(), field.getType(), messages);
            }

            AField fieldAnnotation = field.getAnnotation(AField.class);
            if (fieldAnnotation != null) {
                try {
                    Class<?> formClass = Class.forName(fieldAnnotation.description());
                    buildForm(builder, fieldAnnotation.name(), formClass, messages);
                } catch (Exception e) {
                    SoapUI.logError(e);
                }
            }
        }

        ActionList defaultActions = StringUtils.isBlank(formAnnotation.helpUrl()) ? builder.buildOkCancelActions() : builder
                .buildOkCancelHelpActions(formAnnotation.helpUrl());

        if (actions == null) {
            actions = defaultActions;
        } else {
            actions.addActions(defaultActions);
        }

        XFormDialog dialog = builder.buildDialog(actions, formAnnotation.description(),
                UISupport.createImageIcon(formAnnotation.icon()));

        return dialog;
    }

    public static XFormDialog buildTabbedDialogWithCustomActions(Class<? extends Object> tabbedFormClass,
                                                                 ActionList actions) {
        AForm formAnnotation = tabbedFormClass.getAnnotation(AForm.class);
        if (formAnnotation == null) {
            throw new RuntimeException("formClass is not annotated correctly..");
        }

        MessageSupport messages = MessageSupport.getMessages(tabbedFormClass);
        XFormDialogBuilder builder = XFormFactory.createDialogBuilder(formAnnotation.name());

        for (Field field : tabbedFormClass.getFields()) {
            APage pageAnnotation = field.getAnnotation(APage.class);
            if (pageAnnotation != null) {
                buildForm(builder, pageAnnotation.name(), field.getType(), messages);
            }

            AField fieldAnnotation = field.getAnnotation(AField.class);
            if (fieldAnnotation != null) {
                try {
                    Class<?> formClass = Class.forName(fieldAnnotation.description());
                    buildForm(builder, fieldAnnotation.name(), formClass, messages);
                } catch (Exception e) {
                    SoapUI.logError(e);
                }
            }
        }

        ActionList defaultActions = StringUtils.isBlank(formAnnotation.helpUrl()) ? null : builder
                .buildHelpActions(formAnnotation.helpUrl());

        if (actions == null) {
            actions = defaultActions;
        } else {
            defaultActions.addActions(actions);
            actions = defaultActions;
        }

        XFormDialog dialog = builder.buildDialog(actions, formAnnotation.description(),
                UISupport.createImageIcon(formAnnotation.icon()));

        return dialog;
    }

    public static XFormDialog buildWizard(Class<? extends Object> tabbedFormClass) {
        AForm formAnnotation = tabbedFormClass.getAnnotation(AForm.class);
        if (formAnnotation == null) {
            throw new RuntimeException("formClass is not annotated correctly..");
        }

        MessageSupport messages = MessageSupport.getMessages(tabbedFormClass);
        XFormDialogBuilder builder = XFormFactory.createDialogBuilder(formAnnotation.name());

        for (Field field : tabbedFormClass.getFields()) {
            APage pageAnnotation = field.getAnnotation(APage.class);
            if (pageAnnotation != null) {
                buildForm(builder, pageAnnotation.name(), field.getType(), messages);
            }
        }

        XFormDialog dialog = builder.buildWizard(formAnnotation.description(),
                UISupport.createImageIcon(formAnnotation.icon()), formAnnotation.helpUrl());

        return dialog;
    }

    private static void buildForm(XFormDialogBuilder builder, String name, Class<?> formClass, MessageSupport messages) {
        XForm form = builder.createForm(name);
        for (Field formField : formClass.getFields()) {
            AField formFieldAnnotation = formField.getAnnotation(AField.class);
            if (formFieldAnnotation != null) {
                try {
                    addFormField(form, formField, formFieldAnnotation, messages);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void addFormField(XForm form, Field formField, AField fieldAnnotation, MessageSupport messages)
            throws Exception {
        AFieldType type = fieldAnnotation.type();
        String fieldName = fieldAnnotation.name();
        String name = messages.get(fieldName.length() == 0 ? formField.get(null).toString() : fieldName);
        String description = messages.get(fieldAnnotation.description());
        String[] values = messages.getArray(fieldAnnotation.values());
        String defaultValue = messages.get(fieldAnnotation.defaultValue());
        boolean enabled = fieldAnnotation.enabled();

        XFormField field = null;
        switch (type) {
            case STRING:
                field = form.addTextField(name, description, FieldType.TEXT);
                break;
            case INT:
                field = form.addTextField(name, description, FieldType.TEXT);
                ((XFormTextField) field).setWidth(10);
                break;
            case STRINGAREA:
                field = form.addTextField(name, description, FieldType.TEXTAREA);
                break;
            case BOOLEAN:
                field = form.addCheckBox(name, description);
                break;
            case FILE:
                field = form.addTextField(name, description, FieldType.FILE);
                break;
            case FOLDER:
                field = form.addTextField(name, description, FieldType.FOLDER);
                break;
            case FILE_OR_FOLDER:
                field = form.addTextField(name, description, FieldType.FILE_OR_FOLDER);
                break;
            case ENUMERATION:
                field = form.addComboBox(name, values, description);
                break;
            case RADIOGROUP:
                field = form.addComponent(name, new XFormRadioGroup(values));
                break;
            case MULTILIST:
                field = form.addComponent(name, new XFormMultiSelectList(values));
                break;
            case STRINGLIST:
                field = form.addComponent(name, new JStringListFormField(description, defaultValue));
                break;
            case TABLE:
                field = form.addComponent(name, new JTableFormField(description));
                break;
            case ACTION:
                field = form.addComponent(name, new ActionFormFieldComponent(name, description));
                break;
            case COMPONENT:
                field = form.addComponent(name, new JComponentFormField(name, description));
                break;
            case PASSWORD:
                field = form.addComponent(name, new JPasswordFieldFormField());
                break;
            case INFORMATION:
                field = form.addComponent(name, new JMultilineLabelTextField());
                break;
            case LABEL:
                field = form.addComponent(name, new JLabelFormField(description));
                break;
            case SEPARATOR:
                form.addSeparator(description);
                break;
            case RADIOGROUP_TOP_BUTTON:
                field = form.addComponent(name, new XFormRadioGroupTopButtonPosition(values));
                break;
            case COMBOBOX:
                field = form.addComboBox(name, values, description);
                break;
            default:
                System.out.println("Unsupported field type: " + type);
        }

        if (field != null) {
            field.setEnabled(enabled);
        }
    }
}
