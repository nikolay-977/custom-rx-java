package ru.skillfactory.rxjava.core;

import org.junit.jupiter.api.Test;
import ru.skillfactory.rxjava.scheduler.SingleThreadScheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class ObservableTest {

    // 1. Базовый функционал Observable

    // Проверяет создание Observable, эмиссию элементов и подписку
    // Верифицирует корректность получения элементов и завершения потока
    @Test
    public void testCreateAndSubscribe() {
        List<Integer> received = new ArrayList<>();

        Observable<Integer> observable = Observable.create(observer -> {
            observer.onNext(1);
            observer.onNext(2);
            observer.onNext(3);
            observer.onComplete();
        });

        observable.subscribe(new Observer<Integer>() {
            @Override
            public void onNext(Integer item) {
                received.add(item);
            }

            @Override
            public void onError(Throwable t) {
                fail("Неожиданная ошибка: " + t.getMessage());
            }

            @Override
            public void onComplete() {
                assertEquals(3, received.size(), "Должно быть получено 3 элемента");
            }
        });

        assertEquals(3, received.size(), "Размер списка полученных элементов должен быть 3");
        assertEquals(Integer.valueOf(1), received.get(0), "Первый элемент должен быть 1");
        assertEquals(Integer.valueOf(2), received.get(1), "Второй элемент должен быть 2");
        assertEquals(Integer.valueOf(3), received.get(2), "Третий элемент должен быть 3");
    }

    // 2. Тесты операторов преобразования

    // Проверяет работу оператора map
    // Убеждается, что преобразование применяется к каждому элементу
    @Test
    public void testMapOperator() {
        List<String> received = new ArrayList<>();

        Observable<Integer> observable = Observable.create(observer -> {
            observer.onNext(1);
            observer.onNext(2);
            observer.onNext(3);
            observer.onComplete();
        });

        Observable<String> mappedObservable = observable.map(i -> "Number " + i);

        mappedObservable.subscribe(new Observer<>() {
            @Override
            public void onNext(String item) {
                received.add(item);
            }

            @Override
            public void onError(Throwable t) {
                fail("Неожиданная ошибка при выполнении map: " + t.getMessage());
            }

            @Override
            public void onComplete() {
                assertEquals(3, received.size(), "Должно быть получено 3 преобразованных элемента");
            }
        });

        assertEquals(3, received.size(), "Должно быть 3 преобразованных элемента");
        assertEquals("Number 1", received.get(0), "Первый элемент должен быть 'Number 1'");
        assertEquals("Number 2", received.get(1), "Второй элемент должен быть 'Number 2'");
        assertEquals("Number 3", received.get(2), "Третий элемент должен быть 'Number 3'");
    }

    // Тестирует оператор filter
    // Проверяет, что фильтрация работает согласно предикату
    @Test
    public void testFilterOperator() {
        List<Integer> received = new ArrayList<>();

        Observable<Integer> observable = Observable.create(observer -> {
            observer.onNext(1);
            observer.onNext(2);
            observer.onNext(3);
            observer.onNext(4);
            observer.onComplete();
        });

        Observable<Integer> filteredObservable = observable.filter(i -> i % 2 == 0);

        filteredObservable.subscribe(new Observer<Integer>() {
            @Override
            public void onNext(Integer item) {
                received.add(item);
            }

            @Override
            public void onError(Throwable t) {
                fail("Неожиданная ошибка при фильтрации: " + t.getMessage());
            }

            @Override
            public void onComplete() {
                assertEquals(2, received.size(), "Должно остаться 2 четных элемента");
            }
        });

        assertEquals(2, received.size(), "Должно быть 2 отфильтрованных элемента");
        assertEquals(Integer.valueOf(2), received.get(0), "Первый четный элемент должен быть 2");
        assertEquals(Integer.valueOf(4), received.get(1), "Второй четный элемент должен быть 4");
    }

    // Проверяет оператор flatMap
    // Убеждается, что преобразование в новый Observable работает корректно
    @Test
    public void testFlatMapOperator() {
        List<String> received = new ArrayList<>();

        Observable<Integer> observable = Observable.create(observer -> {
            observer.onNext(1);
            observer.onNext(2);
            observer.onComplete();
        });

        Observable<String> flatMappedObservable = observable.flatMap(i ->
                Observable.create(observer -> {
                    observer.onNext("A" + i);
                    observer.onNext("B" + i);
                    observer.onComplete();
                }));

        flatMappedObservable.subscribe(new Observer<String>() {
            @Override
            public void onNext(String item) {
                received.add(item);
            }

            @Override
            public void onError(Throwable t) {
                fail("Неожиданная ошибка при flatMap: " + t.getMessage());
            }

            @Override
            public void onComplete() {
                assertEquals(4, received.size(), "Должно быть 4 элемента после flatMap");
            }
        });

        assertEquals(4, received.size(), "Должно быть 4 элемента после преобразования");
        assertEquals("A1", received.get(0), "Первый элемент должен быть 'A1'");
        assertEquals("B1", received.get(1), "Второй элемент должен быть 'B1'");
        assertEquals("A2", received.get(2), "Третий элемент должен быть 'A2'");
        assertEquals("B2", received.get(3), "Четвертый элемент должен быть 'B2'");
    }

    // 3. Тесты управления потоками выполнения

    // Проверяет работу subscribeOn
    // Убеждается, что подписка выполняется в указанном потоке
    @Test
    public void testSubscribeOn() throws InterruptedException {
        AtomicInteger counter = new AtomicInteger();
        SingleThreadScheduler scheduler = new SingleThreadScheduler();

        Observable<Integer> observable = Observable.create(observer -> {
            assertNotEquals(Thread.currentThread().getName(), "main");
            observer.onNext(1);
            observer.onNext(2);
            observer.onNext(3);
            observer.onComplete();
        });

        Observable<Integer> scheduledObservable = observable.subscribeOn(scheduler);

        scheduledObservable.subscribe(new Observer<Integer>() {
            @Override
            public void onNext(Integer item) {
                counter.incrementAndGet();
            }

            @Override
            public void onError(Throwable t) {
                fail("Неожиданная ошибка в subscribeOn: " + t.getMessage());
            }

            @Override
            public void onComplete() {
                assertEquals(3, counter.get(), "Должно быть получено 3 элемента");
            }
        });

        Thread.sleep(100);
        assertEquals(3, counter.get(), "Счетчик должен показать 3 полученных элемента");
    }

    // Тестирует observeOn
    // Проверяет, что обработка элементов происходит в нужном потоке
    @Test
    public void testObserveOn() throws InterruptedException {
        AtomicInteger counter = new AtomicInteger();
        SingleThreadScheduler scheduler = new SingleThreadScheduler();

        Observable<Integer> observable = Observable.create(observer -> {
            observer.onNext(1);
            observer.onNext(2);
            observer.onNext(3);
            observer.onComplete();
        });

        Observable<Integer> scheduledObservable = observable.observeOn(scheduler);

        scheduledObservable.subscribe(new Observer<Integer>() {
            @Override
            public void onNext(Integer item) {
                assertNotEquals("main", Thread.currentThread().getName(),
                        "Обработка должна выполняться не в основном потоке");
                counter.incrementAndGet();
            }

            @Override
            public void onError(Throwable t) {
                fail("Неожиданная ошибка в observeOn: " + t.getMessage());
            }

            @Override
            public void onComplete() {
                assertEquals(3, counter.get(), "Должно быть обработано 3 элемента");
            }
        });

        Thread.sleep(100);
        assertEquals(3, counter.get(), "Счетчик должен показать 3 обработанных элемента");
    }

    // Проверяет комбинацию subscribeOn и observeOn
    // Убеждается, что подписка и обработка происходят в разных потоках
    @Test
    public void testCombineSchedulers() throws InterruptedException {
        AtomicInteger counter = new AtomicInteger();
        SingleThreadScheduler subscribeScheduler = new SingleThreadScheduler();
        SingleThreadScheduler observeScheduler = new SingleThreadScheduler();

        Observable<Integer> observable = Observable.create((Observable.OnSubscribe<Integer>) observer -> {
                    assertNotEquals("main", Thread.currentThread().getName(),
                            "Эмиссия должна выполняться не в основном потоке");
                    observer.onNext(1);
                    observer.onNext(2);
                    observer.onComplete();
                })
                .subscribeOn(subscribeScheduler)
                .observeOn(observeScheduler);

        observable.subscribe(new Observer<>() {
            @Override
            public void onNext(Integer item) {
                assertNotEquals("main", Thread.currentThread().getName(),
                        "Обработка должна выполняться не в основном потоке");
                counter.incrementAndGet();
            }

            @Override
            public void onError(Throwable t) {
                fail("Неожиданная ошибка при комбинации scheduler'ов: " + t.getMessage());
            }

            @Override
            public void onComplete() {
                assertEquals(2, counter.get(), "Должно быть обработано 2 элемента");
            }
        });

        Thread.sleep(100);
        assertEquals(2, counter.get(), "Счетчик должен показать 2 обработанных элемента");
    }

    // 4. Тесты обработки ошибок

    // Проверяет базовый сценарий обработки ошибок
    // Убеждается, что onError прерывает поток
    @Test
    public void testErrorHandling() {
        List<Integer> received = new ArrayList<>();
        List<Throwable> errors = new ArrayList<>();
        AtomicBoolean completed = new AtomicBoolean(false);

        Observable<Integer> observable = Observable.create(observer -> {
            try {
                observer.onNext(1);
                observer.onError(new RuntimeException("Test error"));
                // Эти вызовы не должны выполняться из-за предыдущей ошибки
                observer.onNext(2);
                observer.onComplete();
            } catch (Exception e) {
                observer.onError(e);
            }
        });

        observable.subscribe(new Observer<Integer>() {
            @Override
            public void onNext(Integer item) {
                received.add(item);
            }

            @Override
            public void onError(Throwable t) {
                errors.add(t);
            }

            @Override
            public void onComplete() {
                completed.set(true);
            }
        });

        assertEquals(1, received.size(), "Должен быть получен только 1 элемент до ошибки");
        assertEquals(Integer.valueOf(1), received.get(0), "Полученный элемент должен быть 1");
        assertEquals(1, errors.size(), "Должна быть получена 1 ошибка");
        assertEquals("Test error", errors.get(0).getMessage(),
                "Сообщение ошибки должно соответствовать ожидаемому");
        assertFalse(completed.get(), "Поток не должен завершаться успешно после ошибки");
    }

    // Проверяет автоматическую отписку при ошибках
    // Убеждается, что isDisposed возвращает true после onError
    @Test
    public void testDisposeOnError() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean disposedWhenErrorHandled = new AtomicBoolean(false);

        DisposableObserver<Integer> disposableObserver = new DisposableObserver<Integer>() {
            @Override
            protected void handleError(Throwable t) {
                disposedWhenErrorHandled.set(isDisposed());
                latch.countDown();
            }

            @Override
            public void onNext(Integer item) {
                fail("Не должно быть получено элементов после ошибки");
            }

            @Override
            public void onComplete() {
                fail("Поток не должен завершаться успешно после ошибки");
            }
        };

        Observable.<Integer>create(observer -> {
            observer.onError(new RuntimeException("Test"));
            observer.onNext(2);
        }).subscribeWith(disposableObserver);

        assertTrue(latch.await(1, TimeUnit.SECONDS),
                "Обработка ошибки должна завершиться в течение 1 секунды");
        assertTrue(disposedWhenErrorHandled.get(),
                "Observer должен быть помечен как disposed при обработке ошибки");
        assertTrue(disposableObserver.isDisposed(),
                "Observer должен оставаться disposed после обработки ошибки");
    }

    // 5. Тесты Disposable и управления подписками

    // Проверяет ручное управление подпиской
    // Тестирует методы dispose() и isDisposed()
    @Test
    public void testDisposable() {
        List<Integer> received = new ArrayList<>();
        List<Integer> emitted = new ArrayList<>();

        DisposableObserver<Integer> observer = new DisposableObserver<>() {
            @Override
            public void onNext(Integer item) {
                received.add(item);
                if (item == 2) {
                    dispose();
                }
            }

            @Override
            public void onError(Throwable t) {
                fail("Неожиданная ошибка: " + t.getMessage());
            }

            @Override
            protected void handleError(Throwable t) {
                fail("Неожиданная ошибка при комбинации обработке ошибки" + t.getMessage());
            }

            @Override
            public void onComplete() {
                fail("Поток не должен завершаться в этом тесте");
            }
        };

        Observable<Integer> observable = Observable.create(obs -> {
            try {
                for (int i = 1; i <= 3; i++) {
                    if (!observer.isDisposed()) {
                        emitted.add(i);
                        obs.onNext(i);
                    } else {
                        return;
                    }
                }
                if (!observer.isDisposed()) {
                    obs.onComplete();
                }
            } catch (Exception e) {
                if (!observer.isDisposed()) {
                    obs.onError(e);
                }
            }
        });

        observable.subscribeWith(observer);

        assertEquals(2, received.size(), "Должно быть получено 2 элемента до отписки");
        assertEquals(Integer.valueOf(1), received.get(0), "Первый элемент должен быть 1");
        assertEquals(Integer.valueOf(2), received.get(1), "Второй элемент должен быть 2");

        assertEquals(2, emitted.size(), "Должно быть эмитировано 2 элемента");
        assertEquals(Integer.valueOf(1), emitted.get(0), "Первый эмитированный элемент должен быть 1");
        assertEquals(Integer.valueOf(2), emitted.get(1), "Второй эмитированный элемент должен быть 2");

        assertTrue(observer.isDisposed(), "Observer должен быть в состоянии disposed после отписки");
    }

    // Проверяет поведение при множественных подписках
    // Убеждается, что каждый подписчик получает все элементы
    @Test
    public void testMultipleSubscriptions() {
        AtomicInteger emitCount = new AtomicInteger();
        AtomicInteger nextCount = new AtomicInteger();
        AtomicInteger completeCount = new AtomicInteger();

        Observable<Integer> observable = Observable.create(observer -> {
            emitCount.incrementAndGet();
            observer.onNext(1);
            observer.onComplete();
        });

        observable.subscribe(new Observer<>() {
            @Override
            public void onNext(Integer item) {
                nextCount.incrementAndGet();
            }

            @Override
            public void onError(Throwable t) {
                fail("Не ожидалась ошибка в подписчике: " + t.getMessage());
            }

            @Override
            public void onComplete() {
                completeCount.incrementAndGet();
            }
        });

        observable.subscribe(new Observer<>() {
            @Override
            public void onNext(Integer item) {
                nextCount.incrementAndGet();
            }

            @Override
            public void onError(Throwable t) {
                fail("Не ожидалась ошибка в подписчике: " + t.getMessage());
            }

            @Override
            public void onComplete() {
                completeCount.incrementAndGet();
            }
        });

        assertEquals(2, emitCount.get(), "Источник должен эмитировать данные для каждого подписчика");
        assertEquals(2, nextCount.get(), "Каждый подписчик должен получить элемент");
        assertEquals(2, completeCount.get(), "Каждый подписчик должен получить onComplete");
    }
}