package cs151.application;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Displays a student's profile details alongside their comment history.
 */
public class ReportStudentDetailController {

    private final ObservableList<CommentEntry> comments = FXCollections.observableArrayList();

    private StudentProfile profile;
    private ReportsController.ReportFilter returnFilter = ReportsController.ReportFilter.WHITELIST;

    @FXML
    private VBox rootContainer;

    @FXML
    private Label nameValue;

    @FXML
    private Label statusValue;

    @FXML
    private Label jobStatusValue;

    @FXML
    private Label jobDetailsValue;

    @FXML
    private Label languagesValue;

    @FXML
    private Label databasesValue;

    @FXML
    private Label roleValue;

    @FXML
    private Label flagsValue;

    @FXML
    private TableView<CommentEntry> commentsTable;

    @FXML
    private TableColumn<CommentEntry, String> commentDateColumn;

    @FXML
    private TableColumn<CommentEntry, String> commentPreviewColumn;

    @FXML
    private Label feedbackLabel;

    @FXML
    private void initialize() {
        commentsTable.setItems(comments);
        commentsTable.setPlaceholder(new Label("No comments recorded."));
        commentDateColumn.setCellValueFactory(cell -> {
            CommentEntry entry = cell.getValue();
            return entry == null ? new ReadOnlyStringWrapper("") : entry.dateProperty();
        });
        commentPreviewColumn.setCellValueFactory(cell -> {
            CommentEntry entry = cell.getValue();
            return entry == null ? new ReadOnlyStringWrapper("") : entry.previewProperty();
        });
        configureRowInteraction();
    }

    public void setProfile(StudentProfile profile) {
        this.profile = profile;
        nameValue.setText(profile.getFullName());
        statusValue.setText(profile.getAcademicStatus());
        jobStatusValue.setText(profile.getJobStatusLabel());
        jobDetailsValue.setText(profile.getJobDetailsDisplay());
        languagesValue.setText(profile.formatLanguages());
        databasesValue.setText(profile.formatDatabases());
        roleValue.setText(profile.getPreferredRole());
        flagsValue.setText(buildFlagLabel(profile));
        comments.setAll(toCommentEntries(profile.getComments()));
        feedbackLabel.setText("Double-click a comment to view it in full.");
        feedbackLabel.setStyle("-fx-text-fill: #2e7d32;");
    }

    public void setReturnFilter(ReportsController.ReportFilter filter) {
        if (filter != null) {
            this.returnFilter = filter;
        }
    }

    @FXML
    private void onBackToReports() throws IOException {
        Stage currentStage = (Stage) rootContainer.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("reports-view.fxml"));
        Parent view = loader.load();
        ReportsController controller = loader.getController();
        controller.setInitialFilter(returnFilter);
        switchScene(currentStage, view, "Student Reports");
    }

    private void configureRowInteraction() {
        commentsTable.setRowFactory(table -> {
            TableRow<CommentEntry> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    openCommentDetail(row.getItem());
                }
            });
            return row;
        });
    }

    private void openCommentDetail(CommentEntry entry) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("comment-detail-view.fxml"));
            Parent view = loader.load();
            CommentDetailController controller = loader.getController();
            controller.setComment(profile.getFullName(), entry);
            Stage dialog = new Stage();
            controller.setDialogStage(dialog);
            dialog.setTitle("Comment Details");
            dialog.setScene(new Scene(view));
            dialog.initOwner(rootContainer.getScene().getWindow());
            dialog.show();
        } catch (IOException exception) {
            feedbackLabel.setText("Unable to open comment details.");
            feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
        }
    }

    private List<CommentEntry> toCommentEntries(List<String> storedComments) {
        List<CommentEntry> entries = new ArrayList<>();
        if (storedComments == null) {
            return entries;
        }
        for (String comment : storedComments) {
            if (comment == null || comment.isBlank()) {
                continue;
            }
            entries.add(CommentEntry.from(comment));
        }
        return entries;
    }

    private String buildFlagLabel(StudentProfile profile) {
        if (profile.isWhitelist()) {
            return "Whitelist";
        }
        if (profile.isBlacklist()) {
            return "Blacklist";
        }
        return "";
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

    /**
     * Lightweight view model for displaying comment metadata.
     */
    static final class CommentEntry {
        private final StringProperty date;
        private final StringProperty preview;
        private final String fullText;

        private CommentEntry(String date, String preview, String fullText) {
            this.date = new SimpleStringProperty(date);
            this.preview = new SimpleStringProperty(preview);
            this.fullText = fullText;
        }

        public static CommentEntry from(String storedValue) {
            String date = "";
            String text = storedValue == null ? "" : storedValue;
            if (storedValue != null) {
                int newlineIndex = storedValue.indexOf('\n');
                if (newlineIndex > 0) {
                    date = storedValue.substring(0, newlineIndex).trim();
                    text = storedValue.substring(newlineIndex + 1).trim();
                }
            }
            String preview = text.length() > 90 ? text.substring(0, 87) + "..." : text;
            return new CommentEntry(date, preview, storedValue == null ? "" : storedValue);
        }

        public StringProperty dateProperty() {
            return date;
        }

        public StringProperty previewProperty() {
            return preview;
        }

        public String getFullText() {
            return fullText;
        }
    }
}
