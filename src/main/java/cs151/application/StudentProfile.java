package cs151.application;

import java.util.Collections;
import java.util.List;

/**
 * Represents a student profile captured through the Define Student Profiles workflow.
 */
public class StudentProfile {

    private final String fullName;
    private final String academicStatus;
    private final boolean employed;
    private final String jobDetails;
    private final List<String> programmingLanguages;
    private final List<String> databases;
    private final String preferredRole;
    private final List<String> comments;
    private final boolean whitelist;
    private final boolean blacklist;

    public StudentProfile(String fullName,
                          String academicStatus,
                          boolean employed,
                          String jobDetails,
                          List<String> programmingLanguages,
                          List<String> databases,
                          String preferredRole,
                          List<String> comments,
                          boolean whitelist,
                          boolean blacklist) {
        this.fullName = fullName;
        this.academicStatus = academicStatus;
        this.employed = employed;
        this.jobDetails = jobDetails;
        this.programmingLanguages = List.copyOf(programmingLanguages);
        this.databases = List.copyOf(databases);
        this.preferredRole = preferredRole;
        this.comments = List.copyOf(comments);
        this.whitelist = whitelist;
        this.blacklist = blacklist;
    }

    public String getFullName() {
        return fullName;
    }

    public String getAcademicStatus() {
        return academicStatus;
    }

    public boolean isEmployed() {
        return employed;
    }

    public String getJobDetails() {
        return jobDetails;
    }

    /**
     * @return a display-ready representation of the student's employment details
     */
    public String getJobDetailsDisplay() {
        if (jobDetails == null || jobDetails.isBlank()) {
            return employed ? "" : "N/A";
        }
        return jobDetails;
    }

    public List<String> getProgrammingLanguages() {
        return Collections.unmodifiableList(programmingLanguages);
    }

    public List<String> getDatabases() {
        return Collections.unmodifiableList(databases);
    }

    public String getPreferredRole() {
        return preferredRole;
    }

    public List<String> getComments() {
        return Collections.unmodifiableList(comments);
    }

    public boolean isWhitelist() {
        return whitelist;
    }

    public boolean isBlacklist() {
        return blacklist;
    }

    public String getJobStatusLabel() {
        return employed ? "Employed" : "Not Employed";
    }

    public String formatLanguages() {
        return String.join(", ", programmingLanguages);
    }

    public String formatDatabases() {
        return String.join(", ", databases);
    }

    public String formatComments() {
        if (comments == null || comments.isEmpty()) {
            return "";
        }

        StringBuilder out = new StringBuilder();
        String previousDate = null;
        for (int i = 0; i < comments.size(); i++) {
            String c = comments.get(i);
            if (c == null || c.isBlank()) {
                continue;
            }

            // Expect comments to be prefixed with date in yyyy-MM-dd (e.g. "2025-10-24 - note")
            String datePrefix = null;
            int dash = c.indexOf(" - ");
            if (dash > 0 && dash >= 8) {
                datePrefix = c.substring(0, dash);
            }

            if (previousDate != null && datePrefix != null && !datePrefix.equals(previousDate)) {
                // separate different days with a blank line for readability
                out.append(System.lineSeparator());
            }

            if (out.length() > 0) {
                out.append(System.lineSeparator());
            }
            out.append(c);

            previousDate = datePrefix != null ? datePrefix : previousDate;
        }

        return out.toString();
    }

    public String getWhitelistLabel() {
        return whitelist ? "Yes" : "No";
    }

    public String getBlacklistLabel() {
        return blacklist ? "Yes" : "No";
    }
}
