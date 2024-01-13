package com.akraml.algo;

import com.akraml.algo.interpreter.AlgoInterpreter;
import com.akraml.algo.interpreter.Algorithm;
import com.akraml.algo.interpreter.InterpretationException;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * The main class of the interpreter application.
 */
public final class AlgoMain {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Usage: ./algo <file>");
            return;
        }
        final String fileName = args[0];
        if (!fileName.toLowerCase().endsWith(".algo")) {
            System.out.println("Please specify a valid file, file name should end with '.algo'");
            return;
        }
        final File file = new File(fileName);
        if (!file.exists()) {
            System.out.println("Cannot find a file with this name.");
            return;
        }
        final AlgoInterpreter interpreter = new AlgoInterpreter(file);
        interpreter.readFile();
        Algorithm algorithm = null;
        try {
            algorithm = interpreter.interpret();
        } catch (final InterpretationException exception) {
            System.err.println("[INTERPRETER] " + exception.getMessage());
            return;
        }
        if (algorithm == null) { // This one should be impossible, so idc.
            System.err.println("[CRITICAL ERROR] Algorithm is null, could be an interpretation issue?");
            return;
        }
        System.out.println("[TEST] Algorithm name is " + algorithm.getName());
        for (Map.Entry<String, Object> entry : algorithm.getVariables().entrySet()) {
            System.out.println("[TEST] Found variable " + entry.getKey() +
                    (entry.getValue().getClass().getSimpleName().equals("Object") ? " with no value assigned" : " with value " + entry.getValue()));
        }
    }

}
