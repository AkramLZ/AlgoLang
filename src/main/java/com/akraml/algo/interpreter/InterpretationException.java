package com.akraml.algo.interpreter;

/**
 * This exception will be thrown if there is an error or mistake related to interpretation.
 * It's meant to show if  there is any error and show in which line is it.
 */
public final class InterpretationException extends Exception {

    public InterpretationException(String message) {
        super(message);
    }

    public InterpretationException(String message,
                                   Throwable cause) {
        super(message, cause);
    }

}
