package cs151.application;

import java.io.IOException;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Controller for the home view. Provides navigation into the programming language definition flow.
 */
public class MainController {

    @FXML
    private Button defineLanguageButton;

    @FXML
    private Button defineStudentProfilesButton;

    @FXML
    private VBox homeRoot;

    @FXML
    private Button searchStudentProfilesButton;

    @FXML
    private void initialize() {
        Platform.runLater(() -> {
            if (homeRoot != null) {
                homeRoot.requestFocus();
            }
        });
    }

    /**
     * Loads the Define Programming Languages page on the current stage.
     *
     * @throws IOException if the FXML for the destination page cannot be loaded
     */
    @FXML
    protected void onDefineProgrammingLanguage() throws IOException {
        Stage currentStage = (Stage) defineLanguageButton.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("define-language-view.fxml"));
        switchScene(currentStage, loader, "Define Programming Language");
    }

    /**
     * Loads the Define Student Profiles page on the current stage.
     *
     * @throws IOException if the FXML for the destination page cannot be loaded
     */
    @FXML
    protected void onDefineStudentProfiles() throws IOException {
        Stage currentStage = (Stage) defineStudentProfilesButton.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("student-profile-view.fxml"));
        switchScene(currentStage, loader, "Define Student Profiles");
    }

    @FXML
    protected void onSearchStudentProfiles() throws IOException {
        Stage currentStage = (Stage) searchStudentProfilesButton.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("search-student-profile-view.fxml"));
        switchScene(currentStage, loader, "Search Student Profiles");
    }

    private void switchScene(Stage stage, FXMLLoader loader, String title) throws IOException {
        Parent view = loader.load();
        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(view);
            stage.setScene(scene);
        } else {
            scene.setRoot(view);
        }
        stage.setTitle(title);
    }
}