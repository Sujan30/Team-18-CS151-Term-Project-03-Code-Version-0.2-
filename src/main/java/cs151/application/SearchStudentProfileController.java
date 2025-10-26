package cs151.application;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Controller providing a read/search/delete experience for stored student profiles.
 */
public class SearchStudentProfileController {

    private static final List<String> ACADEMIC_STATUSES = List.of("Freshman", "Sophomore", "Junior", "Senior", "Graduate");
    private static final List<String> DATABASE_OPTIONS = List.of("MySQL", "Postgres", "MongoDB", "SQLite", "Oracle");
    private static final List<String> PREFERRED_ROLES = List.of("Front-End", "Back-End", "Full-Stack", "Data", "Other");

    private final StudentProfileRepository profileRepository = new StudentProfileRepository();
    private final LanguageRepository languageRepository = new LanguageRepository();
    private final ObservableList<StudentProfile> allProfiles = FXCollections.observableArrayList();
    private final ObservableList<StudentProfile> filteredProfiles = FXCollections.observableArrayList();

    @FXML
    private VBox rootContainer;

    @FXML
    private TextField nameFilterField;

    @FXML
    private ComboBox<String> statusFilterCombo;

    @FXML
    private ComboBox<String> languageFilterCombo;

    @FXML
    private ComboBox<String> databaseFilterCombo;

    @FXML
    private ComboBox<String> roleFilterCombo;

    @FXML
    private TableView<StudentProfile> profilesTable;

    @FXML
    private TableColumn<StudentProfile, String> nameColumn;

    @FXML
    private TableColumn<StudentProfile, String> statusColumn;

    @FXML
    private TableColumn<StudentProfile, String> jobStatusColumn;

    @FXML
    private TableColumn<StudentProfile, String> jobDetailsColumn;

    @FXML
    private TableColumn<StudentProfile, String> roleColumn;

    @FXML
    private TableColumn<StudentProfile, String> languagesColumn;

    @FXML
    private TableColumn<StudentProfile, String> databasesColumn;

    @FXML
    private TableColumn<StudentProfile, String> commentsColumn;

    @FXML
    private TableColumn<StudentProfile, String> whitelistColumn;

    @FXML
    private TableColumn<StudentProfile, String> blacklistColumn;

    @FXML
    private Label feedbackLabel;

    @FXML
    private Button deleteButton;

    @FXML
    private Button editButton;

    @FXML
    private void initialize() {
        setupFilters();
        setupTable();
        deleteButton.disableProperty().bind(profilesTable.getSelectionModel().selectedItemProperty().isNull());
        editButton.disableProperty().bind(profilesTable.getSelectionModel().selectedItemProperty().isNull());
        boolean loaded = loadProfiles();
        applyFilters(false);
        if (!loaded) {
            showError("Unable to load stored profiles. Define profiles first.");
        }
        Platform.runLater(() -> rootContainer.requestFocus());
    }

    @FXML
    private void onSearchProfiles() {
        applyFilters(true);
    }

    @FXML
    private void onResetFilters() {
        clearFilters();
        applyFilters(true);
    }

    @FXML
    private void onDeleteSelectedProfile() {
        StudentProfile selected = profilesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select a profile before deleting.");
            return;
        }

