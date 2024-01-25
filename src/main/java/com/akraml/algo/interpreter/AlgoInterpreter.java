package com.akraml.algo.interpreter;

import com.akraml.algo.interpreter.processor.OperationProcessor;
import com.akraml.algo.interpreter.processor.Processor;
import com.akraml.algo.interpreter.processor.ReadProcessor;
import com.akraml.algo.interpreter.processor.WriteProcessor;
import com.akraml.algo.interpreter.token.TokenType;
import com.akraml.algo.interpreter.token.TokenizeException;
import com.akraml.algo.interpreter.token.Tokenizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * This class will handle file translation from readable code into processable instructions.
 */
public final class AlgoInterpreter {

    private static final Pattern VALIDATION_PATTERN = Pattern.compile("^[A-Za-z0-9_]+$");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^[0-9].*");

    private final File file;
    private final List<String> fileContent = new ArrayList<>();
    private final Tokenizer tokenizer = new Tokenizer();
    private final Map<String, Object> variables = new HashMap<>(), constants = new HashMap<>();
    private final Map<String, Processor> processorMap = new HashMap<>();
    private final OperationProcessor operationProcessor = new OperationProcessor(this);

    public AlgoInterpreter(final File file) {
        this.file = file;
        processorMap.put("Write", new WriteProcessor(this));
        processorMap.put("Read", new ReadProcessor(this));
    }

