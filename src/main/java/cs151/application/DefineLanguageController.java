package cs151.application;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;


public class DefineLanguageController {

    @FXML
    private TextField nameField;

    @FXML
    private Label feedbackLabel;

    /**
     * Validates and saves the programming language definition.
     * <p>
     * A language name is required; on success, the user receives a confirmation message.
     * </p>
     */
    @FXML
    protected void onSaveLanguage() {
        String enteredName = nameField.getText() == null ? "" : nameField.getText().trim();
        if (enteredName.isEmpty()) {
            feedbackLabel.setText("Language name is required.");
            feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
            nameField.requestFocus();
            return;
        }

        feedbackLabel.setText(String.format("Saved programming language: %s", enteredName));
        feedbackLabel.setStyle("-fx-text-fill: #2e7d32;");
    }

    /**
     * Returns the user to the home page.
     *
     * @throws IOException if the home view cannot be loaded
     */
    @FXML
    protected void onBackToHome() throws IOException {
        Stage currentStage = (Stage) feedbackLabel.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(loader.load(), 480, 320);
        currentStage.setScene(scene);
        currentStage.setTitle("Academic Folio");
    }
}
