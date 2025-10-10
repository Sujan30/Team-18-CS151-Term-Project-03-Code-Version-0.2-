package cs151.application;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

/**
 * Controller for the home view. Provides navigation into the programming language definition flow.
 */
public class MainController {

    @FXML
    private Button defineLanguageButton;

    /**
     * Loads the Define Programming Languages page on the current stage.
     *
     * @throws IOException if the FXML for the destination page cannot be loaded
     */
    @FXML
    protected void onDefineProgrammingLanguage() throws IOException {
        Stage currentStage = (Stage) defineLanguageButton.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("define-language-view.fxml"));
        Scene scene = new Scene(loader.load(), 720, 540);
        currentStage.setScene(scene);
        currentStage.setTitle("Define Programming Language");
    }
}