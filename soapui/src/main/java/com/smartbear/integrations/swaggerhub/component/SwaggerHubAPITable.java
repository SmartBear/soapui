package com.smartbear.integrations.swaggerhub.component;

import com.smartbear.integrations.swaggerhub.engine.ApiDescriptor;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class SwaggerHubAPITable extends TableView {
    private ObservableList<SwaggerHubAPITableModel> tableModels = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());

    public SwaggerHubAPITable() {
        configure();
    }

    private void configure() {
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        getStyleClass().add("text-12px");

        TableColumn name = new TableColumn("Name");
        name.setCellValueFactory(new PropertyValueFactory<SwaggerHubAPITableModel, String>("name"));

        TableColumn desc = new TableColumn("Description");
        desc.setCellValueFactory(new PropertyValueFactory<SwaggerHubAPITableModel, String>("descr"));

        TableColumn oasVersion = new TableColumn("OAS Version");
        oasVersion.setCellValueFactory(new PropertyValueFactory<SwaggerHubAPITableModel, String>("oasVersion"));

        TableColumn visibility = new TableColumn("Visibility");
        visibility.setCellValueFactory(new PropertyValueFactory<SwaggerHubAPITableModel, String>("visibility"));

        TableColumn owner = new TableColumn("Owner");
        owner.setCellValueFactory(new PropertyValueFactory<SwaggerHubAPITableModel, String>("owner"));

        TableColumn versions = new TableColumn("Versions");
        versions.setCellValueFactory(new PropertyValueFactory<SwaggerHubAPITableModel, Object>("versions"));

        setItems(tableModels);
        getColumns().addAll(name, owner, desc, oasVersion, visibility, versions);

        setPlaceholder(new Label("No content in the table"));
    }

    public void clearTable() {
        tableModels.clear();
    }

    public void addDescription(ApiDescriptor descriptor) {
        tableModels.add(new SwaggerHubAPITableModel(descriptor));
    }
}
