package com.github.gerard.python4j.exceptions;

public class PythonException extends Exception {

    public PythonException() {
        super();
    }

    public PythonException(String message) {
        super(message);
    }

    public PythonException(String message, Throwable cause) {
        super(message, cause);
    }
}
