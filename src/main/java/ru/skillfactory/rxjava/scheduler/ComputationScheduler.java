package ru.skillfactory.rxjava.scheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ComputationScheduler implements Scheduler {
    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();
    private final ExecutorService executor;

    public ComputationScheduler() {
        this.executor = Executors.newFixedThreadPool(THREAD_COUNT);
    }

    @Override
    public void execute(Runnable task) {
        executor.execute(task);
    }
}