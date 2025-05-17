package ru.skillfactory.rxjava.core;

public interface Disposable {
    void dispose();

    boolean isDisposed();
}
