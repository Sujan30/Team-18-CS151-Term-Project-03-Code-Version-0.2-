package cs151.application;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Provides simple whitelist/blacklist reports driven from persisted student profiles.
 */
public class ReportsController {

    private final StudentProfileRepository profileRepository = new StudentProfileRepository();

    private final ObservableList<StudentProfile> displayedProfiles = FXCollections.observableArrayList();
    private List<StudentProfile> allProfiles = new ArrayList<>();

    private ReportFilter currentFilter = ReportFilter.WHITELIST;
    private final ToggleGroup reportToggleGroup = new ToggleGroup();

    @FXML
    private VBox rootContainer;

    @FXML
    private RadioButton whitelistRadio;

    @FXML
    private RadioButton blacklistRadio;

    @FXML
    private TableView<StudentProfile> reportTable;

    @FXML
    private TableColumn<StudentProfile, String> nameColumn;

    @FXML
    private TableColumn<StudentProfile, String> statusColumn;

    @FXML
    private TableColumn<StudentProfile, String> jobStatusColumn;

    @FXML
    private TableColumn<StudentProfile, String> roleColumn;

    @FXML
    private TableColumn<StudentProfile, String> flagsColumn;

    @FXML
    private Label statusLabel;

    @FXML
    private void initialize() {
        setupToggleGroup();
        setupTable();
        refreshFromStorage();
        applyFilter();
        Platform.runLater(() -> rootContainer.requestFocus());
    }

    private void setupToggleGroup() {
        whitelistRadio.setToggleGroup(reportToggleGroup);
        blacklistRadio.setToggleGroup(reportToggleGroup);
        whitelistRadio.setOnAction(event -> {
            currentFilter = ReportFilter.WHITELIST;
            applyFilter();
        });
        blacklistRadio.setOnAction(event -> {
            currentFilter = ReportFilter.BLACKLIST;
            applyFilter();
        });
        whitelistRadio.setSelected(true);
    }

    private void setupTable() {
        reportTable.setItems(displayedProfiles);
        reportTable.setPlaceholder(new Label("No students match the selected report."));
        nameColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getFullName()));
        statusColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getAcademicStatus()));
        jobStatusColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getJobStatusLabel()));
        roleColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getPreferredRole()));
        flagsColumn.setCellValueFactory(cell -> {
            StudentProfile profile = cell.getValue();
            String label = profile.isWhitelist() ? "Whitelist" : profile.isBlacklist() ? "Blacklist" : "";
            return new ReadOnlyStringWrapper(label);
        });

        reportTable.setRowFactory(table -> {
            TableRow<StudentProfile> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    openDetail(row.getItem());
                }
            });
            return row;
        });
    }

    private void refreshFromStorage() {
        try {
            allProfiles = profileRepository.loadAll();
            allProfiles.sort(Comparator.comparing(StudentProfile::getFullName, String.CASE_INSENSITIVE_ORDER));
            statusLabel.setText("Select whitelist or blacklist to view matching students.");
            statusLabel.setStyle("-fx-text-fill: #2e7d32;");
        } catch (IOException exception) {
            allProfiles = List.of();
            statusLabel.setText("Unable to load profiles. Please define student profiles first.");
            statusLabel.setStyle("-fx-text-fill: #d32f2f;");
        }
    }

    private void applyFilter() {
        if (allProfiles.isEmpty()) {
            displayedProfiles.clear();
            return;
        }

        List<StudentProfile> filtered = allProfiles.stream()
                .filter(profile -> currentFilter == ReportFilter.WHITELIST ? profile.isWhitelist() : profile.isBlacklist())
                .toList();
        displayedProfiles.setAll(filtered);

        if (filtered.isEmpty()) {
            statusLabel.setText("No students found for the selected report.");
            statusLabel.setStyle("-fx-text-fill: #d32f2f;");
        } else {
            statusLabel.setText(String.format("Showing %d student(s) marked as %s.", filtered.size(),
                    currentFilter == ReportFilter.WHITELIST ? "Whitelist" : "Blacklist"));
            statusLabel.setStyle("-fx-text-fill: #2e7d32;");
        }
    }

    @FXML
    private void onRefresh() {
        refreshFromStorage();
        applyFilter();
    }

    @FXML
    private void onBackToHome() throws IOException {
        Stage currentStage = (Stage) rootContainer.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("hello-view.fxml"));
        Parent view = loader.load();
        switchScene(currentStage, view, "Curriculum Setup");
    }

    private void openDetail(StudentProfile profile) {
        try {
            Stage currentStage = (Stage) rootContainer.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("report-student-detail-view.fxml"));
            Parent view = loader.load();
            ReportStudentDetailController controller = loader.getController();
            controller.setProfile(profile);
            controller.setReturnFilter(currentFilter);
            switchScene(currentStage, view, "Student Report Detail");
        } catch (IOException exception) {
            statusLabel.setText("Unable to open detail view. Please try again.");
            statusLabel.setStyle("-fx-text-fill: #d32f2f;");
        }
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

    public void setInitialFilter(ReportFilter filter) {
        if (filter == null) {
            return;
        }
        this.currentFilter = filter;
        if (filter == ReportFilter.WHITELIST) {
            whitelistRadio.setSelected(true);
        } else {
            blacklistRadio.setSelected(true);
        }
        applyFilter();
    }

    public enum ReportFilter {
        WHITELIST,
        BLACKLIST
    }
}
