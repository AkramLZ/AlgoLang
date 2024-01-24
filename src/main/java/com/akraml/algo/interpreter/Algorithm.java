package com.akraml.algo.interpreter;

import com.akraml.algo.interpreter.processor.Processor;

import java.util.LinkedHashMap;
import java.util.Map;

public final class Algorithm {

    private final String name;
    private Map<String, Object> variables, constants;
    private final Map<String, Processor> commands = new LinkedHashMap<>();

    Algorithm(final String name) {
        this.name = name;
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

    public Map<String, Processor> getCommands() {
        return commands;
    }

    public Map<String, Object> getConstants() {
        return constants;
    }

    public void startExecution() {
        for (final Map.Entry<String, Processor> entry : commands.entrySet()) {
            try {
                entry.getValue().process(entry.getKey()).run();
            } catch (final Exception exception) {
                exception.printStackTrace();
                throw new RuntimeException(new InterpretationException("Failed to interpret this line\n" + entry.getKey(), exception));
            }
        }
    }

    public void setConstants(Map<String, Object> constants) {
        this.constants = constants;
    }
}