    public void readFile() throws FileNotFoundException {
        // Initialize scanner and start reading file content.S
        final Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            fileContent.add(scanner.nextLine());
        }
    }

    public Algorithm interpret() throws InterpretationException {
        // Check if file is empty
        if (fileContent.stream().filter(str -> !str.trim().isEmpty()).toList().isEmpty()) {
            throw new InterpretationException("File is empty");
        }
        // Now, we will start the interpreter.
        int currentLine = 0;

        Algorithm algorithm = null;

        // Perform order check.
        boolean algorithmPresent = false,
                variablesPresent = false,
                constantsPresent = false,
                beginPresent = false;
        for (String str : fileContent) {
            currentLine++;
            if (str.trim().startsWith("//")) continue;
            str = removeComments(str);
            // Identify algorithm name first.
            if (!variablesPresent && !constantsPresent && !beginPresent && !algorithmPresent) {
                if (str.startsWith("Algorithm ")) {
                    algorithmPresent = true;
                    continue;
                }
            }
            // Check for out-of-order with algorithm keyword.
            if (variablesPresent || constantsPresent || beginPresent) {
                if (str.startsWith("Algorithm ")) {
                    throw new InterpretationException("Error in line " + currentLine + ": Algorithm name should be first");
                }
            }
            // Check for out of order with constants keyword
            if (constantsPresent && str.equals("Constants")) {
                throw new InterpretationException("Error in line " + currentLine + ": Constants keyword duplication");
            }
            // Check for out of order with variables keyword
            if (variablesPresent && str.equals("Variables")) {
                throw new InterpretationException("Error in line " + currentLine + ": Variables keyword duplication");
            }
            // Check for begin duplication
            if (beginPresent && str.equals("Begin")) {
                throw new InterpretationException("Error in line " + currentLine + ": Begin keyword duplication");
            }
            // Identify constants now.
            if (!beginPresent && !variablesPresent && str.equals("Variables")) {
                variablesPresent = true;
                continue;
            }
            if (!beginPresent && !constantsPresent && str.equals("Constants")) {
                constantsPresent = true;
                continue;
            }
            if (!beginPresent && str.equals("Begin")) {
                beginPresent = true;
            }
        }
        if (!algorithmPresent) {
            throw new InterpretationException("Algorithm name is missing");
        }
        if (!beginPresent) {
            throw new InterpretationException("Algorithm begin body is missing");
        }

        // Initialize file name.
            currentLine = 0;
        for (String str : fileContent) {
            currentLine++;
            if (str.trim().startsWith("//")) continue;
            str = removeComments(str);
            if (!str.trim().isEmpty() && !str.startsWith("Algorithm ")) {
                throw new InterpretationException("Error in line " + currentLine +
                        ": The file should start with 'Algorithm <algorithm name>'");
            }
            if (!str.endsWith(";")) { // Check for EOL (End of line)
                throw new InterpretationException("Error in line " + currentLine +
                        ": Semicolon ';' is missing in the end of the line.");
            }
            String algorithmName = str.replaceFirst("Algorithm ", "");
            algorithmName = algorithmName.substring(0, algorithmName.length() - 1);
            if (!VALIDATION_PATTERN.matcher(algorithmName).matches()) {
                throw new InterpretationException("Error in line " + currentLine +
                        ": Invalid algorithm name '" + algorithmName + "'");
            }
            if (NUMBER_PATTERN.matcher(algorithmName).matches()) {
                throw new InterpretationException("Error in line " + currentLine +
                        ": Algorithm name should not start with a number.");
            }
            algorithm = new Algorithm(algorithmName);
            break;
        }

        if (algorithm == null) {
            throw new InterpretationException("An unexpected error occurred when initializing the algorithm");
        }

        // Initialize variables now.
        currentLine = 0;
        boolean beganVariables = false;
        for (String str : fileContent) {
            currentLine++;
            if (str.trim().startsWith("//")) continue;
            str = removeComments(str);
            if (!beganVariables && str.equals("Variables")) {
                beganVariables = true;
                continue;
            }
            if (beganVariables) {
                if (str.trim().isEmpty()) break;
                // We will tokenize the string now.
                // Here's how it's going to be tokenized:
                // x:                Integer;
                // ^                   ^
                // variable name     data type
                //
                // Another case: let's say variable is pre-identified.
                // in this case, we will check if there's an equal symbol in variable name's part
                // example: x =       "Hello, World!":            String;
                //          ^              ^                        ^
                //   variable name     variable data              data type
                final int leadingSpaces = countLeadingSpaces(str);
                if (leadingSpaces != 4) {
                    throw new InterpretationException("Error in line " + currentLine +
                            ": Expected 4 white spaces, but found " + countLeadingSpaces(str) + "\n" + str);
                }
                if (!str.contains(":") || !str.contains(";")) {
                    throw new InterpretationException("Error in line " + currentLine +
                            ": Invalid format\n" + str);
                }
                final String line = str.replaceFirst("\\s{2,}", "");
                final String variablePart = line.split(":")[0];
                String variableName, variableData = null;
                if (variablePart.contains("=")) {
                    final String[] values = variablePart.split("=", 2);
                    variableName = values[0].trim();
                    if (values.length == 1 || values[1].trim().isEmpty()) {
                        throw new InterpretationException("Error in line " + currentLine +
                                ": Variable is not identified\n" + str);
                    }
                    variableData = values[1];
                    // Let's replace white spaces at first now.
                    if (variableData.startsWith(" ")) {
                        final int preSpaces = countLeadingSpaces(variableData);
                        for (int i = 0; i < preSpaces; i++) {
                            variableData = variableData.replaceFirst(" ", "");
                        }
                    }
                } else {
                    variableName = variablePart.trim().replaceAll("\\s{2,}", "");
                }
                // Check for variable duplication.
                if (variables.containsKey(variableName) || constants.containsKey(variableName)) {
                    throw new InterpretationException("Error at line " + currentLine +
                            ": Variable name duplication '" + variableName + "'\n" + str);
                }
                final String type = line.split(":")[1].trim().replace(";", "");
                final TokenType.DataType dataType = TokenType.DataType.fromName(type);
                if (dataType == null) {
                    throw new InterpretationException("Error at line " + currentLine +
                            ": Invalid data type '" + type + "'\n" + str);
                }
                try {
                    final Object tokenized = tokenizer.tokenize(dataType, variableData);
                    variables.put(variableName, tokenized);
                } catch (final TokenizeException exception) {
                    throw new InterpretationException("Error at line " + currentLine +
                            ": Failed to tokenize: " + exception.getMessage() + "\n" + str);
                }
            }
        }
        // Now, interpret for begin statement.
        currentLine = 0;
        boolean endPresent = false;
        for (String str : fileContent) {
            currentLine++;
            if (str.trim().startsWith("//")) continue;
            str = removeComments(str);
            if (str.equals("Begin")) {
                for (int j = currentLine; j < fileContent.size(); j++) {
                    currentLine++;
                    if (fileContent.get(j).trim().startsWith("//")) continue;
                    final String line = removeComments(fileContent.get(j));
                    final int spacesCount = countLeadingSpaces(line);
                    if (line.equalsIgnoreCase("End")) {
                        endPresent = true;
                        break;
                    }
                    if (spacesCount != 4) {
                        throw new InterpretationException("Error in line " + currentLine +
                                ": Expected 4 white spaces, but found " + countLeadingSpaces(line) + "\n" + line);
                    }
                    final String command = line.replaceFirst(" {4}", "");
                    if (!command.endsWith(";")) {
                        throw new InterpretationException("Error in line " + currentLine +
                                ": Line should end with ';'\n" + line);
                    }
                    final Processor processor = getProcessor(command);
                    if (processor == null) {
                        throw new InterpretationException("Error in line " + currentLine +
                                ": Invalid statement\n" + line);
                    }
                    algorithm.getCommands().put(command, processor);
                }
                break;
            }
        }
        if (!endPresent) {
            throw new InterpretationException("Algorithm has Begin body without End close\n" +
                    "Suggestion: Add `End` keyword to the end of your algorithm");
        }
        algorithm.setVariables(variables);
        algorithm.setConstants(constants);
        return algorithm;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public Map<String, Object> getConstants() {
        return constants;
    }

    public Tokenizer getTokenizer() {
        return tokenizer;
    }

    public Processor getProcessor(final String command) {
        // Let's first check if it's an operation.

        final String[] values = command.split("=", 2);
        if (values.length == 2) {
            final String variableName = values[0].trim();
            // TODO: Handle for strings too
            if (variables.containsKey(variableName) || constants.containsKey(variableName)) {
                return operationProcessor;
            }
        }

        for (final Map.Entry<String, Processor> entry : processorMap.entrySet()) {
            if (command.trim().startsWith(entry.getKey())) return entry.getValue();
        }
        return null;
    }

    private int countLeadingSpaces(final String input) {
        int count = 0;
        for (char c : input.toCharArray()) {
            if (c == ' ') {
                count++;
            } else {
                break;
            }
        }
        return count;
    }

    private final String removeComments(final String input) {
        return input.replaceAll("//.*", "").replaceAll("\\s+$", "");
    }

}
