package cs151.application;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

/**
 * Simple dialog to display a single comment in full.
 */
public class CommentDetailController {

    @FXML
    private Label studentNameLabel;

    @FXML
    private Label commentDateLabel;

    @FXML
    private TextArea commentBodyArea;

    @FXML
    private Button closeButton;

    private Stage dialogStage;

    @FXML
    private void initialize() {
        commentBodyArea.setWrapText(true);
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setComment(String studentName, ReportStudentDetailController.CommentEntry entry) {
        studentNameLabel.setText(studentName);
        commentDateLabel.setText(entry.dateProperty().get());
        commentBodyArea.setText(entry.getFullText());
    }

    @FXML
    private void onCloseDialog() {
        Stage stage = dialogStage;
        if (stage == null && closeButton != null && closeButton.getScene() != null) {
            stage = (Stage) closeButton.getScene().getWindow();
        }
        if (stage != null) {
            stage.close();
        }
    }
}
