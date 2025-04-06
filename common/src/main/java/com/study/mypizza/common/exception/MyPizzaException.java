package com.study.mypizza.common.exception;

public class MyPizzaException extends Exception {
    public MyPizzaException() {
        super();
    }

    public MyPizzaException(String message) {
        super(message);
    }

    public MyPizzaException(String message, Throwable cause) {
        super(message, cause);
    }

    public MyPizzaException(Throwable cause) {
        super(cause);
    }
}
