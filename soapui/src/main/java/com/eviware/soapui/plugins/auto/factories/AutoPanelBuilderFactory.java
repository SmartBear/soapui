package com.eviware.soapui.plugins.auto.factories;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.PanelBuilder;
import com.eviware.soapui.model.util.PanelBuilderFactory;
import com.eviware.soapui.plugins.auto.PluginPanelBuilder;

/**
 * Created by ole on 15/06/14.
 */
public class AutoPanelBuilderFactory extends SimpleSoapUIFactory<PanelBuilder> implements PanelBuilderFactory {
    private Class<? extends ModelItem> targetModelItem;

    public AutoPanelBuilderFactory(PluginPanelBuilder annotation, Class<PanelBuilder> panelBuilderClass) {
        super(PanelBuilderFactory.class, panelBuilderClass);
        targetModelItem = annotation.targetModelItem();
    }

    @Override
    public PanelBuilder createPanelBuilder() {
        return create();
    }

    @Override
    public Class getTargetModelItem() {
        return targetModelItem;
    }
}
