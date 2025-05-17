package ru.skillfactory.rxjava.core;

public interface Observer<T> {
    void onNext(T item);

    void onError(Throwable t);

    void onComplete();

    default boolean isDisposed() {
        return false;
    }
}