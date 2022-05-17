package com.matyrobbrt.sectionprotection.util;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Value<T> implements Supplier<T>, Consumer<T> {
    private T value;
    private final T defaultValue;

    public Value(T defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public T get() {
        return value == null ? defaultValue : value;
    }

    @Override
    public void accept(T t) {
        this.value = t;
    }
}
