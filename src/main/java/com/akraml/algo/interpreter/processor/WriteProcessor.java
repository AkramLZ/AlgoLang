package com.akraml.algo.interpreter.processor;

import com.akraml.algo.interpreter.AlgoInterpreter;
import com.akraml.algo.interpreter.InterpretationException;
import com.akraml.algo.interpreter.token.TokenType;
import com.akraml.algo.interpreter.token.TokenizeException;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class WriteProcessor implements Processor {

    private static final Pattern PATTERN = Pattern.compile("Write\\((.*?)\\);");

    private final AlgoInterpreter interpreter;

    public WriteProcessor(final AlgoInterpreter interpreter) {
        this.interpreter = interpreter;
    }

    @Override
    public Runnable process(String s) throws InterpretationException {
        return () -> {
            final Matcher matcher = PATTERN.matcher(s);
            if (!matcher.find()) throw new RuntimeException(new InterpretationException("Failed to interpret write segment"));
            final String contentInside = matcher.group(1);
            final String[] components = splitOutsideQuotes(contentInside);
            final StringBuilder builder = new StringBuilder();
            for (String component : components) {
                if (component.startsWith(" ")) component = component.replaceFirst("\\s", "");
                if (component.trim().startsWith("\"") || component.trim().endsWith("\"")) {
                    if (!TokenType.DataType.STRING.isValid(component)) {
                        throw new RuntimeException(new InterpretationException("Invalid string: " + component));
                    }
                    try {
                        final String tokenizedComponent = (String) interpreter.getTokenizer().tokenize(TokenType.DataType.STRING, component);
                        builder.append(tokenizedComponent);
                    } catch (TokenizeException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    final String variable = component.trim();
                    if (interpreter.getVariables().containsKey(variable)) {
                        builder.append(interpreter.getVariables().get(variable));
                    } else if (interpreter.getConstants().containsKey(variable)) {
                        builder.append(interpreter.getConstants().get(variable));
                    } else {
                        throw new RuntimeException(new InterpretationException("Invalid variable: " + component));
                    }
                }
            }
            System.out.print(builder);
        };
    }

    private String[] splitOutsideQuotes(final String input) {
        return input.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
    }

}
