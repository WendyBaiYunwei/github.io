package pwe.planner.model.module;

import static java.util.Objects.requireNonNull;
import static pwe.planner.commons.util.AppUtil.checkArgument;

/**
 * Represents a Module's code in the application.
 * Guarantees: immutable; is valid as declared in {@link #isValidCode(String)}
 */
public class Code implements Comparable<Code> {

    public static final String MESSAGE_CONSTRAINTS =
            "Codes should begin with two alphabets, followed by four digits, and may optionally end with an alphabet. "
            + "Codes should not be blank";

    /*
     * The first character of the code must not be a whitespace,
     * otherwise " " (a blank string) becomes a valid input.
     */
    public static final String VALIDATION_REGEX = "[\\p{Alpha}]{2,3}[\\p{Digit}]{4}[\\p{Alpha}]?";

    public final String value;

    /**
     * Constructs an {@code Code}.
     *
     * @param code A valid code.
     */
    public Code(String code) {
        requireNonNull(code);
        checkArgument(isValidCode(code), MESSAGE_CONSTRAINTS);

        value = code.toUpperCase();
    }

    /**
     * Returns true if a given string is a valid code.
     */
    public static boolean isValidCode(String test) {
        requireNonNull(test);

        return test.matches(VALIDATION_REGEX);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof Code // instanceof handles nulls
                && value.equals(((Code) other).value)); // state check
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public int compareTo(Code other) {
        return value.compareTo(other.value);
    }
}
