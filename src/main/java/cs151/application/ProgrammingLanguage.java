package cs151.application;

/**
 * Represents a programming language entry captured from the Define Programming Languages form.
 */
public class ProgrammingLanguage {

    private final String name;

    /**
     * Creates a new language descriptor.
     *
     * @param name display name of the language
     */
    public ProgrammingLanguage(String name) {
        this.name = name;
    }

    /**
     * @return the language name as entered by the user
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
