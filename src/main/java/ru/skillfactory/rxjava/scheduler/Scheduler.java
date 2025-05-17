package ru.skillfactory.rxjava.scheduler;

public interface Scheduler {
    void execute(Runnable task);
}