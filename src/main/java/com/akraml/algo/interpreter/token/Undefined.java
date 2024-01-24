package com.akraml.algo.interpreter.token;

public class Undefined {

    private final TokenType.DataType dataType;

    public Undefined(final TokenType.DataType dataType) {
        this.dataType = dataType;
    }

    public TokenType.DataType getDataType() {
        return dataType;
    }

}
