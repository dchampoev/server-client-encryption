package serverUI;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import serverUI.api.AdminUserService;
import serverUI.api.Right;
import serverUI.api.UserInfo;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

public class AdminPanelController {

    // Users tab
    @FXML private TableView<UserRow> tblUsers;
    @FXML private TableColumn<UserRow, String> colUsername;
    @FXML private TableColumn<UserRow, String> colRights;

    @FXML private TextField tfUsername;
    @FXML private PasswordField pfPassword;
    @FXML private CheckBox cbEncrypt;
    @FXML private CheckBox cbDecrypt;
    @FXML private CheckBox cbAdmin;
    @FXML private Label lblUserResult;

    // Top bar + Logs tab
    @FXML private Label lblServerStatus;
    @FXML private Label lblPort;
    @FXML private Label lblClients;

    @FXML private Label lblExportResult;

    @FXML private TextArea taLogs;
    @FXML private CheckBox cbAutoScroll;

    private final ObservableList<UserRow> rows = FXCollections.observableArrayList();

    private AdminUserService service;
    private serverUI.api.ServerControlService serverControl;
    private serverUI.api.LogService logService;

    private Timeline refreshTimer;

    public void setService(AdminUserService service) {
        this.service = service;
        reloadTableFromService(true);
    }

    public void setServerControl(serverUI.api.ServerControlService s) {
        this.serverControl = s;
        refreshServerStatus();
    }

    public void setLogService(serverUI.api.LogService s) {
        this.logService = s;
        refreshLogs();
    }

    @FXML
    public void initialize() {
        colUsername.setCellValueFactory(c -> c.getValue().usernameProperty());
        colRights.setCellValueFactory(c -> c.getValue().rightsProperty());
        tblUsers.setItems(rows);

        tblUsers.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) return;
            tfUsername.setText(newV.getUsername());
            applyRightsStringToCheckboxes(newV.getRights());
        });
        startRefreshTimer();
    }

    // -------- top buttons --------

    @FXML
    private void onStartServer() {
        if (serverControl == null) return;
        serverControl.start();
        refreshServerStatus();
    }

    @FXML
    private void onStopServer() {
        if (serverControl == null) return;
        serverControl.stop();
        refreshServerStatus();
    }

    // -------- users buttons --------

    @FXML
    private void onReloadUsers() {
        reloadTableFromService(true);
        lblUserResult.setText("Users reloaded.");
    }

    @FXML
    private void onSaveUsers() {
        ensureService();
        service.save();
        lblUserResult.setText("Users saved.");
    }

    @FXML
    private void onAddOrSaveUser() {
        ensureService();

        String username = tfUsername.getText().trim();
        String password = pfPassword.getText();

        if (username.isEmpty() || password == null || password.isEmpty()) {
            lblUserResult.setText("Username and password required.");
            return;
        }

        EnumSet<Right> rights = EnumSet.noneOf(Right.class);
        if (cbEncrypt.isSelected()) rights.add(Right.ENCRYPT);
        if (cbDecrypt.isSelected()) rights.add(Right.DECRYPT);
        if (cbAdmin.isSelected()) rights.add(Right.ADMIN);

        service.addOrUpdateUser(username, password, rights);
        service.save();

        reloadTableFromService(false);
        clearForm();
        lblUserResult.setText("User saved.");
    }

    @FXML
    private void onDeleteUser() {
        ensureService();

        UserRow selected = tblUsers.getSelectionModel().getSelectedItem();
        if (selected == null) {
            lblUserResult.setText("Select user first.");
            return;
        }

        String username = selected.getUsername();
        if ("admin".equals(username)) {
            lblUserResult.setText("Cannot delete admin.");
            return;
        }

        boolean ok = service.deleteUser(username);
        if (ok) {
            service.save();
            reloadTableFromService(false);
            lblUserResult.setText("User deleted.");
        } else {
            lblUserResult.setText("User not found.");
        }
    }

    @FXML
    private void onClearForm() {
        clearForm();
    }

    // -------- export buttons --------

    @FXML
    private void onExportByCryptogram() {
        if (lblExportResult != null) lblExportResult.setText("Not implemented yet.");
    }

    @FXML
    private void onExportByCard() {
        if (lblExportResult != null) lblExportResult.setText("Not implemented yet.");
    }

    // -------- logs buttons --------

    @FXML
    private void onClearLogs() {
        if (logService != null) logService.clear();
        refreshLogs();
    }

    // ---------------- helpers ----------------

    private void reloadTableFromService(boolean callReload) {
        ensureService();
        if (callReload) service.reload();

        List<UserInfo> users = service.listUsers();

        rows.clear();
        for (UserInfo u : users) {
            rows.add(new UserRow(u.username, rightsToText(u.rights)));
        }
    }

    private static String rightsToText(EnumSet<Right> rights) {
        if (rights == null || rights.isEmpty()) return "(none)";
        return rights.stream().map(Enum::name).sorted().collect(Collectors.joining(","));
    }

    private void applyRightsStringToCheckboxes(String rightsText) {
        cbEncrypt.setSelected(false);
        cbDecrypt.setSelected(false);
        cbAdmin.setSelected(false);

        if (rightsText == null || rightsText.isBlank() || rightsText.equals("(none)")) return;

        for (String p : rightsText.split(",")) {
            String r = p.trim();
            if (r.equals("ENCRYPT")) cbEncrypt.setSelected(true);
            else if (r.equals("DECRYPT")) cbDecrypt.setSelected(true);
            else if (r.equals("ADMIN")) cbAdmin.setSelected(true);
        }
    }

    private void clearForm() {
        tfUsername.clear();
        pfPassword.clear();
        cbEncrypt.setSelected(false);
        cbDecrypt.setSelected(false);
        cbAdmin.setSelected(false);
    }

    private void ensureService() {
        if (service == null) {
            throw new IllegalStateException("AdminUserService not set. Call controller.setService(...) before showing UI.");
        }
    }

    private void refreshServerStatus() {
        if (serverControl == null) return;

        lblPort.setText(String.valueOf(serverControl.getPort()));
        lblClients.setText(String.valueOf(serverControl.getActiveClients()));

        updateServerStatusVisual();
    }

    private void refreshLogs() {
        if (logService == null || taLogs == null) return;

        taLogs.setText(logService.readAll());

        if (cbAutoScroll != null && cbAutoScroll.isSelected()) {
            taLogs.setScrollTop(Double.MAX_VALUE);
        }
    }

    private void startRefreshTimer() {
        if (refreshTimer != null) return;

        refreshTimer = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    refreshServerStatus();
                    refreshLogs();
                })
        );
        refreshTimer.setCycleCount(Timeline.INDEFINITE);
        refreshTimer.play();
    }
    private void updateServerStatusVisual() {
        if (serverControl == null) return;

        boolean running = serverControl.isRunning();

        lblServerStatus.setText(running ? "RUNNING" : "STOPPED");
        lblServerStatus.setStyle(
                running
                        ? "-fx-text-fill: #2ecc71; -fx-font-weight: bold;"
                        : "-fx-text-fill: #e74c3c; -fx-font-weight: bold;"
        );
    }
}