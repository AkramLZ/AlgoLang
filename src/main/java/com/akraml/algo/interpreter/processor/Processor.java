package com.akraml.algo.interpreter.processor;

import com.akraml.algo.interpreter.InterpretationException;

public interface Processor {

    Runnable process(final String s) throws InterpretationException;

}
