package ru.skillfactory.rxjava.core;

import ru.skillfactory.rxjava.scheduler.Scheduler;

import java.util.function.Function;
import java.util.function.Predicate;

public class Observable<T> {
    private final OnSubscribe<T> onSubscribe;

    private Observable(OnSubscribe<T> onSubscribe) {
        this.onSubscribe = onSubscribe;
    }

    public static <T> Observable<T> create(OnSubscribe<T> onSubscribe) {
        return new Observable<>(onSubscribe);
    }

    public void subscribe(Observer<T> observer) {
        try {
            onSubscribe.call(new Observer<T>() {
                private volatile boolean done = false;

                @Override
                public void onNext(T item) {
                    if (!done && !observer.isDisposed()) {
                        observer.onNext(item);
                    }
                }

                @Override
                public void onError(Throwable t) {
                    if (!done && !observer.isDisposed()) {
                        done = true;
                        observer.onError(t);
                    }
                }

                @Override
                public void onComplete() {
                    if (!done && !observer.isDisposed()) {
                        done = true;
                        observer.onComplete();
                    }
                }
            });
        } catch (Throwable t) {
            if (!observer.isDisposed()) {
                observer.onError(t);
            }
        }
    }

    public <R> Observable<R> map(Function<T, R> mapper) {
        return new Observable<>(observer ->
                subscribe(new Observer<T>() {
                    @Override
                    public void onNext(T item) {
                        observer.onNext(mapper.apply(item));
                    }

                    @Override
                    public void onError(Throwable t) {
                        observer.onError(t);
                    }

                    @Override
                    public void onComplete() {
                        observer.onComplete();
                    }
                }));
    }

    public Observable<T> filter(Predicate<T> predicate) {
        return new Observable<>(observer ->
                subscribe(new Observer<T>() {
                    @Override
                    public void onNext(T item) {
                        if (predicate.test(item)) {
                            observer.onNext(item);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        observer.onError(t);
                    }

                    @Override
                    public void onComplete() {
                        observer.onComplete();
                    }
                }));
    }

    public <R> Observable<R> flatMap(Function<T, Observable<R>> mapper) {
        return new Observable<>(observer ->
                subscribe(new Observer<T>() {
                    @Override
                    public void onNext(T item) {
                        try {
                            mapper.apply(item).subscribe(new Observer<R>() {
                                @Override
                                public void onNext(R mappedItem) {
                                    observer.onNext(mappedItem);
                                }

                                @Override
                                public void onError(Throwable t) {
                                    observer.onError(t);
                                }

                                @Override
                                public void onComplete() {
                                    // Do nothing, wait for outer observable to complete
                                }
                            });
                        } catch (Exception e) {
                            observer.onError(e);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        observer.onError(t);
                    }

                    @Override
                    public void onComplete() {
                        observer.onComplete();
                    }
                }));
    }

    public Observable<T> subscribeOn(Scheduler scheduler) {
        return new Observable<>(observer ->
                scheduler.execute(() -> subscribe(observer)));
    }

    public Observable<T> observeOn(Scheduler scheduler) {
        return new Observable<>(observer ->
                subscribe(new Observer<T>() {
                    @Override
                    public void onNext(T item) {
                        scheduler.execute(() -> observer.onNext(item));
                    }

                    @Override
                    public void onError(Throwable t) {
                        scheduler.execute(() -> observer.onError(t));
                    }

                    @Override
                    public void onComplete() {
                        scheduler.execute(observer::onComplete);
                    }
                }));
    }

    public Disposable subscribeWith(DisposableObserver<T> observer) {
        subscribe(observer);
        return observer;
    }

    public interface OnSubscribe<T> {
        void call(Observer<T> observer);
    }
}