        try {
            boolean removed = profileRepository.deleteByName(selected.getFullName());
            if (removed) {
                boolean reloaded = loadProfiles();
                applyFilters(false);
                if (reloaded) {
                    showSuccess(String.format("Deleted profile for %s.", selected.getFullName()));
                } else {
                    showError("Profile deleted, but unable to reload the latest records.");
                }
            } else {
                showError("Profile not found in storage. Refresh and try again.");
            }
        } catch (IOException exception) {
            showError("Unable to delete the profile. Please try again.");
        }
    }

    // [Edit Mode]
    @FXML
    private void onEditSelectedProfile() {
        StudentProfile selected = profilesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select a profile before editing.");
            return;
        }

        try {
            Stage currentStage = (Stage) rootContainer.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("student-profile-view.fxml"));
            Parent view = loader.load();

            StudentProfileController controller = loader.getController();
            controller.initializeForEdit(selected);

            Scene scene = currentStage.getScene();
            scene.setRoot(view);
            currentStage.setTitle("Edit Student Profile");
        } catch (IOException exception) {
            showError("Unable to open edit view. Please try again.");
        }
    }

    @FXML
    private void onBackToHome() throws IOException {
        Stage currentStage = (Stage) rootContainer.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("hello-view.fxml"));
        switchScene(currentStage, loader, "Curriculum Setup");
    }

    private void setupFilters() {
        statusFilterCombo.setItems(FXCollections.observableArrayList(ACADEMIC_STATUSES));
        languageFilterCombo.setPromptText("Any");
        databaseFilterCombo.setItems(FXCollections.observableArrayList(DATABASE_OPTIONS));
        roleFilterCombo.setItems(FXCollections.observableArrayList(PREFERRED_ROLES));
        statusFilterCombo.setPromptText("Any");
        databaseFilterCombo.setPromptText("Any");
        roleFilterCombo.setPromptText("Any");
        populateLanguageFilter();
    }

    private void populateLanguageFilter() {
        try {
            List<String> languages = languageRepository.loadAll().stream()
                    .map(ProgrammingLanguage::getName)
                    .collect(Collectors.toList());
            languageFilterCombo.setItems(FXCollections.observableArrayList(languages));
            languageFilterCombo.setDisable(languages.isEmpty());
            if (languages.isEmpty()) {
                languageFilterCombo.setPromptText("No languages defined");
            } else {
                languageFilterCombo.setPromptText("Any");
            }
        } catch (IOException exception) {
            languageFilterCombo.setItems(FXCollections.observableArrayList());
            languageFilterCombo.setDisable(true);
            languageFilterCombo.setPromptText("Unable to load languages");
        }
    }

    private void setupTable() {
        nameColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getFullName()));
        statusColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getAcademicStatus()));
        jobStatusColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getJobStatusLabel()));
        jobDetailsColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getJobDetailsDisplay()));
        roleColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getPreferredRole()));
        languagesColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().formatLanguages()));
        databasesColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().formatDatabases()));
        commentsColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().formatComments()));
        whitelistColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getWhitelistLabel()));
        blacklistColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getBlacklistLabel()));

        profilesTable.setItems(filteredProfiles);
        nameColumn.setSortType(TableColumn.SortType.ASCENDING);
        profilesTable.getSortOrder().clear();
        profilesTable.getSortOrder().add(nameColumn);
        configureWrappingColumn(jobDetailsColumn);
        configureWrappingColumn(languagesColumn);
        configureWrappingColumn(databasesColumn);
        configureWrappingColumn(commentsColumn);
        profilesTable.setPlaceholder(new Label("No profiles match the current filters."));
    }

    private boolean loadProfiles() {
        try {
            List<StudentProfile> storedProfiles = profileRepository.loadAll();
            allProfiles.setAll(storedProfiles);
            FXCollections.sort(allProfiles, Comparator.comparing(StudentProfile::getFullName, String.CASE_INSENSITIVE_ORDER));
            return true;
        } catch (IOException exception) {
            allProfiles.clear();
            return false;
        }
    }

    private void applyFilters(boolean displayResult) {
        String nameFilter = normalize(nameFilterField.getText());
        String statusFilter = valueOrEmpty(statusFilterCombo == null ? null : statusFilterCombo.getValue());
        String languageFilter = valueOrEmpty(languageFilterCombo == null ? null : languageFilterCombo.getValue());
        String databaseFilter = valueOrEmpty(databaseFilterCombo == null ? null : databaseFilterCombo.getValue());
        String roleFilter = valueOrEmpty(roleFilterCombo == null ? null : roleFilterCombo.getValue());

        List<StudentProfile> matches = allProfiles.stream()
                .filter(profile -> matchesName(profile, nameFilter))
                .filter(profile -> matchesSingleValue(profile.getAcademicStatus(), statusFilter))
                .filter(profile -> matchesCollection(profile.getProgrammingLanguages(), languageFilter))
                .filter(profile -> matchesCollection(profile.getDatabases(), databaseFilter))
                .filter(profile -> matchesSingleValue(profile.getPreferredRole(), roleFilter))
                .collect(Collectors.toList());

        filteredProfiles.setAll(matches);
        FXCollections.sort(filteredProfiles, Comparator.comparing(StudentProfile::getFullName, String.CASE_INSENSITIVE_ORDER));
        profilesTable.sort();

        if (displayResult) {
            if (matches.isEmpty()) {
                showError("No profiles matched your filters.");
            } else {
                showSuccess(String.format("Showing %d profile(s).", matches.size()));
            }
        } else {
            clearFeedback();
        }
    }

    private boolean matchesName(StudentProfile profile, String filter) {
        if (filter.isEmpty()) {
            return true;
        }
        return profile.getFullName().toLowerCase(Locale.ENGLISH).contains(filter.toLowerCase(Locale.ENGLISH));
    }

    private boolean matchesSingleValue(String candidate, String filter) {
        if (filter.isEmpty()) {
            return true;
        }
        return candidate != null && candidate.equalsIgnoreCase(filter);
    }

    private boolean matchesCollection(List<String> values, String filter) {
        if (filter.isEmpty()) {
            return true;
        }
        return values.stream().anyMatch(value -> value.equalsIgnoreCase(filter));
    }

    private void clearFilters() {
        nameFilterField.clear();
        statusFilterCombo.getSelectionModel().clearSelection();
        languageFilterCombo.getSelectionModel().clearSelection();
        databaseFilterCombo.getSelectionModel().clearSelection();
        roleFilterCombo.getSelectionModel().clearSelection();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private void clearFeedback() {
        feedbackLabel.setText("");
    }

    private void showSuccess(String message) {
        feedbackLabel.setText(message);
        feedbackLabel.setStyle("-fx-text-fill: #2e7d32;");
    }

    private void showError(String message) {
        feedbackLabel.setText(message);
        feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
    }

    private void configureWrappingColumn(TableColumn<StudentProfile, String> column) {
        column.setCellFactory(col -> new TableCell<>() {
            private final Text text = new Text();

            {
                text.wrappingWidthProperty().bind(col.widthProperty().subtract(16));
                setGraphic(text);
                setPrefHeight(Control.USE_COMPUTED_SIZE);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
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
