package pwe.planner.logic.parser;

import static pwe.planner.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static pwe.planner.logic.parser.CliSyntax.PREFIX_CODE;

import java.util.Set;
import java.util.stream.Stream;

import pwe.planner.logic.commands.PlannerRemoveCommand;
import pwe.planner.logic.parser.exceptions.ParseException;
import pwe.planner.model.module.Code;

/**
 * Parses input arguments and creates a new PlannerRemoveCommand object.
 */
public class PlannerRemoveCommandParser implements Parser<PlannerRemoveCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the PlannerRemoveCommand
     * and returns an PlannerRemoveCommand object for execution.
     * @throws ParseException if the user input does not conform the expected format.
     */
    public PlannerRemoveCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_CODE);

        if (!arePrefixesPresent(argMultimap, PREFIX_CODE)
                || !argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, PlannerRemoveCommand.MESSAGE_USAGE));
        }

        Set<Code> codes = ParserUtil.parseCodes(argMultimap.getAllValues(PREFIX_CODE));

        return new PlannerRemoveCommand(codes);
    }

    /**
     * Returns true if none of the prefixes contains empty {@code Optional} values in the given
     * {@code ArgumentMultimap}.
     */
    private static boolean arePrefixesPresent(ArgumentMultimap argumentMultimap, Prefix... prefixes) {
        return Stream.of(prefixes).allMatch(prefix -> argumentMultimap.getValue(prefix).isPresent());
    }

}
