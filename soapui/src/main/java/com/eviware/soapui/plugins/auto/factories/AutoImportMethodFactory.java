package com.eviware.soapui.plugins.auto.factories;

import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.impl.actions.ImportMethod;
import com.eviware.soapui.impl.actions.ImportMethodFactory;
import com.eviware.soapui.plugins.auto.PluginImportMethod;
import com.eviware.soapui.support.action.SoapUIAction;

public class AutoImportMethodFactory extends SimpleSoapUIFactory<SoapUIAction> implements ImportMethodFactory {
    private final String label;

    public AutoImportMethodFactory(PluginImportMethod annotation, Class<SoapUIAction> actionClass) {
        super(ImportMethodFactory.class, actionClass);
        label = annotation.label();
    }

    @Override
    public ImportMethod createNewImportMethod() {
        return new ImportMethod() {
            @Override
            public SoapUIAction<WorkspaceImpl> getImportAction() {
                return create();
            }

            @Override
            public String getLabel() {
                return label;
            }
        };
    }
}