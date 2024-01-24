package com.akraml.algo.interpreter.processor;

import com.akraml.algo.interpreter.AlgoInterpreter;
import com.akraml.algo.interpreter.InterpretationException;
import com.akraml.algo.interpreter.token.TokenType;
import com.akraml.algo.interpreter.token.Undefined;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ReadProcessor implements Processor {

    private static final Pattern PATTERN = Pattern.compile("Read\\((.*?)\\);");

    private final AlgoInterpreter interpreter;

    public ReadProcessor(final AlgoInterpreter interpreter) {
        this.interpreter = interpreter;
    }

    @Override
    public Runnable process(String s) throws InterpretationException {
        return () -> {
            final Matcher matcher = PATTERN.matcher(s);
            final Scanner scanner = new Scanner(System.in);
            if (!matcher.find()) throw new RuntimeException(new InterpretationException("Failed to interpret write segment"));
            final String variablesInside = matcher.group(1);
            final String[] names = variablesInside.split(",\\s*");
            for (final String name : names) {
                final String variableName = name.trim();
                if (interpreter.getConstants().containsKey(variableName)) {
                    throw new RuntimeException(new InterpretationException("Cannot read a constant value '" + variableName + "'"));
                }
                if (!interpreter.getVariables().containsKey(variableName)) {
                    throw new RuntimeException(new InterpretationException("Invalid variable '" + variableName + "'"));
                }
                final Object obj = interpreter.getVariables().get(variableName);
                if (obj instanceof Undefined undefined) {
                    if (undefined.getDataType() == TokenType.DataType.STRING) {
                        final String scanned = scanner.nextLine();
                        interpreter.getVariables().replace(variableName, scanned);
                    } else if (undefined.getDataType() == TokenType.DataType.INTEGER) {
                        final int scanned = scanner.nextInt();
                        interpreter.getVariables().replace(variableName, scanned);
                    } else if (undefined.getDataType() == TokenType.DataType.DOUBLE) {
                        final double scanned = scanner.nextDouble();
                        interpreter.getVariables().replace(variableName, scanned);
                    }
                } else {
                    final TokenType.DataType dataType = TokenType.DataType.getByClass(obj.getClass());
                    if (dataType != null) {
                        if (dataType == TokenType.DataType.STRING) {
                            final String scanned = scanner.nextLine();
                            interpreter.getVariables().replace(variableName, scanned);
                        } else if (dataType == TokenType.DataType.INTEGER) {
                            final int scanned = scanner.nextInt();
                            interpreter.getVariables().replace(variableName, scanned);
                        } else if (dataType == TokenType.DataType.DOUBLE) {
                            final double scanned = scanner.nextDouble();
                            interpreter.getVariables().replace(variableName, scanned);
                        }
                    }
                }
            }
        };
    }
}
