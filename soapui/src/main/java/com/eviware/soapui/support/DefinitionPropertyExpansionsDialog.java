package com.eviware.soapui.support;

import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.DefaultActionList;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormDialogBuilder;
import com.eviware.x.form.XFormFactory;
import com.eviware.x.form.XFormTextField;
import com.eviware.x.impl.swing.JTextAreaFormField;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.util.List;

public class DefinitionPropertyExpansionsDialog {
    public static final MessageSupport messages = MessageSupport.getMessages(DefinitionPropertyExpansionsDialog.class);

    private XFormDialog dialog = null;
    private boolean importDefinition;

    public boolean isImportDefinition() {
        return importDefinition;
    }

    private ActionList buildActions() {
        ActionList actions = new DefaultActionList("Actions");
        actions.addAction(new ImportAction());
        actions.addAction(new CancelAction());
        return actions;
    }

    private XFormDialog buildDialog(List<String> propertyExpansions) {
        XFormDialogBuilder builder = XFormFactory.createDialogBuilder(messages.get(
                "DefinitionPropertyExpansionsDialog.Title"));
        XForm form = builder.createForm("Basic");
        XFormTextField vulnerabilitiesTextField = form.addTextField(
                messages.get("DefinitionPropertyExpansionsDialog.Field.Vulnerabilities.Name"),
                messages.get("DefinitionPropertyExpansionsDialog.Field.Vulnerabilities.Description"),
                XForm.FieldType.TEXTAREA);
        StringBuilder propertyExpansionsBuilder = new StringBuilder();
        for (String propertyExpansion : propertyExpansions) {
            propertyExpansionsBuilder.append(propertyExpansion).append("\n");
        }
        if (propertyExpansionsBuilder.length() > 0) {
            propertyExpansionsBuilder.deleteCharAt(propertyExpansionsBuilder.length() - 1);
        }
        vulnerabilitiesTextField.setValue(propertyExpansionsBuilder.toString());
        ((JTextAreaFormField) vulnerabilitiesTextField).setEditable(false);
        dialog = builder.buildDialog(buildActions(),
                messages.get("DefinitionPropertyExpansionsDialog.Description"),
                UISupport.createImageIcon("/warning_32x32.png"));
        return dialog;
    }

    public void showDialog(List<String> propertyExpansions) {
        if (dialog == null) {
            dialog = buildDialog(propertyExpansions);
        }
        dialog.show();
    }

    private void closeDialog() {
        dialog.setVisible(false);
        dialog = null;
    }

    private class ImportAction extends AbstractAction {

        public ImportAction() {
            super(messages.get("DefinitionPropertyExpansionsDialog.Action.Import.Name"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            importDefinition = true;
            closeDialog();
        }
    }

    private class CancelAction extends AbstractAction {

        public CancelAction() {
            super(messages.get("DefinitionPropertyExpansionsDialog.Action.Cancel.Name"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            importDefinition = false;
            closeDialog();
        }
    }
}
