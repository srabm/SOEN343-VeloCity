package com.concordia.velocity.reservation;

import java.util.concurrent.*;

public class ReservationManager {

    private static final ScheduledExecutorService SCHEDULER =
            Executors.newScheduledThreadPool(4);

    private static final ConcurrentHashMap<String, ScheduledFuture<?>> activeTimers =
            new ConcurrentHashMap<>();

    public static void schedule(String bikeId, Runnable task, long minutes) {
        cancel(bikeId); // cancel old timer if exists
        ScheduledFuture<?> future = SCHEDULER.schedule(task, minutes, TimeUnit.MINUTES);
        activeTimers.put(bikeId, future);
    }

    public static void cancel(String bikeId) {
        ScheduledFuture<?> task = activeTimers.remove(bikeId);
        if (task != null && !task.isDone()) {
            task.cancel(true);
            System.out.println("[DEBUG] Cancelled reservation timer for bike " + bikeId);
        }
    }
}
