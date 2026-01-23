package serverUI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import serverUI.api.AdminUserService;

import java.util.Objects;

public class AdminApp extends Application {

    private static AdminUserService adminService;
    private static serverUI.api.ServerControlService serverControl;
    private static serverUI.api.LogService logService;
    private static serverUI.api.ExportService exportService;

    public static void setAdminService(AdminUserService service) {
        adminService = service;
    }
    public static void setServerControl(serverUI.api.ServerControlService s) { serverControl = s; }
    public static void setLogService(serverUI.api.LogService s) { logService = s; }
    public static void setExportService(serverUI.api.ExportService s) { exportService = s; }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                getClass().getResource("AdminPanel.fxml")
        ));
        Parent root = loader.load();

        AdminPanelController controller = loader.getController();
        controller.setService(adminService);
        controller.setServerControl(serverControl);
        controller.setLogService(logService);
        controller.setExportService(exportService);

        Scene scene = new Scene(root);

        stage.setTitle("Server Admin Panel");
        stage.sizeToScene();
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}