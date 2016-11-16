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

package com.eviware.soapui.support.action;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.ActionMappingPositionTypeConfig;
import com.eviware.soapui.config.SoapUIActionConfig;
import com.eviware.soapui.config.SoapUIActionGroupConfig;
import com.eviware.soapui.config.SoapUIActionMappingConfig;
import com.eviware.soapui.config.SoapUIActionsConfig;
import com.eviware.soapui.config.SoapuiActionsDocumentConfig;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.soapui.support.action.support.DefaultActionMapping;
import com.eviware.soapui.support.action.support.DefaultSoapUIActionGroup;
import com.eviware.soapui.support.action.support.StandaloneActionMapping;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Global SoapUIAction Registry
 *
 * @author ole.matzura
 */

@SuppressWarnings("unchecked")
public class SoapUIActionRegistry {
    private Map<String, SoapUIAction> actions = new HashMap<String, SoapUIAction>();
    private Map<String, SoapUIActionGroup> actionGroups = new HashMap<String, SoapUIActionGroup>();

    public void addAction(String soapuiActionID, SoapUIAction action) {
        actions.put(soapuiActionID, action);
    }

    public SoapUIActionGroup findGroupWithClass(Class<? extends SoapUIActionGroup> aClass) {
        for (SoapUIActionGroup soapUIActionGroup : actionGroups.values()) {
            if (soapUIActionGroup.getClass().equals(aClass)) {
                return soapUIActionGroup;
            }
        }
        throw new IllegalArgumentException("Action group not found for class " + aClass);
    }

    public void removeAction(String soapuiActionID) {
        actions.remove(soapuiActionID);
    }


    public static class SeperatorAction extends AbstractSoapUIAction {
        public static final String SOAPUI_ACTION_ID = "SeperatorAction";
        public static SeperatorAction INSTANCE = new SeperatorAction();
        private static SoapUIActionMapping defaultMapping = new DefaultActionMapping(SeperatorAction.SOAPUI_ACTION_ID,
                null, null, false, null);

        public SeperatorAction() {
            super(null, null);
        }

        public void perform(ModelItem target, Object param) {
        }

        public static SoapUIActionMapping getDefaultMapping() {
            return defaultMapping;
        }
    }

    public static class SoapUIActionGroupAction<T extends ModelItem> extends AbstractSoapUIAction<T> {
        private SoapUIActionGroup actionGroup;
        private final String actionGroupId;
        private boolean insert;

        public SoapUIActionGroupAction(String name, String description, String actionGroupId) {
            super(name, description);
            this.actionGroupId = actionGroupId;
        }

        public SoapUIActionGroup getActionGroup() {
            if (actionGroup == null) {
                actionGroup = SoapUI.getActionRegistry().getActionGroup(actionGroupId);
            }
            return actionGroup;
        }

        public void perform(T target, Object param) {
            SoapUIActionGroup group = getActionGroup();
            List<SoapUIActionMapping<T>> mappings = group.getActionMappings(target);
            for (SoapUIActionMapping<T> mapping : mappings) {
                if (mapping.isDefault()) {
                    mapping.getAction().perform(target, param);
                }
            }
        }

        public void setInsert(boolean insert) {
            this.insert = insert;
        }

        public boolean isInsert() {
            return insert;
        }
    }

    public SoapUIAction getAction(String soapUIActionId) {
        SoapUIAction soapUIAction = actions.get(soapUIActionId);
        if (soapUIAction == null) {
            System.err.println("Missing action [" + soapUIActionId + "]");
        }
        return soapUIAction;
    }

    public SoapUIActionRegistry(InputStream config) {
        // default actions
        addAction(SeperatorAction.SOAPUI_ACTION_ID, SeperatorAction.INSTANCE);

        if (config != null) {
            addConfig(config, SoapUI.class.getClassLoader());
        }
    }

