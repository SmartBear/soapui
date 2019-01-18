package com.eviware.soapui.impl.rest.actions.explorer.callback;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.support.WorkspaceListenerAdapter;
import com.eviware.soapui.model.workspace.Workspace;
import com.eviware.soapui.support.UISupport;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import javafx.application.Platform;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ModuleStarterPageCallback extends WorkspaceListenerAdapter {

    Logger log = LoggerFactory.getLogger(ModuleStarterPageCallback.class);

    public static String CALLBACK = "moduleStarterPageCallback";

    private Workspace workspace;
    private String moduleId;
    //private Reloadable reloadable;

    public ModuleStarterPageCallback(Workspace workspace, String moduleId) {

        this.workspace = workspace;
        this.moduleId = moduleId;
    }

    public void close() {
        Platform.exit();
    }

    /**
     * This is a JavaScript callback used to get recent modelItems
     */
    public ModelItem[] getRecentItems() {
        ArrayList<ModelItem> items = Lists.newArrayList();


        final Map<String, ModelItem> modelItemsInWorkspace = getChildrenRecursively(workspace);

        //for (String modelItemId : Lists.reverse(workspace.getRecentItems(moduleId))) {
        //   if (modelItemsInWorkspace.containsKey(modelItemId)) {
        //       items.add(modelItemsInWorkspace.get(modelItemId));
        //   }
        //}


        return items.toArray(new ModelItem[items.size()]);
    }

    public String getRecentItemsAsJSON() {
        ModelItem[] items = getRecentItems();

        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < items.length; i++) {
            ModelItem modelItem = items[i];
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", modelItem.getId());
                jsonObject.put("name", modelItem.getName());
                jsonObject.put("icon", modelItem.getIcon().toString());
                jsonArray.put(jsonObject);
            } catch (JSONException json) {
                //Logging.logError(json);
            }
        }
        return jsonArray.toString();
    }

    /**
     * This is a JavaScript callback used to to open modelitems by id
     */
    public void openModelItemWithId(final String Id) {
        log.info("Opening ModelItem with id: {}", Id);

        SwingUtilities.invokeLater(() -> {
            try {
                UISupport.setHourglassCursor();
                getAndShow(Id);
            } finally {
                UISupport.resetCursor();
            }
        });
    }

    private void getAndShow(String modelItemId) {
        final Map<String, ModelItem> items = getChildrenRecursively(workspace);
        if (items.containsKey(modelItemId)) {
            ModelItem modelItem = items.get(modelItemId);
            UISupport.showDesktopPanel(modelItem);
        } else {
            log.warn("Could not open ModelItem with id: {}", modelItemId);
            UISupport.showErrorMessage("Unable to find the requested item in the current workspace");
            reloadPage();
        }
    }

    private void reloadPage() {
        // if (reloadable != null) {
        // reloadable.reload();
        //  }
    }

    private Map<String, ModelItem> getChildrenRecursively(ModelItem modelItem) {
        HashMap<String, ModelItem> items = Maps.newHashMap();
        for (ModelItem item : modelItem.getChildren()) {
            items.put(item.getId(), item);
            items.putAll(getChildrenRecursively(item));
        }
        return items;
    }

    //public void setReloadable(Reloadable reloadable) {
    //   this.reloadable = reloadable;
    //}
}
