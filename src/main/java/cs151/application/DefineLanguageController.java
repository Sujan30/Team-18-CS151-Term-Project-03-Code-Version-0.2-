package cs151.application;

import java.io.IOException;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Controller for the Define Programming Language page. Provides validation and navigation back to the home page.
 */
public class DefineLanguageController {

    private final LanguageRepository repository = new LanguageRepository();
    private final ObservableList<ProgrammingLanguage> languages = FXCollections.observableArrayList();

    @FXML
    private TextField nameField;

    @FXML
    private Label feedbackLabel;

    @FXML
    private TableView<ProgrammingLanguage> languagesTable;

    @FXML
    private TableColumn<ProgrammingLanguage, String> nameColumn;

    @FXML
    private VBox rootContainer;

    @FXML
    private void initialize() {
        nameColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getName()));
        languagesTable.setItems(languages);
        nameColumn.setSortType(TableColumn.SortType.ASCENDING);
        languagesTable.getSortOrder().clear();
        languagesTable.getSortOrder().add(nameColumn);
        refreshFromStorage();
        Platform.runLater(() -> rootContainer.requestFocus());
    }

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

        boolean alreadyExists = languages.stream()
                .anyMatch(language -> language.getName().equalsIgnoreCase(enteredName));
        if (alreadyExists) {
            feedbackLabel.setText("Language already exists.");
            feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
            nameField.selectAll();
            nameField.requestFocus();
            return;
        }

        ProgrammingLanguage newLanguage = new ProgrammingLanguage(enteredName);
        languages.add(newLanguage);
        sortLanguages();

        try {
            repository.saveAll(languages);
            feedbackLabel.setText(String.format("Saved programming language: %s", enteredName));
            feedbackLabel.setStyle("-fx-text-fill: #2e7d32;");
            nameField.clear();
            nameField.requestFocus();
        } catch (IOException exception) {
            languages.remove(newLanguage);
            feedbackLabel.setText("Unable to store language; please try again.");
            feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
        }
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
        switchScene(currentStage, loader, "Curriculum Setup");
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

    private void refreshFromStorage() {
        try {
            List<ProgrammingLanguage> storedLanguages = repository.loadAll();
            languages.setAll(storedLanguages);
            sortLanguages();
        } catch (IOException exception) {
            feedbackLabel.setText("Unable to load stored languages. Add a new entry to begin.");
            feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
        }
    }

    private void sortLanguages() {
        FXCollections.sort(languages, Comparator.comparing(ProgrammingLanguage::getName, String.CASE_INSENSITIVE_ORDER));
        languagesTable.sort();
    }
}