    public void addConfig(InputStream config, ClassLoader classLoader) {
        try {
            SoapuiActionsDocumentConfig configDocument = SoapuiActionsDocumentConfig.Factory.parse(config);
            SoapUIActionsConfig soapuiActions = configDocument.getSoapuiActions();

            for (SoapUIActionConfig action : soapuiActions.getActionList()) {
                try {
                    String id = action.getId();
                    Class<?> actionClass = Class.forName(action.getActionClass(), true, classLoader);

                    addAction(id, (SoapUIAction) actionClass.newInstance());
                } catch (Exception e) {
                    SoapUI.logError(e);
                    e.printStackTrace();
                }
            }

            for (SoapUIActionGroupConfig group : soapuiActions.getActionGroupList()) {
                SoapUIActionGroup actionGroup;

                // modify existing?
                String groupId = group.getId();
                if (actionGroups.containsKey(groupId)) {
                    actionGroup = actionGroups.get(groupId);

                    if (group.isSetClass1()) {
                        actionGroup = createActionGroupClassFromConfig(group);
                        addActionGroup(actionGroup, groupId);
                    }

                    addMappings(actionGroup, group);
                } else {
                    if (group.isSetClass1()) {
                        actionGroup = createActionGroupClassFromConfig(group);
                    } else {
                        actionGroup = new DefaultSoapUIActionGroup(groupId, group.getName());
                    }

                    addMappings(actionGroup, group);
                    addActionGroup(actionGroup, groupId);
                }
            }
        } catch (Exception e) {
            SoapUI.logError(e);
        } finally {
            try {
                config.close();
            } catch (IOException e) {
                SoapUI.logError(e);
            }
        }
    }

    // package protected to facilitate unit testing
    SoapUIActionGroup addActionGroup(SoapUIActionGroup actionGroup, String groupId) {
        return actionGroups.put(groupId, actionGroup);
    }

    private SoapUIActionGroup createActionGroupClassFromConfig(SoapUIActionGroupConfig group)
            throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException,
            InvocationTargetException {
        SoapUIActionGroup actionGroup;
        Class<SoapUIActionGroup> actionGroupClass = (Class<SoapUIActionGroup>) Class.forName(group.getClass1());

        Constructor<SoapUIActionGroup> constructor = actionGroupClass.getConstructor(new Class[]{String.class,
                String.class});
        if (constructor != null) {
            actionGroup = constructor.newInstance(group.getId(), group.getName());
        } else {
            actionGroup = actionGroupClass.newInstance();
        }
        return actionGroup;
    }

    private void addMappings(SoapUIActionGroup actionGroup, SoapUIActionGroupConfig groupConfig) {
        for (SoapUIActionMappingConfig mapping : groupConfig.getActionMappingList()) {
            try {
                int insertIndex = -1;
                if (mapping.isSetPosition() && mapping.isSetPositionRef()) {
                    insertIndex = actionGroup.getMappingIndex(mapping.getPositionRef());
                    if (mapping.getPosition() == ActionMappingPositionTypeConfig.AFTER) {
                        insertIndex++;
                    }
                }

                if (mapping.isSetGroupId()) {
                    SoapUIActionGroupAction actionListAction = new SoapUIActionGroupAction(mapping.getName(),
                            mapping.getDescription(), mapping.getGroupId());
                    StandaloneActionMapping actionMapping = new StandaloneActionMapping(actionListAction);

                    actionGroup.addMapping(mapping.getGroupId(), insertIndex, actionMapping);

                    if (mapping.isSetName()) {
                        actionMapping.setName(mapping.getName());
                    }

                    if (mapping.isSetDescription()) {
                        actionMapping.setDescription(mapping.getDescription());
                    }
                } else if (mapping.getActionId().equals(SeperatorAction.SOAPUI_ACTION_ID)) {
                    actionGroup.addMapping(SeperatorAction.SOAPUI_ACTION_ID, insertIndex,
                            SeperatorAction.getDefaultMapping());
                } else {
                    DefaultActionMapping actionMapping = new DefaultActionMapping(mapping.getActionId(),
                            mapping.getKeyStroke(), mapping.getIconPath(), mapping.getActionId().equals(
                            groupConfig.getDefault()), mapping.getParam());
                    actionGroup.addMapping(mapping.getActionId(), insertIndex, actionMapping);

                    if (mapping.isSetName()) {
                        actionMapping.setName(mapping.getName());
                    }

                    if (mapping.isSetDescription()) {
                        actionMapping.setDescription(mapping.getDescription());
                    }
                }
            } catch (Exception e) {
                System.err.println("Error initializing ActionMapping: " + e);
                SoapUI.logError(e);
            }
        }
    }

    public <T extends ModelItem> SoapUIActionGroup<T> getActionGroup(String groupId) {
        return actionGroups.get(groupId);
    }

    public void performAction(String soapUIActionId, ModelItem modelItem, Object param) {
        SoapUIAction<ModelItem> action = getAction(soapUIActionId);
        if (action != null) {
            action.perform(modelItem, param);
        }
    }

    public SoapUIActionGroup addActionGroup(SoapUIActionGroup actionGroup) {
        return addActionGroup(actionGroup, actionGroup.getId());
    }
}
