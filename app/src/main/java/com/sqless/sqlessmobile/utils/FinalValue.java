package com.sqless.sqlessmobile.utils;

/**
 * Created by Morgan on 26/03/2018.
 */

public class FinalValue<T> {

    public FinalValue(T value) {
        this.value = value;
    }

    public FinalValue() {
    }

    private T value;

    public T getValue() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }
}
