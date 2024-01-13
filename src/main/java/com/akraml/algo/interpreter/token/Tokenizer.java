package com.akraml.algo.interpreter.token;

public final class Tokenizer {

    public Object tokenize(final TokenType.DataType dataType,
                           final String variable) throws TokenizeException {
        // Check if data type is a number.
        if (variable == null) return new Object();
        if (Number.class.isAssignableFrom(dataType.getTypeClass())) {
            switch (dataType) {
                case DOUBLE -> {
                    if (!TokenType.DataType.DOUBLE.isValid(variable))
                        throw new TokenizeException("Illegal real number type '" + variable + "'");
                    return Double.parseDouble(variable);
                }
                case INTEGER -> {
                    if (!TokenType.DataType.INTEGER.isValid(variable))
                        throw new TokenizeException("Illegal integer type '" + variable + "'");
                    return Integer.parseInt(variable);
                }
                case STRING -> {

                }
            }
        } else if (dataType == TokenType.DataType.STRING) {
            if (!TokenType.DataType.STRING.isValid(variable))
                throw new TokenizeException("Illegal string type '" + variable + "'");
            return variable.substring(1, variable.length() - 1).replace("\\\"", "\"");
        };
        return null;
    }

}
