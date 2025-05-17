package ru.skillfactory.rxjava.core;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class DisposableObserver<T> implements Observer<T>, Disposable {
    private final AtomicBoolean errorHandled = new AtomicBoolean(false);
    private volatile boolean disposed;

    @Override
    public void dispose() {
        disposed = true;
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    @Override
    public void onError(Throwable t) {
        if (errorHandled.compareAndSet(false, true)) {
            dispose();
            handleError(t); // Абстрактный метод для обработки ошибки
        }
    }

    protected abstract void handleError(Throwable t);

    @Override
    public void onComplete() {
        dispose();
    }
}