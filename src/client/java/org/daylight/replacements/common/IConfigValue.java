package org.daylight.replacements.common;


public interface IConfigValue<T> {
    T get();
    T getCached();
    void set(T value);
    void save();
}