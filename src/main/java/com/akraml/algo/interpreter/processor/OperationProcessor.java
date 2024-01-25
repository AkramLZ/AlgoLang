package com.akraml.algo.interpreter.processor;

import com.akraml.algo.interpreter.AlgoInterpreter;
import com.akraml.algo.interpreter.InterpretationException;
import com.akraml.algo.interpreter.token.TokenType;
import com.akraml.algo.interpreter.token.Undefined;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Operation processor uses JavaScript Engine, expect it to be slow.
 */
public final class OperationProcessor implements Processor {

    private static final Pattern PATTERN = Pattern.compile("\\b\\w+\\b");
    private static final ScriptEngineManager SCRIPT_ENGINE_MANAGER = new ScriptEngineManager();
    private static final ScriptEngine SCRIPT_ENGINE = SCRIPT_ENGINE_MANAGER.getEngineByName("nashorn");

    private final AlgoInterpreter interpreter;

    public OperationProcessor(final AlgoInterpreter interpreter) {
        this.interpreter = interpreter;
    }

    @Override
    public Runnable process(String s) throws InterpretationException {
        return () -> {
            final String[] variableSplit = s.trim().split("=", 2);
            if (variableSplit.length != 2) {
                throw new RuntimeException(new InterpretationException("Invalid statement\n" + s));
            }
            final String variableName = variableSplit[0].trim();
            if (interpreter.getConstants().containsKey(variableName)) {
                throw new RuntimeException(new InterpretationException("Modifying constant\n" + s));
            }
            if (!interpreter.getVariables().containsKey(variableName)) {
                throw new RuntimeException(new InterpretationException("Undefined variable\n" + s));
            }
            String expression = variableSplit[1];
            if (expression.startsWith(" ")) {
                expression = expression.replaceAll("^\\s+", ""); // Remove whitespaces from the first.
            }
            if (expression.endsWith(" ")) {
                expression = expression.replaceAll("\\s+$", ""); // Remove whitespaces from the end.
            }
            expression = expression.replaceAll(";+$", ""); // Remove ; from the last
            // Now I assume we have a clean expression, let's identify the expression to check if it's valid or not.
            final Matcher matcher = PATTERN.matcher(expression);
            while (matcher.find()) {
                final String variable = matcher.group();
                if (isNumber(variable)) continue;
                if (interpreter.getConstants().containsKey(variable)) {
                    expression = expression.replaceAll("\\b" + variable + "\\b", interpreter.getConstants().get(variable).toString());
                } else if (interpreter.getVariables().containsKey(variable)) {
                    expression = expression.replaceAll("\\b" + variable + "\\b", interpreter.getVariables().get(variable).toString());
                } else {
                    throw new RuntimeException(new InterpretationException("Undefined variable '" + variable + "'\n" + s));
                }
            }
            // Let's eval now.
            try {
                final Object result = SCRIPT_ENGINE.eval(expression);
                if (!(result instanceof Number)) {
                    throw new RuntimeException(new InterpretationException("Unexpected result, expected number but found " +
                            result.getClass().getSimpleName() + "\n" + s));
                }
                final Object storedVariable = interpreter.getVariables().get(variableName);
                if (storedVariable instanceof Undefined undefined) {
                    if (undefined.getDataType() == TokenType.DataType.DOUBLE) {
                        final Double dR = (Double) result;
                        interpreter.getVariables().replace(variableName, dR);
                    } else if (undefined.getDataType() == TokenType.DataType.INTEGER) {
                        final Integer iR = (Integer) result;
                        interpreter.getVariables().replace(variableName, iR);
                    } else {
                        throw new RuntimeException(new InterpretationException("Wrong data type, numeric operation for " +
                                undefined.getDataType().getName() + s));
                    }
                } else {
                    if (storedVariable instanceof Integer) {
                        final Integer iR = (Integer) result;
                        interpreter.getVariables().replace(variableName, iR);
                    } else if (storedVariable instanceof Double) {
                        final Double dR = (Double) result;
                        interpreter.getVariables().replace(variableName, dR);
                    } else {
                        throw new RuntimeException(new InterpretationException("Wrong data type, numeric operation for " +
                                storedVariable.getClass().getSimpleName() + s));
                    }
                }
            } catch (final Exception exception) {
                exception.printStackTrace();
                //throw new RuntimeException(new InterpretationException("Failed to eval expression\n" + s));
            }
        };
    }

    private boolean isNumber(final String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

}
