package pwe.planner.logic.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static pwe.planner.logic.parser.CliSyntax.PREFIX_CODE;
import static pwe.planner.logic.parser.CliSyntax.PREFIX_COREQUISITE;
import static pwe.planner.logic.parser.CliSyntax.PREFIX_CREDITS;
import static pwe.planner.logic.parser.CliSyntax.PREFIX_NAME;
import static pwe.planner.logic.parser.CliSyntax.PREFIX_SEMESTER;
import static pwe.planner.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.ArrayList;
import java.util.List;

import pwe.planner.commons.core.index.Index;
import pwe.planner.logic.CommandHistory;
import pwe.planner.logic.commands.exceptions.CommandException;
import pwe.planner.model.Application;
import pwe.planner.model.Model;
import pwe.planner.model.module.Module;
import pwe.planner.model.module.NameContainsKeywordsPredicate;
import pwe.planner.testutil.EditModuleDescriptorBuilder;

/**
 * Contains helper methods for testing commands.
 */
public class CommandTestUtil {
    // Generic valid attributes
    public static final String VALID_SEMESTER_ONE = "1";
    public static final String VALID_SEMESTER_TWO = "2";
    public static final String VALID_SEMESTER_THREE = "3";
    public static final String VALID_SEMESTER_FOUR = "4";
    public static final String VALID_TAG_HUSBAND = "husband";
    public static final String VALID_TAG_FRIEND = "friend";

    public static final String VALID_CODE_AMY = "AAA0000A";
    public static final String VALID_CODE_BOB = "BBB1111B";
    public static final String VALID_NAME_AMY = "Amy Bee";
    public static final String VALID_NAME_BOB = "Bob Choo";
    public static final String VALID_CREDITS_AMY = "0";
    public static final String VALID_CREDITS_BOB = "999";
    public static final String VALID_SEMESTER_AMY_ONE = VALID_SEMESTER_ONE;
    public static final String VALID_SEMESTER_AMY_TWO = VALID_SEMESTER_TWO;
    public static final String VALID_SEMESTER_BOB_THREE = VALID_SEMESTER_THREE;
    public static final String VALID_SEMESTER_BOB_FOUR = VALID_SEMESTER_FOUR;

    public static final String CODE_DESC_AMY = " " + PREFIX_CODE + VALID_CODE_AMY;
    public static final String CODE_DESC_BOB = " " + PREFIX_CODE + VALID_CODE_BOB;
    public static final String NAME_DESC_AMY = " " + PREFIX_NAME + VALID_NAME_AMY;
    public static final String NAME_DESC_BOB = " " + PREFIX_NAME + VALID_NAME_BOB;
    public static final String CREDITS_DESC_AMY = " " + PREFIX_CREDITS + VALID_CREDITS_AMY;
    public static final String CREDITS_DESC_BOB = " " + PREFIX_CREDITS + VALID_CREDITS_BOB;
    public static final String SEMESTER_DESC_AMY_ONE = " " + PREFIX_SEMESTER + VALID_SEMESTER_AMY_ONE;
    public static final String SEMESTER_DESC_AMY_TWO = " " + PREFIX_SEMESTER + VALID_SEMESTER_AMY_TWO;
    public static final String SEMESTERS_DESC_AMY = SEMESTER_DESC_AMY_ONE + SEMESTER_DESC_AMY_TWO;
    public static final String SEMESTER_DESC_BOB_THREE = " " + PREFIX_SEMESTER + VALID_SEMESTER_BOB_THREE;
    public static final String SEMESTER_DESC_BOB_FOUR = " " + PREFIX_SEMESTER + VALID_SEMESTER_BOB_FOUR;
    public static final String SEMESTERS_DESC_BOB = SEMESTER_DESC_BOB_THREE + SEMESTER_DESC_BOB_FOUR;
    public static final String TAG_DESC_FRIEND = " " + PREFIX_TAG + VALID_TAG_FRIEND;
    public static final String TAG_DESC_HUSBAND = " " + PREFIX_TAG + VALID_TAG_HUSBAND;

    public static final String INVALID_CODE_DESC = " " + PREFIX_CODE; // empty string not allowed for codes
    public static final String INVALID_NAME_DESC = " " + PREFIX_NAME + "Jame§"; // '§' not allowed in names
    public static final String INVALID_CREDITS_DESC = " " + PREFIX_CREDITS + "1a"; // 'a' not allowed in credits
    public static final String INVALID_SEMESTER_DESC_ZERO = " " + PREFIX_SEMESTER + "0"; // 0 is out of range (1-4)
    public static final String INVALID_SEMESTER_DESC_FIVE = " " + PREFIX_SEMESTER + "5"; // 5 is out of range (1-4)
    public static final String INVALID_COREQUISITE_DESC = " " + PREFIX_COREQUISITE + "1000";
    public static final String INVALID_TAG_DESC = " " + PREFIX_TAG + "hubby*"; // '*' not allowed in tags

