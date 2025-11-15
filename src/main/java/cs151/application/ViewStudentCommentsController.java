package cs151.application;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Controller for reviewing and appending dated comments to a stored {@link StudentProfile}.
 */
public class ViewStudentCommentsController {

    private static final DateTimeFormatter COMMENT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final StudentProfileRepository profileRepository = new StudentProfileRepository();

    private final ObservableList<String> comments = FXCollections.observableArrayList();

    private StudentProfile currentProfile;

    private String returnNameFilter = "";
    private String returnStatusFilter = "";
    private String returnLanguageFilter = "";
    private String returnDatabaseFilter = "";
    private String returnRoleFilter = "";
    private String successMessageOnReturn;

    @FXML
    private VBox rootContainer;

    @FXML
    private Label studentNameLabel;

    @FXML
    private ListView<String> commentsListView;

    @FXML
    private TextArea commentInputArea;

    @FXML
    private Label feedbackLabel;

    @FXML
    private void initialize() {
        commentsListView.setItems(comments);
        commentsListView.setPlaceholder(new Label("No comments recorded yet."));
        commentsListView.setCellFactory(list -> new ListCell<>() {
            private final Text text = new Text();

            {
                text.wrappingWidthProperty().bind(commentsListView.widthProperty().subtract(24));
                setGraphic(text);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isBlank()) {
                    text.setText("");
                } else {
                    text.setText(item);
                }
            }
        });
        clearFeedback();
        Platform.runLater(() -> commentInputArea.requestFocus());
    }

    public void setProfile(StudentProfile profile) {
        this.currentProfile = profile;
        studentNameLabel.setText(profile.getFullName());
        comments.setAll(profile.getComments());
        if (comments.isEmpty()) {
            commentsListView.setPlaceholder(new Label("No comments recorded yet."));
        }
        commentInputArea.clear();
        clearFeedback();
    }

    public void setReturnState(String nameFilter, String statusFilter, String languageFilter, String databaseFilter,
                               String roleFilter) {
        this.returnNameFilter = safeValue(nameFilter);
        this.returnStatusFilter = safeValue(statusFilter);
        this.returnLanguageFilter = safeValue(languageFilter);
        this.returnDatabaseFilter = safeValue(databaseFilter);
        this.returnRoleFilter = safeValue(roleFilter);
    }

    @FXML
    private void onAddComment() {
        clearFeedback();
        String enteredComment = commentInputArea.getText() == null ? "" : commentInputArea.getText().trim();
        if (enteredComment.isEmpty()) {
            setError("Enter a comment before saving.");
            commentInputArea.requestFocus();
            return;
        }

        String sanitized = enteredComment.replaceAll("\r?\n", " ").trim();
        sanitized = sanitized.replaceAll("\s+", " ");
        if (sanitized.isEmpty()) {
            setError("Enter a comment before saving.");
            commentInputArea.requestFocus();
            return;
        }

        String stampedComment = String.format(Locale.ENGLISH, "%s - %s",
                LocalDate.now().format(COMMENT_DATE_FORMAT), sanitized);
        List<String> updatedComments = new ArrayList<>(currentProfile.getComments());
        updatedComments.add(stampedComment);

        StudentProfile updatedProfile = new StudentProfile(currentProfile.getFullName(),
                currentProfile.getAcademicStatus(),
                currentProfile.isEmployed(),
                currentProfile.getJobDetails(),
                currentProfile.getProgrammingLanguages(),
                currentProfile.getDatabases(),
                currentProfile.getPreferredRole(),
                updatedComments,
                currentProfile.isWhitelist(),
                currentProfile.isBlacklist());

        try {
            boolean updated = profileRepository.updateProfile(currentProfile.getFullName(), updatedProfile);
            if (updated) {
                currentProfile = updatedProfile;
                comments.setAll(updatedProfile.getComments());
                commentInputArea.clear();
                successMessageOnReturn = String.format(Locale.ENGLISH, "Added comment for %s.", currentProfile.getFullName());
                setSuccess("Comment added.");
            } else {
                setError("Unable to save the comment. Please try again.");
            }
        } catch (IOException exception) {
            setError("Unable to save the comment. Please try again.");
        }
    }

    @FXML
    private void onBackToSearch() throws IOException {
        Stage stage = (Stage) rootContainer.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("search-student-profile-view.fxml"));
        Parent view = loader.load();
        SearchStudentProfileController controller = loader.getController();
        controller.applyInitialFilters(returnNameFilter, returnStatusFilter, returnLanguageFilter, returnDatabaseFilter,
                returnRoleFilter);
        if (successMessageOnReturn != null) {
            controller.showSuccessMessage(successMessageOnReturn);
        }
        switchScene(stage, view, "Search Student Profiles");
    }

    private void clearFeedback() {
        feedbackLabel.setText("");
    }

    private void setError(String message) {
        feedbackLabel.setText(message);
        feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
    }

    private void setSuccess(String message) {
        feedbackLabel.setText(message);
        feedbackLabel.setStyle("-fx-text-fill: #2e7d32;");
    }

    private String safeValue(String value) {
        return value == null ? "" : value;
    }

    private void switchScene(Stage stage, Parent view, String title) {
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
