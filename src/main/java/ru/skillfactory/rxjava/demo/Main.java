package ru.skillfactory.rxjava.demo;

import ru.skillfactory.rxjava.core.DisposableObserver;
import ru.skillfactory.rxjava.core.Observable;
import ru.skillfactory.rxjava.core.Observer;
import ru.skillfactory.rxjava.scheduler.ComputationScheduler;
import ru.skillfactory.rxjava.scheduler.IOThreadScheduler;
import ru.skillfactory.rxjava.scheduler.Scheduler;

import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Демонстрация RxJava-подобной библиотеки ===");
        System.out.println("Текущий поток: " + Thread.currentThread().getName());

        // 1. Демонстрация базового Observable
        System.out.println("\n1. Базовый Observable:");
        Observable<Integer> basicObservable = Observable.create(observer -> {
            System.out.println("Эмитируем данные в потоке: " + Thread.currentThread().getName());
            observer.onNext(1);
            observer.onNext(2);
            observer.onNext(3);
            observer.onComplete();
        });

        basicObservable.subscribe(new Observer<>() {
            @Override
            public void onNext(Integer item) {
                System.out.println("Получено: " + item + " в потоке: " + Thread.currentThread().getName());
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Ошибка: " + t.getMessage());
            }

            @Override
            public void onComplete() {
                System.out.println("Поток завершен");
            }
        });

        // 2. Демонстрация операторов map и filter
        System.out.println("\n2. Операторы map и filter:");
        Observable<Integer> numbersObservable = Observable.create(observer -> {
            for (int i = 1; i <= 5; i++) {
                observer.onNext(i);
            }
            observer.onComplete();
        });

        Observable<String> filteredAndMapped = numbersObservable
                .filter(i -> i % 2 == 0)  // Оставляем только четные
                .map(i -> "Число " + i);   // Преобразуем в строку

        filteredAndMapped.subscribe(new Observer<>() {
            @Override
            public void onNext(String item) {
                System.out.println("Отфильтровано и преобразовано: " + item);
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Ошибка: " + t.getMessage());
            }

            @Override
            public void onComplete() {
                System.out.println("Преобразование завершено");
            }
        });

        // 3. Демонстрация flatMap
        System.out.println("\n3. Оператор flatMap:");
        Observable<Integer> sourceObservable = Observable.create(observer -> {
            observer.onNext(1);
            observer.onNext(2);
            observer.onComplete();
        });

        Observable<Integer> flatMappedObservable = sourceObservable.flatMap(i ->
                Observable.create(sub -> {
                    sub.onNext(i * 10);
                    sub.onNext(i * 100);
                    sub.onComplete();
                }));

        flatMappedObservable.subscribe(new Observer<>() {
            @Override
            public void onNext(Integer item) {
                System.out.println("FlatMap результат: " + item);
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Ошибка: " + t.getMessage());
            }

            @Override
            public void onComplete() {
                System.out.println("FlatMap завершен");
            }
        });

        // 4. Демонстрация subscribeOn и observeOn
        System.out.println("\n4. Управление потоками выполнения:");
        Observable<Integer> threadedObservable = Observable.create(observer -> {
            System.out.println("Эмитируем в потоке: " + Thread.currentThread().getName());
            observer.onNext(1);
            observer.onNext(2);
            observer.onNext(3);
            observer.onComplete();
        });

        Scheduler ioScheduler = new IOThreadScheduler();
        Scheduler computationScheduler = new ComputationScheduler();

        Observable<Integer> scheduledObservable = threadedObservable
                .subscribeOn(ioScheduler)  // Подписка в IO потоке
                .observeOn(computationScheduler);  // Обработка в computation потоке

        scheduledObservable.subscribe(new Observer<>() {
            @Override
            public void onNext(Integer item) {
                System.out.println("Обработка " + item + " в потоке: " + Thread.currentThread().getName());
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Ошибка: " + t.getMessage());
            }

            @Override
            public void onComplete() {
                System.out.println("Обработка завершена в потоке: " + Thread.currentThread().getName());
            }
        });

        // 5. Демонстрация Disposable
        System.out.println("\n5. Отписка (Disposable):");
        Observable<Integer> disposableObservable = Observable.create(observer -> {
            try {
                for (int i = 1; i <= 10; i++) {
                    if (!(observer).isDisposed()) {
                        System.out.println("Эмитируем: " + i);
                        observer.onNext(i);
                        TimeUnit.MILLISECONDS.sleep(200);
                    } else {
                        return;
                    }
                }
                observer.onComplete();
            } catch (InterruptedException e) {
                observer.onError(e);
            }
        });

        disposableObservable.subscribeWith(new DisposableObserver<>() {
            @Override
            public void onNext(Integer item) {
                System.out.println("Получено: " + item);
                if (item == 3) {
                    System.out.println("Отписываемся на элементе 3");
                    dispose();
                }
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Ошибка: " + t.getMessage());
            }

            @Override
            protected void handleError(Throwable t) {

            }

            @Override
            public void onComplete() {
                System.out.println("Поток завершен (не должно быть видно)");
            }
        });

        // 6. Демонстрация обработки ошибок
        System.out.println("\n6. Обработка ошибок:");
        Observable<Integer> errorObservable = Observable.create(observer -> {
            observer.onNext(1);
            observer.onNext(2);
            observer.onError(new RuntimeException("Искусственная ошибка"));
            observer.onNext(3);  // Не должно быть получено
            observer.onComplete();  // Не должно быть вызвано
        });

        errorObservable.subscribe(new Observer<>() {
            @Override
            public void onNext(Integer item) {
                System.out.println("Получено значение: " + item);
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Обработана ошибка: " + t.getMessage());
            }

            @Override
            public void onComplete() {
                System.out.println("Завершение (не должно быть видно)");
            }
        });

        // Даем время для завершения асинхронных операций
        TimeUnit.SECONDS.sleep(2);
        System.out.println("\n=== Демонстрация завершена ===");
    }
}