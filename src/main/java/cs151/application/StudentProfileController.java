package cs151.application;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.text.Text;

/**
 * Controller coordinating the student profile definition workflow.
 */
public class StudentProfileController {

    private static final List<String> ACADEMIC_STATUSES = List.of("Freshman", "Sophomore", "Junior", "Senior", "Graduate");
    private static final List<String> DATABASE_OPTIONS = List.of("MySQL", "Postgres", "MongoDB", "SQLite", "Oracle");
    private static final List<String> PREFERRED_ROLES = List.of("Front-End", "Back-End", "Full-Stack", "Data", "Other");
    private static final DateTimeFormatter COMMENT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final LanguageRepository languageRepository = new LanguageRepository();
    private final StudentProfileRepository profileRepository = new StudentProfileRepository();
    private final ObservableList<StudentProfile> profiles = FXCollections.observableArrayList();
    private final ObservableList<String> comments = FXCollections.observableArrayList();
    private ToggleGroup jobStatusGroup;

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
    private TextArea commentInputArea;

    @FXML
    private ListView<String> commentsListView;

    @FXML
    private CheckBox whitelistCheckBox;

    @FXML
    private CheckBox blacklistCheckBox;

    @FXML
    private Label feedbackLabel;

    @FXML
    private Button toggleProfilesButton;

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
    private VBox profilesContainer;

    @FXML
    private BorderPane rootPane;

    @FXML
    private void initialize() {
        initializeJobStatusControls();
        initializeSelections();
        initializeTable();
        loadLanguages();
        loadProfiles();
        profilesContainer.setVisible(false);
        profilesContainer.setManaged(false);
        toggleProfilesButton.setText("View Stored Profiles");
        Platform.runLater(() -> rootPane.requestFocus());
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

        commentsListView.setItems(comments);

        whitelistCheckBox.selectedProperty().addListener(this::handleWhitelistChange);

        blacklistCheckBox.selectedProperty().addListener(this::handleBlacklistChange);
    }

    private void initializeTable() {
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

        profilesTable.setItems(profiles);
        nameColumn.setSortType(TableColumn.SortType.ASCENDING);
        profilesTable.getSortOrder().clear();
        profilesTable.getSortOrder().add(nameColumn);
        configureWrappingColumn(jobDetailsColumn);
        configureWrappingColumn(languagesColumn);
        configureWrappingColumn(databasesColumn);
        configureWrappingColumn(commentsColumn);
        profilesTable.setPlaceholder(new Label("Click \"View Stored Profiles\" to display records."));
    }

    private void loadLanguages() {
        try {
            List<ProgrammingLanguage> allLanguages = languageRepository.loadAll();
            List<String> languageNames = allLanguages.stream()
                    .map(ProgrammingLanguage::getName)
                    .collect(Collectors.toList());
            languagesListView.setItems(FXCollections.observableArrayList(languageNames));
            languagesListView.setDisable(languageNames.isEmpty());
            if (languageNames.isEmpty()) {
                languagesListView.setPlaceholder(new Label("Define programming languages first."));
            } else {
                languagesListView.setPlaceholder(null);
            }
        } catch (IOException exception) {
            feedbackLabel.setText("Unable to load programming languages. Define them first.");
            feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
            languagesListView.setDisable(true);
            languagesListView.setPlaceholder(new Label("Define programming languages first."));
        }
    }

    private boolean loadProfiles() {
        try {
            List<StudentProfile> storedProfiles = profileRepository.loadAll();
            profiles.setAll(storedProfiles);
            sortProfiles();
            return true;
        } catch (IOException exception) {
            feedbackLabel.setText("Unable to load stored profiles. Add a new profile to begin.");
            feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
            return false;
        }
    }

    private void handleJobStatusChange(@SuppressWarnings("unused") ObservableValue<? extends Toggle> observable,
                                       @SuppressWarnings("unused") Toggle previousToggle,
                                       Toggle currentToggle) {
        if (currentToggle == null) {
            jobDetailsField.setDisable(true);
            jobDetailsField.clear();
            return;
        }

        boolean employed = currentToggle == employedRadio;
        jobDetailsField.setDisable(!employed);
        if (!employed) {
            jobDetailsField.clear();
        }
    }

    private void handleWhitelistChange(@SuppressWarnings("unused") ObservableValue<? extends Boolean> observable,
                                       @SuppressWarnings("unused") Boolean previousValue,
                                       Boolean currentValue) {
        if (Boolean.TRUE.equals(currentValue)) {
            blacklistCheckBox.setSelected(false);
        }
    }