    public static final String PREAMBLE_WHITESPACE = "\t  \r  \n";
    public static final String PREAMBLE_NON_EMPTY = "NonEmptyPreamble";

    public static final EditCommand.EditModuleDescriptor DESC_AMY;
    public static final EditCommand.EditModuleDescriptor DESC_BOB;

    static {
        DESC_AMY = new EditModuleDescriptorBuilder()
                .withCode(VALID_CODE_AMY)
                .withName(VALID_NAME_AMY)
                .withCredits(VALID_CREDITS_AMY)
                .withSemesters(VALID_SEMESTER_AMY_ONE, VALID_SEMESTER_AMY_TWO)
                .withTags(VALID_TAG_FRIEND)
                .build();

        DESC_BOB = new EditModuleDescriptorBuilder()
                .withName(VALID_NAME_BOB)
                .withCredits(VALID_CREDITS_BOB)
                .withCode(VALID_CODE_BOB)
                .withSemesters(VALID_SEMESTER_BOB_THREE, VALID_SEMESTER_BOB_FOUR)
                .withTags(VALID_TAG_HUSBAND, VALID_TAG_FRIEND)
                .build();
    }

    /**
     * Executes the given {@code command}, confirms that <br>
     * - the returned {@link CommandResult} matches {@code expectedCommandResult} <br>
     * - the {@code actualModel} matches {@code expectedModel} <br>
     * - the {@code actualCommandHistory} remains unchanged.
     */
    public static void assertCommandSuccess(Command command, Model actualModel, CommandHistory actualCommandHistory,
            CommandResult expectedCommandResult, Model expectedModel) {
        CommandHistory expectedCommandHistory = new CommandHistory(actualCommandHistory);
        try {
            CommandResult result = command.execute(actualModel, actualCommandHistory);
            assertEquals(expectedCommandResult, result);
            assertEquals(expectedModel, actualModel);
            assertEquals(expectedCommandHistory, actualCommandHistory);
        } catch (CommandException ce) {
            throw new AssertionError("Execution of command should not fail.", ce);
        }
    }

    /**
     * Convenience wrapper to {@link #assertCommandSuccess(Command, Model, CommandHistory, CommandResult, Model)}
     * that takes a string {@code expectedMessage}.
     */
    public static void assertCommandSuccess(Command command, Model actualModel, CommandHistory actualCommandHistory,
            String expectedMessage, Model expectedModel) {
        CommandResult expectedCommandResult = new CommandResult(expectedMessage);
        assertCommandSuccess(command, actualModel, actualCommandHistory, expectedCommandResult, expectedModel);
    }

    /**
     * Executes the given {@code command}, confirms that <br>
     * - a {@code CommandException} is thrown <br>
     * - the CommandException message matches {@code expectedMessage} <br>
     * - the application, filtered module list and selected module in {@code actualModel} remain unchanged <br>
     * - {@code actualCommandHistory} remains unchanged.
     */
    public static void assertCommandFailure(Command command, Model actualModel, CommandHistory actualCommandHistory,
            String expectedMessage) {
        // we are unable to defensively copy the model for comparison later, so we can
        // only do so by copying its components.
        Application expectedApplication = new Application(actualModel.getApplication());
        List<Module> expectedFilteredList = new ArrayList<>(actualModel.getFilteredModuleList());
        Module expectedSelectedModule = actualModel.getSelectedModule();

        CommandHistory expectedCommandHistory = new CommandHistory(actualCommandHistory);

        try {
            command.execute(actualModel, actualCommandHistory);
            throw new AssertionError("The expected CommandException was not thrown.");
        } catch (CommandException e) {
            assertEquals(expectedMessage, e.getMessage());
            assertEquals(expectedApplication, actualModel.getApplication());
            assertEquals(expectedFilteredList, actualModel.getFilteredModuleList());
            assertEquals(expectedSelectedModule, actualModel.getSelectedModule());
            assertEquals(expectedCommandHistory, actualCommandHistory);
        }
    }

    /**
     * Updates {@code model}'s filtered list to show only the module at the given {@code targetIndex} in the
     * {@code model}'s application.
     */
    public static void showModuleAtIndex(Model model, Index targetIndex) {
        assertTrue(targetIndex.getZeroBased() < model.getFilteredModuleList().size());

        Module module = model.getFilteredModuleList().get(targetIndex.getZeroBased());
        model.updateFilteredModuleList(new NameContainsKeywordsPredicate<>(module.getName().toString()));

        assertEquals(1, model.getFilteredModuleList().size());
    }

    /**
     * Deletes the first module in {@code model}'s filtered list from {@code model}'s application.
     */
    public static void deleteFirstModule(Model model) {
        Module firstModule = model.getFilteredModuleList().get(0);
        model.deleteModule(firstModule);
        model.commitApplication();
    }

}
