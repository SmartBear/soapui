package com.smartbear.integrations.swaggerhub;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.Util.SoapUITools;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.support.http.HttpClientSupport;
import com.eviware.soapui.model.workspace.Workspace;
import com.eviware.soapui.support.UISupport;
import com.google.common.io.ByteStreams;
//import com.smartbear.integrations.swaggerhub.importers.Swagger2Importer;
//import com.smartbear.integrations.swaggerhub.importers.OpenAPI3Importer;
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
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.smartbear.integrations.swaggerhub.Utils.getApiKey;
import static javafx.scene.control.Alert.AlertType.ERROR;
import static javafx.scene.control.Alert.AlertType.WARNING;

public class ImportFromHubDialog extends Dialog {
    public static final Logger log = LoggerFactory.getLogger(ImportFromHubDialog.class);
    public static final String SWAGGER_HUB_LOGIN = "SwaggerHubLogin";
    public static final String SWAGGER_HUB_PASSWORD = "SwaggerHubPassword";
    public static final String GETTING_API_KEY_ERROR = "Cannot retrieve an API Key. Please check your credentials";
    //private static final String PRODUCT_ICON_PATH = UISupport.getImageResourceUrl("/ready-api-icon-16.png").toString();
    private static final String GETTING_LIST_OF_DEFINITIONS_ERROR = "Cannot get list of definitions from SwaggerHub";
    private static final int CONTENT_PANE_WIDTH = 710;
    private static final int CONTENT_PANE_HEIGHT = 525;
    private static final String SEARCH_LIMIT = "50";
    private final TextField loginField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final TextField searchField = new TextField();
    private final CheckBox searchInMyHub = new CheckBox();
    private final CheckBox rememberCombo = new CheckBox();
    private final SwaggerHubAPITable table = new SwaggerHubAPITable();
    private final Button searchButton = new Button("Search");
    private final StackPane stackPane = new StackPane();
    private final WsdlProject project;
    private GridPane loginForm;
    private String apiKey;


    public ImportFromHubDialog(WsdlProject project) {
        this.project = project;
        buildDialog();
    }


    protected void buildDialog() {
        initModality(Modality.APPLICATION_MODAL);
        //setDialogPane(UISupport.getDialogPaneWithPlatformButtonOrder());
        Scene scene = getDialogPane().getScene();
        Stage stage = (Stage) scene.getWindow();
        //stage.getIcons().add(PRODUCT_ICON);
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
        vBox.getChildren().addAll(buildLoginForm(), new Separator(), buildSearchForm(), stackPane);
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
            String login = loginField.getText();
            String password = passwordField.getText();
            String searchQuery = searchField.getText();

            String uri;

            if (rememberCombo.isSelected()) {
                Workspace workspace = SoapUI.getWorkspace();
                workspace.getSettings().setString(SWAGGER_HUB_LOGIN, loginField.getText());
                workspace.getSettings().setString(SWAGGER_HUB_PASSWORD, passwordField.getText());
            }

            if (StringUtils.isNotEmpty(login) && StringUtils.isNotEmpty(password)) {
                try {
                    apiKey = getApiKey(login, password);
                    if (searchInMyHub.isSelected()) {
                        uri = PluginConfig.SWAGGERHUB_API + "?filter=user";
                    } else {
                        uri = PluginConfig.SWAGGERHUB_API + "?limit=" + SEARCH_LIMIT;
                    }
                } catch (Exception e) {
                    log.error(GETTING_API_KEY_ERROR, e);
                    Platform.runLater(() -> {
                        stackPane.getChildren().remove(progressIndicator);
                        buildAlert(GETTING_API_KEY_ERROR, "Error", ERROR).showAndWait();
                        searchButton.setDisable(false);
                    });
                    return;
                }
                importPrivate = true;
            } else {
                uri = PluginConfig.SWAGGERHUB_API + "?limit=" + SEARCH_LIMIT;
            }

            if (StringUtils.isNotEmpty(searchQuery)) {
                try {
                    uri += "&query=" + URLEncoder.encode(searchQuery.trim(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    log.error(e.getMessage(), e);
                }
            }

            try {
                HttpGet get = new HttpGet(uri);
                if (importPrivate) {
                    get.setHeader("Authorization", apiKey);
                }
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

            SwaggerImporter importer = null;

            if (StringUtils.equals(descriptor.oasVersion, "3.0.0")) {
                //importer = new OpenAPI3Importer(project, "application/json", false);
            } else {
                //importer = new Swagger2Importer(project, "application/json", false);
            }

            int selectedVersion = model.getVersionCombo().getSelectionModel().getSelectedIndex();

            String version = descriptor.versions[selectedVersion];
            if (version.startsWith("*-")) {
                version = version.substring(2).trim();
            } else if (version.startsWith("*") || version.startsWith("-")) {
                version = version.substring(1).trim();
            }

            String selectedVersionUrl = defaultVersionUrl.substring(0, defaultVersionUrl.lastIndexOf('/')) + "/" + version;
            if (descriptor.isPrivate) {
                Collections.addAll(result, importer.importSwagger(selectedVersionUrl, apiKey));
            } else {
                Collections.addAll(result, importer.importSwagger(selectedVersionUrl));
            }
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

    private GridPane buildLoginForm() {
        loginForm = new GridPane();
        loginForm.setVgap(8);
        loginForm.setHgap(8);
        loginForm.setPadding(new Insets(8, 0, 8, 0));

        Label loginLabel = createLabel("Login");
        loginForm.add(loginLabel, 0, 0);
        loginForm.add(loginField, 1, 0);

        Label passwordLabel = createLabel("Password");
        loginForm.add(passwordLabel, 0, 1);
        loginForm.add(passwordField, 1, 1);

        Label rememberLabel = createLabel("Remember me");

        loginForm.add(rememberLabel, 0, 2);
        loginForm.add(rememberCombo, 1, 2);

        rememberCombo.setSelected(true);
        searchInMyHub.setOnAction(event -> {
            if (searchInMyHub.isSelected()) {
                populateList();
            }
        });

        setTooltip("Specify your SwaggerHub login", loginField);
        setTooltip("Specify your SwaggerHub password", passwordField);
        setTooltip("Check to save your login and password in workspace", rememberCombo);

        Workspace workspace = SoapUI.getWorkspace();
        passwordField.setText(workspace.getSettings().getString(SWAGGER_HUB_PASSWORD, ""));
        loginField.setText(workspace.getSettings().getString(SWAGGER_HUB_LOGIN, ""));

        passwordField.setPrefColumnCount(25);
        loginField.setPrefColumnCount(25);

        return loginForm;
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

        Label searchInMyHubLabel = createLabel("My APIs only  ");
        gridPane.add(searchInMyHubLabel, 0, 1);
        gridPane.add(searchInMyHub, 1, 1);

        searchField.setPrefColumnCount(25);
        searchField.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                populateList();
                event.consume();
            }
        });

        setTooltip("Searches on owner, name, swagger.info.title and swagger.info.description of all APIs", searchField);
        setTooltip("Show only those APIs that the authenticated user has access to either as owner or collaborator", searchInMyHub);

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
        //stage.getIcons().add(new Image(PRODUCT_ICON_PATH));

        return alert;
    }
}
