package com.akraml.algo.interpreter;

import java.util.Map;

public final class Algorithm {

    private final String name;
    private Map<String, Object> variables;

    Algorithm(final String name,
                        final Map<String, Object> variables,
                        final Map<String, Object> constants) {
        this.name = name;
        this.variables = variables;
//        this.constants = constants;
    }

    public String getName() {
        return name;
    }

    public void setVariables(final Map<String, Object> variables) {
        this.variables = variables;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

}
