package pwe.planner.logic.parser;

import static java.util.Objects.requireNonNull;
import static pwe.planner.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static pwe.planner.commons.core.Messages.MESSAGE_UNKNOWN_COMMAND;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pwe.planner.logic.commands.AddCommand;
import pwe.planner.logic.commands.ClearCommand;
import pwe.planner.logic.commands.Command;
import pwe.planner.logic.commands.DeleteCommand;
import pwe.planner.logic.commands.EditCommand;
import pwe.planner.logic.commands.ExitCommand;
import pwe.planner.logic.commands.FindCommand;
import pwe.planner.logic.commands.HelpCommand;
import pwe.planner.logic.commands.HistoryCommand;
import pwe.planner.logic.commands.ListCommand;
import pwe.planner.logic.commands.PlannerAddCommand;
import pwe.planner.logic.commands.PlannerListAllCommand;
import pwe.planner.logic.commands.PlannerMoveCommand;
import pwe.planner.logic.commands.PlannerRemoveCommand;
import pwe.planner.logic.commands.PlannerSuggestCommand;
import pwe.planner.logic.commands.RedoCommand;
import pwe.planner.logic.commands.RequirementAddCommand;
import pwe.planner.logic.commands.RequirementListCommand;
import pwe.planner.logic.commands.RequirementRemoveCommand;
import pwe.planner.logic.commands.SelectCommand;
import pwe.planner.logic.commands.UndoCommand;
import pwe.planner.logic.parser.exceptions.ParseException;

/**
 * Parses user input.
 */
public class CommandParser {

    /**
     * Used for initial separation of command word and args.
     */
    private static final Pattern BASIC_COMMAND_FORMAT = Pattern.compile("(?<commandWord>\\S+)(?<arguments>.*)");

    /**
     * Parses user input into command for execution.
     *
     * @param userInput full user input string
     * @return the command based on the user input
     * @throws ParseException if the user input does not conform the expected format
     */
    public Command parseCommand(String userInput) throws ParseException {
        requireNonNull(userInput);

        final Matcher matcher = BASIC_COMMAND_FORMAT.matcher(userInput.trim());
        if (!matcher.matches()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE));
        }

        final String commandWord = matcher.group("commandWord");
        final String arguments = matcher.group("arguments");
        switch (commandWord) {

        case AddCommand.COMMAND_WORD:
            return new AddCommandParser().parse(arguments);

        case EditCommand.COMMAND_WORD:
            return new EditCommandParser().parse(arguments);

        case SelectCommand.COMMAND_WORD:
            return new SelectCommandParser().parse(arguments);

        case DeleteCommand.COMMAND_WORD:
            return new DeleteCommandParser().parse(arguments);

        case ClearCommand.COMMAND_WORD:
            return new ClearCommandParser().parse(arguments);

        case FindCommand.COMMAND_WORD:
            return new FindCommandParser().parse(arguments);

        case ListCommand.COMMAND_WORD:
            return new ListCommand();

        case RequirementAddCommand.COMMAND_WORD:
            return new RequirementAddCommandParser().parse(arguments);

        case RequirementListCommand.COMMAND_WORD:
            return new RequirementListCommand();

        case RequirementRemoveCommand.COMMAND_WORD:
            return new RequirementRemoveCommandParser().parse(arguments);

        case HistoryCommand.COMMAND_WORD:
            return new HistoryCommand();

        case PlannerListAllCommand.COMMAND_WORD:
            return new PlannerListAllCommand();

        case PlannerMoveCommand.COMMAND_WORD:
            return new PlannerMoveCommandParser().parse(arguments);

        case ExitCommand.COMMAND_WORD:
            return new ExitCommand();

        case HelpCommand.COMMAND_WORD:
            return new HelpCommand();

        case UndoCommand.COMMAND_WORD:
            return new UndoCommand();

        case RedoCommand.COMMAND_WORD:
            return new RedoCommand();

        case PlannerAddCommand.COMMAND_WORD:
            return new PlannerAddCommandParser().parse(arguments);

        case PlannerRemoveCommand.COMMAND_WORD:
            return new PlannerRemoveCommandParser().parse(arguments);

        case PlannerSuggestCommand.COMMAND_WORD:
            return new PlannerSuggestCommandParser().parse(arguments);

        default:
            throw new ParseException(MESSAGE_UNKNOWN_COMMAND);
        }
    }
}