    private void handleBlacklistChange(@SuppressWarnings("unused") ObservableValue<? extends Boolean> observable,
                                       @SuppressWarnings("unused") Boolean previousValue,
                                       Boolean currentValue) {
        if (Boolean.TRUE.equals(currentValue)) {
            whitelistCheckBox.setSelected(false);
        }
    }

    @FXML
    protected void onAddComment() {
        String entry = commentInputArea.getText() == null ? "" : commentInputArea.getText().trim();
        if (entry.isEmpty()) {
            feedbackLabel.setText("Enter a comment before adding it.");
            feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
            return;
        }
        String stampedComment = String.format("%s - %s", LocalDate.now().format(COMMENT_DATE_FORMAT), entry);
        comments.add(stampedComment);
        commentInputArea.clear();
        feedbackLabel.setText("Comment added.");
        feedbackLabel.setStyle("-fx-text-fill: #2e7d32;");
    }

    @FXML
    protected void onSaveProfile() {
        feedbackLabel.setText("");
        String trimmedName = fullNameField.getText() == null ? "" : fullNameField.getText().trim();
        if (trimmedName.isEmpty()) {
            setError("Full Name is required.");
            fullNameField.requestFocus();
            return;
        }

        boolean nameExists = profiles.stream()
                .anyMatch(profile -> profile.getFullName().equalsIgnoreCase(trimmedName));
        if (nameExists) {
            setError("A profile with this name already exists.");
            fullNameField.requestFocus();
            fullNameField.selectAll();
            return;
        }

        String academicStatus = academicStatusCombo.getValue();
        if (academicStatus == null || academicStatus.isBlank()) {
            setError("Select the academic status.");
            academicStatusCombo.requestFocus();
            return;
        }

        boolean employed = jobStatusGroup.getSelectedToggle() == employedRadio;
        String jobDetails = jobDetailsField.getText() == null ? "" : jobDetailsField.getText().trim();
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

        List<String> storedComments = new ArrayList<>(comments);

        StudentProfile profile = new StudentProfile(trimmedName,
                academicStatus,
                employed,
                jobDetails,
                selectedLanguages,
                selectedDatabases,
                preferredRole,
                storedComments,
                whitelist,
                blacklist);

        profiles.add(profile);
        sortProfiles();
        try {
            profileRepository.saveAll(profiles);
            clearForm();
            setSuccess("Profile saved successfully.");
        } catch (IOException exception) {
            profiles.remove(profile);
            setError("Unable to save profile. Please try again.");
        }
    }

    @FXML
    protected void onRefreshProfiles() {
        if (!profilesContainer.isVisible()) {
            onToggleProfiles();
            return;
        }

        if (loadProfiles()) {
            setSuccess("Profiles refreshed.");
        }
    }

    @FXML
    protected void onResetForm() {
        clearForm();
        setSuccess("Form cleared.");
    }

    @FXML
    protected void onBackToHome() throws IOException {
        Stage currentStage = (Stage) feedbackLabel.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("hello-view.fxml"));
        switchScene(currentStage, loader, "Curriculum Setup");
    }

    private void clearForm() {
        fullNameField.clear();
        academicStatusCombo.getSelectionModel().clearSelection();
        jobStatusGroup.selectToggle(unemployedRadio);
        jobDetailsField.clear();
        languagesListView.getSelectionModel().clearSelection();
        databasesListView.getSelectionModel().clearSelection();
        preferredRoleCombo.getSelectionModel().clearSelection();
        comments.clear();
        commentInputArea.clear();
        whitelistCheckBox.setSelected(false);
        blacklistCheckBox.setSelected(false);
    }

    private void sortProfiles() {
        FXCollections.sort(profiles, Comparator.comparing(StudentProfile::getFullName, String.CASE_INSENSITIVE_ORDER));
        profilesTable.sort();
    }

    private void setError(String message) {
        feedbackLabel.setText(message);
        feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
    }

    private void setSuccess(String message) {
        feedbackLabel.setText(message);
        feedbackLabel.setStyle("-fx-text-fill: #2e7d32;");
    }

    @FXML
    protected void onToggleProfiles() {
        boolean makeVisible = !profilesContainer.isVisible();
        profilesContainer.setVisible(makeVisible);
        profilesContainer.setManaged(makeVisible);
        if (makeVisible) {
            boolean loaded = loadProfiles();
            toggleProfilesButton.setText("Hide Stored Profiles");
            if (loaded && profiles.isEmpty()) {
                setSuccess("No stored profiles yet. Save a profile to populate the table.");
            }
        } else {
            toggleProfilesButton.setText("View Stored Profiles");
        }
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
