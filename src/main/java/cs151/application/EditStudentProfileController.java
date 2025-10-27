package cs151.application;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Controller allowing faculty to review and edit a stored {@link StudentProfile} instance.
 */
public class EditStudentProfileController {

    private static final List<String> ACADEMIC_STATUSES = List.of("Freshman", "Sophomore", "Junior", "Senior", "Graduate");
    private static final List<String> DATABASE_OPTIONS = List.of("MySQL", "Postgres", "MongoDB", "SQLite", "Oracle");
    private static final List<String> PREFERRED_ROLES = List.of("Front-End", "Back-End", "Full-Stack", "Data", "Other");

    private final StudentProfileRepository profileRepository = new StudentProfileRepository();
    private final LanguageRepository languageRepository = new LanguageRepository();

    private ToggleGroup jobStatusGroup;
    private String originalName;

    private String returnNameFilter = "";
    private String returnStatusFilter = "";
    private String returnLanguageFilter = "";
    private String returnDatabaseFilter = "";
    private String returnRoleFilter = "";
    private String successMessageOnReturn;

    @FXML
    private VBox rootContainer;

    @FXML
    private TextField fullNameField;

    @FXML
    private ComboBox<String> academicStatusCombo;

    @FXML
    private RadioButton employedRadio;

    @FXML
    private RadioButton unemployedRadio;

    @FXML
    private TextField jobDetailsField;

    @FXML
    private ListView<String> languagesListView;

    @FXML
    private ListView<String> databasesListView;

    @FXML
    private ComboBox<String> preferredRoleCombo;

    @FXML
    private TextArea commentsArea;

    @FXML
    private CheckBox whitelistCheckBox;

    @FXML
    private CheckBox blacklistCheckBox;

    @FXML
    private Label feedbackLabel;

    @FXML
    private void initialize() {
        initializeJobStatusControls();
        initializeSelections();
        clearFeedback();
        Platform.runLater(() -> rootContainer.requestFocus());
    }

    /**
     * Populates fields with the stored profile data and retains the original name for update tracking.
     */
    public void setProfile(StudentProfile profile) {
        this.originalName = profile.getFullName();
        populateFields(profile);
    }

    /**
     * Captures the filter selections from the search view so they can be restored when returning.
     */
    public void setReturnState(String nameFilter, String statusFilter, String languageFilter, String databaseFilter,
                               String roleFilter) {
        this.returnNameFilter = safeValue(nameFilter);
        this.returnStatusFilter = safeValue(statusFilter);
        this.returnLanguageFilter = safeValue(languageFilter);
        this.returnDatabaseFilter = safeValue(databaseFilter);
        this.returnRoleFilter = safeValue(roleFilter);
    }

