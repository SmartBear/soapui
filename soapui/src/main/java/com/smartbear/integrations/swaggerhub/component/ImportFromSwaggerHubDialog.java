package com.smartbear.integrations.swaggerhub.component;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.support.http.HttpClientSupport;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.google.common.io.ByteStreams;
import com.smartbear.integrations.swaggerhub.engine.ApiDescriptor;
import com.smartbear.integrations.swaggerhub.engine.ApisJsonImporter;
import com.smartbear.swagger.Swagger2Importer;
import com.smartbear.swagger.SwaggerImporter;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.collections.CollectionUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.smartbear.integrations.swaggerhub.PublishToSwaggerHubAction.SWAGGERHUB_API;
import static javafx.scene.control.Alert.AlertType.ERROR;
import static javafx.scene.control.Alert.AlertType.WARNING;

public class ImportFromSwaggerHubDialog extends Dialog {
    private static final String UNSUPPORTED_OAS_VERSION_MESSAGE = "SoapUI OS can work only with OAS version 2.0";
    public static final Logger log = LoggerFactory.getLogger(ImportFromSwaggerHubDialog.class);
    private static final String GETTING_LIST_OF_DEFINITIONS_ERROR = "Cannot get list of definitions from SwaggerHub";
    private static final int CONTENT_PANE_WIDTH = 710;
    private static final int CONTENT_PANE_HEIGHT = 525;
    private static final String SEARCH_LIMIT = "50";
    private final TextField searchField = new TextField();
    private final SwaggerHubAPITable table = new SwaggerHubAPITable();
    private final Button searchButton = new Button("Search");
    private final StackPane stackPane = new StackPane();
    private final WsdlProject project;

    public ImportFromSwaggerHubDialog(WsdlProject project) {
        this.project = project;
        buildDialog();
    }

    protected void buildDialog() {
        initModality(Modality.APPLICATION_MODAL);
        Scene scene = getDialogPane().getScene();
        Stage stage = (Stage) scene.getWindow();
        setResizable(false);
        setTitle("Import from SwaggerHub");
        String css = this.getClass().getResource("/css/swaggerhub-plugin.css").toExternalForm();
        scene.getStylesheets().add(css);
        createButtons();

        VBox vBox = new VBox();

        BorderPane root = new BorderPane();
        root.setMinSize(CONTENT_PANE_WIDTH, CONTENT_PANE_HEIGHT);
        root.setMaxSize(CONTENT_PANE_WIDTH, CONTENT_PANE_HEIGHT);
        root.setStyle("-fx-background-color: white");

        table.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        BorderPane pane = new BorderPane(table);
        pane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        searchButton.setOnAction(event -> {
            populateList();
        });

        ScrollPane tableScroll = new ScrollPane(pane);
        tableScroll.setFitToHeight(true);
        tableScroll.setFitToWidth(true);
        tableScroll.getStyleClass().add("white-scroll");

        tableScroll.setMaxHeight(CONTENT_PANE_HEIGHT);
        stackPane.getChildren().add(tableScroll);
        vBox.getChildren().addAll(new Separator(), buildSearchForm(), stackPane);
        root.setCenter(vBox);
        getDialogPane().setContent(root);
    }

