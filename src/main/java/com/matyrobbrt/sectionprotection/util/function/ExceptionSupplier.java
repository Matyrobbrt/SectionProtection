package com.matyrobbrt.sectionprotection.util.function;

public interface ExceptionSupplier<T> {
    T get() throws Throwable;
}
