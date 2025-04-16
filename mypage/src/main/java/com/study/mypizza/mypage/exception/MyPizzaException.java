package com.study.mypizza.mypage.exception;

public class MyPizzaException extends RuntimeException {
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
