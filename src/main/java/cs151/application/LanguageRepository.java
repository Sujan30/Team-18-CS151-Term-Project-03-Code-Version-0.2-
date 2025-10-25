package cs151.application;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides basic file-backed persistence for programming language definitions.
 * <p>
 * Entries are stored one per line within the project's {@code data/} directory so that the application can reload them
 * on launch without additional infrastructure.
 * </p>
 */
public class LanguageRepository {

    private final Path storagePath;

    /**
      * Builds a repository using the default storage location inside the project workspace.
     */
    public LanguageRepository() {
          this(Paths.get(System.getProperty("user.dir"), "data", "programming-languages.csv"));
    }

    LanguageRepository(Path storagePath) {
        this.storagePath = storagePath;
    }

    /**
     * Loads all programming languages currently stored on disk.
     *
     * @return ordered list of languages (empty list when none exist)
     * @throws IOException when the storage file cannot be accessed
     */
    public List<ProgrammingLanguage> loadAll() throws IOException {
        if (Files.notExists(storagePath)) {
            ensureParentDirectory();
            return new ArrayList<>();
        }

        try {
            return Files.readAllLines(storagePath, StandardCharsets.UTF_8).stream()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .map(ProgrammingLanguage::new)
                    .collect(Collectors.toCollection(ArrayList::new));
        } catch (IOException ioException) {
            throw new IOException("Unable to read stored programming languages", ioException);
        }
    }

    /**
     * Persists the provided language list to disk, replacing any previous content.
     *
     * @param languages collection of languages to store
     * @throws IOException when the storage file cannot be written
     */
    public void saveAll(List<ProgrammingLanguage> languages) throws IOException {
        ensureParentDirectory();
        List<String> sortedNames = languages.stream()
                .map(ProgrammingLanguage::getName)
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());

        try {
            Files.write(storagePath, sortedNames, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (IOException ioException) {
            throw new IOException("Unable to save programming languages", ioException);
        }
    }

    private void ensureParentDirectory() throws IOException {
        Path parent = storagePath.getParent();
        if (parent != null && Files.notExists(parent)) {
            Files.createDirectories(parent);
        }
    }
}
