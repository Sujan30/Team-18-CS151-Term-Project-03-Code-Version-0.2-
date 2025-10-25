package cs151.application;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Simple flat-file persistence for {@link StudentProfile} records using Base64 delimited columns.
 */
public class StudentProfileRepository {

    private static final String FIELD_DELIMITER = "|";
    private static final String LIST_DELIMITER = ";";

    private final Path storagePath;

    /**
     * Builds a repository targeting the default data folder within the project workspace.
     */
    public StudentProfileRepository() {
        this(Paths.get(System.getProperty("user.dir"), "data", "student-profiles.csv"));
    }

    StudentProfileRepository(Path storagePath) {
        this.storagePath = storagePath;
    }

    /**
     * Loads all stored student profiles sorted alphabetically by name.
     *
     * @return list of stored profiles (empty list when none exist)
     * @throws IOException when the storage file cannot be read
     */
    public List<StudentProfile> loadAll() throws IOException {
        if (Files.notExists(storagePath)) {
            ensureParentDirectory();
            return new ArrayList<>();
        }

        List<String> lines = Files.readAllLines(storagePath, StandardCharsets.UTF_8);
        List<StudentProfile> profiles = new ArrayList<>(lines.size());
        for (String line : lines) {
            if (line == null || line.isBlank()) {
                continue;
            }
            StudentProfile profile = parseLine(line);
            if (profile != null) {
                profiles.add(profile);
            }
        }

        profiles.sort(Comparator.comparing(StudentProfile::getFullName, String.CASE_INSENSITIVE_ORDER));
        return profiles;
    }

    /**
     * Persists the provided profiles, replacing any previously stored entries.
     *
     * @param profiles collection of profiles to save
     * @throws IOException when the storage file cannot be written
     */
    public void saveAll(List<StudentProfile> profiles) throws IOException {
        ensureParentDirectory();
        List<String> sortedLines = profiles.stream()
                .sorted(Comparator.comparing(StudentProfile::getFullName, String.CASE_INSENSITIVE_ORDER))
                .map(this::formatLine)
                .collect(Collectors.toList());

        Files.write(storagePath, sortedLines, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
    }

    /**
     * Deletes the profile whose full name matches the provided value (case-insensitive).
     *
     * @param fullName name of the profile to delete
     * @return {@code true} if a profile was removed, {@code false} otherwise
     * @throws IOException when the storage file cannot be updated
     */
    public boolean deleteByName(String fullName) throws IOException {
        if (fullName == null || fullName.isBlank()) {
            return false;
        }

        List<StudentProfile> profiles = loadAll();
        boolean removed = profiles.removeIf(profile -> profile.getFullName().equalsIgnoreCase(fullName.trim()));
        if (removed) {
            saveAll(profiles);
        }
        return removed;
    }

    private StudentProfile parseLine(String line) {
        String[] segments = line.split("\\|", -1);
        if (segments.length != 10) {
            return null;
        }

        String fullName = decode(segments[0]);
        String academicStatus = decode(segments[1]);
        boolean employed = Boolean.parseBoolean(segments[2]);
        String jobDetails = decode(segments[3]);
        List<String> languages = parseList(segments[4]);
        List<String> databases = parseList(segments[5]);
        String preferredRole = decode(segments[6]);
        List<String> comments = parseList(segments[7]);
        boolean whitelist = Boolean.parseBoolean(segments[8]);
        boolean blacklist = Boolean.parseBoolean(segments[9]);

        return new StudentProfile(fullName, academicStatus, employed, jobDetails, languages, databases, preferredRole,
                comments, whitelist, blacklist);
    }

    private String formatLine(StudentProfile profile) {
        return String.join(FIELD_DELIMITER,
                encode(profile.getFullName()),
                encode(profile.getAcademicStatus()),
                Boolean.toString(profile.isEmployed()),
                encode(profile.getJobDetails()),
                encodeList(profile.getProgrammingLanguages()),
                encodeList(profile.getDatabases()),
                encode(profile.getPreferredRole()),
                encodeList(profile.getComments()),
                Boolean.toString(profile.isWhitelist()),
                Boolean.toString(profile.isBlacklist()));
    }

    private String encode(String value) {
        String nonNull = value == null ? "" : value;
        return Base64.getEncoder().encodeToString(nonNull.getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String encodedValue) {
        byte[] bytes = Base64.getDecoder().decode(encodedValue);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private String encodeList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        return values.stream()
                .map(this::encode)
                .collect(Collectors.joining(LIST_DELIMITER));
    }

    private List<String> parseList(String encoded) {
        if (encoded == null || encoded.isBlank()) {
            return List.of();
        }
        String[] elements = encoded.split(";", -1);
        List<String> decoded = new ArrayList<>(elements.length);
        for (String element : elements) {
            if (element.isEmpty()) {
                continue;
            }
            decoded.add(decode(element));
        }
        return decoded;
    }

    private void ensureParentDirectory() throws IOException {
        Path parent = storagePath.getParent();
        if (parent != null && Files.notExists(parent)) {
            Files.createDirectories(parent);
        }
    }
}
