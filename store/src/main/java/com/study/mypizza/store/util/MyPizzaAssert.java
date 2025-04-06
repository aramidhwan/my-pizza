package com.study.mypizza.store.util;

import com.study.mypizza.store.exception.MyPizzaException;
import org.springframework.util.Assert;

public class MyPizzaAssert extends Assert {
    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new MyPizzaException(message);
        }
    }
}
