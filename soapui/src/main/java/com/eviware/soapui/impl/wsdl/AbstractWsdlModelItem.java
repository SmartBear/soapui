/*
 * Copyright 2004-2014 SmartBear Software
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

package com.eviware.soapui.impl.wsdl;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.ModelItemConfig;
import com.eviware.soapui.impl.settings.XmlBeansSettingsImpl;
import com.eviware.soapui.impl.wsdl.support.ExternalDependency;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.Releasable;
import com.eviware.soapui.model.support.AbstractModelItem;
import com.eviware.soapui.model.support.AnimatableItem;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.resolver.ResolveContext;

import javax.swing.ImageIcon;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Abstract base class for WSDL-implementation classes
 *
 * @author Ole.Matzura
 */

public abstract class AbstractWsdlModelItem<T extends ModelItemConfig>
        extends AbstractModelItem
        implements Releasable, AnimatableItem {
    private XmlBeansSettingsImpl settings;
    private T config;
    private ImageIcon icon;
    private final ModelItem parent;

    protected AbstractWsdlModelItem(T config, ModelItem parent, String icon) {
        this.parent = parent;
        if (config != null) {
            setConfig(config);
            ensureIdIsSet();
        }

        if (icon != null) {
            this.icon = UISupport.createImageIcon(icon);
        }
    }

    public boolean dependsOn(ModelItem modelItem) {
        return ModelSupport.dependsOn(this, modelItem);
    }

    public ModelItem getParent() {
        return parent;
    }

    public ImageIcon getIcon() {
        return icon;
    }

    @Override
    public void setIcon(ImageIcon icon) {
        if (icon == this.icon) {
            return;
        }

        ImageIcon oldIcon = this.icon;
        this.icon = icon;
        notifyPropertyChanged(ICON_PROPERTY, oldIcon, icon);
    }

    public String getDescription() {
        String description = config.getDescription();
        return StringUtils.hasContent(description) ? description : "";
    }

    public void setDescription(String description) {
        String old = getDescription();
        if (String.valueOf(old).equals(description)) {
            return;
        }

        config.setDescription(description);
        notifyPropertyChanged(DESCRIPTION_PROPERTY, old, description);
    }

    public String getName() {
        return config.getName();
    }

    public void setName(String name) {
        String old = getName();
        name = name.trim();
        config.setName(name);
        notifyPropertyChanged(NAME_PROPERTY, old, name);
    }

    public XmlBeansSettingsImpl getSettings() {
        return settings;
    }

    public T getConfig() {
        return config;
    }

    public void setConfig(T config) {
        this.config = config;

        if (config != null) {
            ensureIdIsSet();
            if (config.isSetName()) {
                config.setName(config.getName().trim());
            }
        }

        if (settings != null) {
            settings.release();
        }

        if (!config.isSetSettings()) {
            config.addNewSettings();
        }

        settings = new XmlBeansSettingsImpl(this, parent == null ? SoapUI.getSettings() : parent.getSettings(),
                this.config.getSettings());
    }

    public String getId() {
        return config.getId();
    }

    private void ensureIdIsSet() {
        if (!config.isSetId()) {
            config.setId(ModelSupport.generateModelItemID());
        }
    }

    protected void setSettings(XmlBeansSettingsImpl settings) {
        if (this.settings != null) {
            this.settings.release();
        }

        this.settings = settings;
    }

    public ModelItem getWsdlModelItemByName(Collection<? extends ModelItem> items,
                                            String name) {
        for (ModelItem item : items) {
            if (item.getName() != null && item.getName().equals(name)) {
                return item;
            }
        }

        return null;
    }

    public ModelItem getWsdlModelItemById(Collection<? extends ModelItem> items, UUID modelItemId) {
        for (ModelItem item : items) {
            if (item.getId() != null && item.getId().equals(modelItemId.toString())) {
                return item;
            }
        }

        return null;
    }

    public void release() {
        if (settings != null) {
            settings.release();
        }
    }

    public void resolve(ResolveContext<?> context) {
        List<? extends ModelItem> children = getChildren();
        if (children == null) {
            return;
        }

        for (ModelItem modelItem : children) {
            if (modelItem instanceof AbstractWsdlModelItem<?>) {
                ((AbstractWsdlModelItem<?>) modelItem).resolve(context);
            }
        }
    }

    public List<ExternalDependency> getExternalDependencies() {
        List<ExternalDependency> result = new ArrayList<ExternalDependency>();
        addExternalDependencies(result);
        return result;
    }

    protected void addExternalDependencies(List<ExternalDependency> dependencies) {
        List<? extends ModelItem> children = getChildren();
        if (children == null) {
            return;
        }

        for (ModelItem modelItem : children) {
            if (modelItem instanceof AbstractWsdlModelItem<?>) {
                ((AbstractWsdlModelItem<?>) modelItem).addExternalDependencies(dependencies);
            }
        }
    }

    public void beforeSave() {
        List<? extends ModelItem> children = getChildren();
        if (children == null) {
            return;
        }

        for (ModelItem modelItem : children) {
            if (modelItem instanceof AbstractWsdlModelItem<?>) {
                ((AbstractWsdlModelItem<?>) modelItem).beforeSave();
            }
        }
    }

    public void afterLoad() {
        List<? extends ModelItem> children = getChildren();
        if (children == null) {
            return;
        }

        for (ModelItem modelItem : children) {
            if (modelItem instanceof AbstractWsdlModelItem<?>) {
                ((AbstractWsdlModelItem<?>) modelItem).afterLoad();
            }
        }
    }
}