    @FXML
    private void onSaveProfile() {
        clearFeedback();

        String trimmedName = normalize(fullNameField.getText());
        if (trimmedName.isEmpty()) {
            setError("Full Name is required.");
            fullNameField.requestFocus();
            return;
        }

        String academicStatus = academicStatusCombo.getValue();
        if (academicStatus == null || academicStatus.isBlank()) {
            setError("Select the academic status.");
            academicStatusCombo.requestFocus();
            return;
        }

        boolean employed = jobStatusGroup.getSelectedToggle() == employedRadio;
        String jobDetails = normalize(jobDetailsField.getText());
        if (employed && jobDetails.isEmpty()) {
            setError("Provide job details for employed students.");
            jobDetailsField.requestFocus();
            return;
        }

        List<String> selectedLanguages = new ArrayList<>(languagesListView.getSelectionModel().getSelectedItems());
        if (selectedLanguages.isEmpty()) {
            setError("Select at least one programming language.");
            languagesListView.requestFocus();
            return;
        }

        List<String> selectedDatabases = new ArrayList<>(databasesListView.getSelectionModel().getSelectedItems());
        if (selectedDatabases.isEmpty()) {
            setError("Select at least one database.");
            databasesListView.requestFocus();
            return;
        }

        String preferredRole = preferredRoleCombo.getValue();
        if (preferredRole == null || preferredRole.isBlank()) {
            setError("Select the preferred professional role.");
            preferredRoleCombo.requestFocus();
            return;
        }

        boolean whitelist = whitelistCheckBox.isSelected();
        boolean blacklist = blacklistCheckBox.isSelected();
        if (whitelist && blacklist) {
            setError("Choose either whitelist or blacklist, not both.");
            return;
        }

        List<String> updatedComments = Arrays.stream(commentsArea.getText().split("\\R"))
                .map(String::trim)
                .filter(entry -> !entry.isEmpty())
                .collect(Collectors.toList());

        StudentProfile updatedProfile = new StudentProfile(trimmedName,
                academicStatus,
                employed,
                jobDetails,
                selectedLanguages,
                selectedDatabases,
                preferredRole,
                updatedComments,
                whitelist,
                blacklist);

        try {
            boolean updated = profileRepository.updateProfile(originalName, updatedProfile);
            if (updated) {
                originalName = updatedProfile.getFullName();
                successMessageOnReturn = String.format(Locale.ENGLISH, "Updated profile for %s.", updatedProfile.getFullName());
                setSuccess("Profile updated successfully.");
            } else {
                setError("Unable to update profile. Ensure the name is unique and the original record still exists.");
            }
        } catch (IOException exception) {
            setError("Unable to save changes. Please try again.");
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

    private void initializeJobStatusControls() {
        jobStatusGroup = new ToggleGroup();
        employedRadio.setToggleGroup(jobStatusGroup);
        unemployedRadio.setToggleGroup(jobStatusGroup);
        unemployedRadio.setSelected(true);
        jobDetailsField.setDisable(true);
        jobStatusGroup.selectedToggleProperty().addListener(this::handleJobStatusChange);
    }

    private void initializeSelections() {
        academicStatusCombo.setItems(FXCollections.observableArrayList(ACADEMIC_STATUSES));
        preferredRoleCombo.setItems(FXCollections.observableArrayList(PREFERRED_ROLES));

        languagesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        databasesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        databasesListView.setItems(FXCollections.observableArrayList(DATABASE_OPTIONS));

        whitelistCheckBox.selectedProperty().addListener(this::handleWhitelistChange);
        blacklistCheckBox.selectedProperty().addListener(this::handleBlacklistChange);

        loadLanguages(List.of());
    }

    private void populateFields(StudentProfile profile) {
        fullNameField.setText(profile.getFullName());
        selectComboValue(academicStatusCombo, profile.getAcademicStatus());

        if (profile.isEmployed()) {
            jobStatusGroup.selectToggle(employedRadio);
            jobDetailsField.setDisable(false);
        } else {
            jobStatusGroup.selectToggle(unemployedRadio);
            jobDetailsField.setDisable(true);
        }
        jobDetailsField.setText(profile.getJobDetails() == null ? "" : profile.getJobDetails());

        loadLanguages(profile.getProgrammingLanguages());
    ensureItemsPresent(databasesListView, profile.getDatabases());
        selectListValues(languagesListView, profile.getProgrammingLanguages());
        selectListValues(databasesListView, profile.getDatabases());
        selectComboValue(preferredRoleCombo, profile.getPreferredRole());

        commentsArea.setText(String.join(System.lineSeparator(), profile.getComments()));
        whitelistCheckBox.setSelected(profile.isWhitelist());
        blacklistCheckBox.setSelected(profile.isBlacklist());
    }

    private void loadLanguages(List<String> preferredLanguages) {
        try {
            List<String> storedLanguages = languageRepository.loadAll().stream()
                    .map(ProgrammingLanguage::getName)
                    .collect(Collectors.toList());
            Set<String> merged = new LinkedHashSet<>(storedLanguages);
            merged.addAll(preferredLanguages);
            ObservableList<String> items = FXCollections.observableArrayList(merged);
            languagesListView.setItems(items);
            languagesListView.setDisable(items.isEmpty());
            if (items.isEmpty()) {
                languagesListView.setPlaceholder(new Label("No languages defined."));
            } else {
                languagesListView.setPlaceholder(null);
            }
        } catch (IOException exception) {
            ObservableList<String> items = FXCollections.observableArrayList(preferredLanguages);
            languagesListView.setItems(items);
            languagesListView.setDisable(items.isEmpty());
            if (items.isEmpty()) {
                languagesListView.setPlaceholder(new Label("Unable to load languages."));
            }
        }
    }

    private void handleJobStatusChange(@SuppressWarnings("unused") javafx.beans.value.ObservableValue<? extends Toggle> observable,
                                       @SuppressWarnings("unused") Toggle previousToggle,
                                       Toggle currentToggle) {
        boolean employed = currentToggle == employedRadio;
        jobDetailsField.setDisable(!employed);
        if (!employed) {
            jobDetailsField.clear();
        }
    }

    private void handleWhitelistChange(@SuppressWarnings("unused") javafx.beans.value.ObservableValue<? extends Boolean> observable,
                                       @SuppressWarnings("unused") Boolean previousValue,
                                       Boolean currentValue) {
        if (Boolean.TRUE.equals(currentValue)) {
            blacklistCheckBox.setSelected(false);
        }
    }

    private void handleBlacklistChange(@SuppressWarnings("unused") javafx.beans.value.ObservableValue<? extends Boolean> observable,
                                       @SuppressWarnings("unused") Boolean previousValue,
                                       Boolean currentValue) {
        if (Boolean.TRUE.equals(currentValue)) {
            whitelistCheckBox.setSelected(false);
        }
    }

    private void selectListValues(ListView<String> listView, List<String> values) {
        listView.getSelectionModel().clearSelection();
        if (values == null || values.isEmpty()) {
            return;
        }
        for (String value : values) {
            int index = findIndexCaseInsensitive(listView.getItems(), value);
            if (index >= 0) {
                listView.getSelectionModel().select(index);
            }
        }
    }

    private void selectComboValue(ComboBox<String> comboBox, String value) {
        comboBox.getSelectionModel().clearSelection();
        if (value == null || value.isBlank()) {
            return;
        }
        comboBox.getItems().stream()
                .filter(item -> item.equalsIgnoreCase(value))
                .findFirst()
                .ifPresent(item -> comboBox.getSelectionModel().select(item));
    }

    private int findIndexCaseInsensitive(List<String> items, String value) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).equalsIgnoreCase(value)) {
                return i;
            }
        }
        return -1;
    }

    private void ensureItemsPresent(ListView<String> listView, List<String> values) {
        if (values == null || values.isEmpty()) {
            return;
        }
        ObservableList<String> items = listView.getItems();
        for (String value : values) {
            if (value == null || value.isBlank()) {
                continue;
            }
            boolean exists = items.stream().anyMatch(existing -> existing.equalsIgnoreCase(value));
            if (!exists) {
                items.add(value);
            }
        }
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

    private String normalize(String value) {
        return value == null ? "" : value.trim();
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