    private void createButtons() {
        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(okButtonType, cancelButtonType);

        Button okButton = (Button) getDialogPane().lookupButton(okButtonType);
        okButton.getStyleClass().add("ok-button");

        okButton.setOnAction(event -> handleOk());
        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            SwaggerHubAPITableModel model = (SwaggerHubAPITableModel) table.getSelectionModel().getSelectedItem();
            if (model == null) {
                buildAlert("Please select API for import", "Select API", WARNING).showAndWait();
                event.consume();
            }
        });

        Button cancelButton = (Button) getDialogPane().lookupButton(cancelButtonType);
        cancelButton.getStyleClass().add("cancel-button");
    }

    private void populateList() {
        searchButton.setDisable(true);
        SoapUI.getThreadPool().execute(() -> {
            ProgressIndicator progressIndicator = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
            progressIndicator.setMaxHeight(table.getHeight() * 0.7);
            progressIndicator.setMaxWidth(table.getWidth() * 0.7);
            Platform.runLater(() -> {
                stackPane.getChildren().add(progressIndicator);
                table.clearTable();
            });
            boolean importPrivate = false;
            String searchQuery = searchField.getText();

            String uri = SWAGGERHUB_API + "?limit=" + SEARCH_LIMIT;

            if (StringUtils.hasContent(searchQuery)) {
                try {
                    uri += "&query=" + URLEncoder.encode(searchQuery.trim(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    log.error(e.getMessage(), e);
                }
            }

            try {
                HttpGet get = new HttpGet(uri);
                HttpResponse response = HttpClientSupport.getHttpClient().execute(get);

                List<ApiDescriptor> descriptors = new ApisJsonImporter().importApis(
                        new String(ByteStreams.toByteArray(response.getEntity().getContent())));

                Platform.runLater(() -> {
                    for (ApiDescriptor descriptor : descriptors) {
                        table.addDescription(descriptor);
                    }
                });
            } catch (Exception e) {
                log.error(GETTING_LIST_OF_DEFINITIONS_ERROR, e);
                Platform.runLater(() -> buildAlert(GETTING_LIST_OF_DEFINITIONS_ERROR, "Error", ERROR).showAndWait());
            } finally {
                Platform.runLater(() -> {
                    stackPane.getChildren().remove(progressIndicator);
                    searchButton.setDisable(false);
                });
            }
        });
    }

    private List<RestService> importApis() {
        List<RestService> result = new ArrayList<>();
        try {
            SwaggerHubAPITableModel model = (SwaggerHubAPITableModel) table.getSelectionModel().getSelectedItem();
            ApiDescriptor descriptor = model.getDescriptor();
            String defaultVersionUrl = descriptor.swaggerUrl;

            SwaggerImporter importer = new Swagger2Importer(project, "application/json");

            int selectedVersion = model.getVersionCombo().getSelectionModel().getSelectedIndex();

            String oasVersion = descriptor.oasVersion;
            if (StringUtils.hasContent(oasVersion) && !oasVersion.startsWith("2.0")) {
                log.error(UNSUPPORTED_OAS_VERSION_MESSAGE);
                UISupport.showErrorMessage(UNSUPPORTED_OAS_VERSION_MESSAGE);
                return result;
            }

            String version = descriptor.versions[selectedVersion];
            if (version.startsWith("*-")) {
                version = version.substring(2).trim();
            } else if (version.startsWith("*") || version.startsWith("-")) {
                version = version.substring(1).trim();
            }

            String selectedVersionUrl = defaultVersionUrl.substring(0, defaultVersionUrl.lastIndexOf('/')) + "/" + version;
            Collections.addAll(result, importer.importSwagger(selectedVersionUrl));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    private void handleOk() {
        SoapUI.getThreadPool().execute(() -> {
            List<RestService> result = importApis();
            if (CollectionUtils.isNotEmpty(result)) {
                UISupport.selectAndShow(result.get(0));
                close();
            }
        });
    }

    private GridPane buildSearchForm() {
        GridPane gridPane = new GridPane();
        gridPane.setVgap(8);
        gridPane.setHgap(8);
        gridPane.setPadding(new Insets(8, 0, 8, 0));

        Label searchLabel = new Label("Search");
        gridPane.add(searchLabel, 0, 0);
        gridPane.add(searchField, 1, 0);
        gridPane.add(searchButton, 2, 0);

        searchField.setPrefColumnCount(25);
        searchField.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                populateList();
                event.consume();
            }
        });

        setTooltip("Searches on owner, name, swagger.info.title and swagger.info.description of all APIs", searchField);
        return gridPane;
    }

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("default-text");
        return label;
    }

    private void setTooltip(String text, Control component) {
        Tooltip tooltip = new Tooltip(text);
        component.setTooltip(tooltip);
    }

    private Alert buildAlert(String text, String title, AlertType alertType) {
        Alert alert = new Alert(alertType, text, ButtonType.OK);
        alert.setHeaderText(title);
        alert.setTitle(title);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white");
        dialogPane.getStyleClass().add("default-text");
        Stage stage = (Stage) dialogPane.getScene().getWindow();

        return alert;
    }
}
