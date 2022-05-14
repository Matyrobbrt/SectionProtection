package com.matyrobbrt.sectionprotection.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ListanableObject<T> {

    private final List<Consumer<T>> listeners = new ArrayList<>();

    public void set(T obj) {
        listeners.forEach(c -> c.accept(obj));
        listeners.clear();
    }

    public void addListener(Consumer<T> listener) {
        this.listeners.add(listener);
    }

}
