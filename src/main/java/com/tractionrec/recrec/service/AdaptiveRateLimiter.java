package com.tractionrec.recrec.service;

import com.tractionrec.recrec.RecRecApplication;

import java.time.Duration;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Adaptive rate limiter that adjusts concurrency based on success/failure rates
 * to handle API rate limiting gracefully.
 */
public class AdaptiveRateLimiter {

    private final int minConcurrency;
    private final int maxConcurrency;

    // ✅ NEVER REPLACED - prevents deadlocks
    private final Semaphore semaphore;
    private final AtomicInteger targetConcurrency;
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicLong lastAdjustmentTime = new AtomicLong(System.currentTimeMillis());

    // Circuit breaker state
    private volatile boolean circuitOpen = false;
    private volatile long circuitOpenTime = 0;
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    private static final Duration CIRCUIT_RECOVERY_TIME = Duration.ofSeconds(30);
    private static final int FAILURE_THRESHOLD = 10; // Open circuit after 10 consecutive failures
    private static final Duration ADJUSTMENT_INTERVAL = Duration.ofSeconds(30); // Adjust every 30 seconds (more conservative)

    public AdaptiveRateLimiter(int initialConcurrency, int minConcurrency, int maxConcurrency) {
        this.minConcurrency = minConcurrency;
        this.maxConcurrency = maxConcurrency;
        this.targetConcurrency = new AtomicInteger(initialConcurrency);
        this.semaphore = new Semaphore(initialConcurrency, true); // ✅ Created once, never replaced
    }

    /**
     * Acquire a permit to make a request
     */
    public void acquire() throws InterruptedException {
        // Check circuit breaker
        if (circuitOpen) {
            if (System.currentTimeMillis() - circuitOpenTime > CIRCUIT_RECOVERY_TIME.toMillis()) {
                // Try to close circuit and allow this request to proceed
                closeCircuit();
            } else {
                // Circuit is open, wait before allowing request
                Thread.sleep(1000);
                throw new RuntimeException("Circuit breaker is open - API appears to be overwhelmed");
            }
        }

        semaphore.acquire();
    }

    /**
     * Release a permit after request completion
     */
    public void release() {
        semaphore.release();
    }

    /**
     * Record a successful request
     */
    public void recordSuccess() {
        successCount.incrementAndGet();
        consecutiveFailures.set(0); // Reset consecutive failure count on success
        adjustConcurrencyIfNeeded();
    }

    /**
     * Record a failed request (timeout, connection error, etc.)
     */
    public void recordFailure() {
        failureCount.incrementAndGet();
        int consecutive = consecutiveFailures.incrementAndGet();

        // Open circuit breaker if too many consecutive failures
        if (consecutive >= FAILURE_THRESHOLD && !circuitOpen) {
            openCircuit();
        }

        adjustConcurrencyIfNeeded();
    }

    /**
     * Adjust concurrency based on success/failure rates
     */
    private void adjustConcurrencyIfNeeded() {
        long now = System.currentTimeMillis();
        long lastAdjustment = lastAdjustmentTime.get();

        if (now - lastAdjustment < ADJUSTMENT_INTERVAL.toMillis()) {
            return; // Too soon to adjust
        }

        if (!lastAdjustmentTime.compareAndSet(lastAdjustment, now)) {
            return; // Another thread is adjusting
        }

        int successes = successCount.getAndSet(0);
        int failures = failureCount.getAndSet(0);
        int total = successes + failures;

        if (total < 5) {
            return; // Not enough data to make adjustment
        }

        double failureRate = (double) failures / total;
        int current = targetConcurrency.get();
        int newConcurrency = current;

        if (failureRate > 0.3) { // More than 30% failure rate (more conservative)
            // Reduce concurrency in increments of 5
            newConcurrency = Math.max(minConcurrency, current - 5);
            System.err.printf("High failure rate (%.1f%%), reducing concurrency from %d to %d%n",
                failureRate * 100, current, newConcurrency);
        } else if (failureRate < 0.05 && current < maxConcurrency) { // Less than 5% failure rate
            // Gradually increase concurrency in increments of 5
            newConcurrency = Math.min(maxConcurrency, current + 5);
            System.out.printf("Low failure rate (%.1f%%), increasing concurrency from %d to %d%n",
                failureRate * 100, current, newConcurrency);
        }

        if (newConcurrency != current) {
            updateConcurrency(newConcurrency);
        }
    }

    /**
     * Update the concurrency limit safely without replacing semaphore
     * ✅ DEADLOCK-FREE: Adjusts permits on existing semaphore
     */
    private void updateConcurrency(int newConcurrency) {
        int oldConcurrency = targetConcurrency.getAndSet(newConcurrency);
        int delta = newConcurrency - oldConcurrency;

        if (delta > 0) {
            // Increase concurrency: add permits
            semaphore.release(delta);
            System.out.printf("Increased concurrency from %d to %d (+%d permits)%n",
                oldConcurrency, newConcurrency, delta);
        } else if (delta < 0) {
            // Decrease concurrency: drain and reset permits
            int drained = semaphore.drainPermits();
            semaphore.release(Math.max(0, newConcurrency));
            System.out.printf("Decreased concurrency from %d to %d (drained %d, released %d)%n",
                oldConcurrency, newConcurrency, drained, Math.max(0, newConcurrency));
        }
    }

    /**
     * Open the circuit breaker
     */
    private void openCircuit() {
        circuitOpen = true;
        circuitOpenTime = System.currentTimeMillis();
        System.err.println("Circuit breaker OPENED - too many failures, pausing requests");

        // Drastically reduce concurrency when circuit opens
        updateConcurrency(Math.max(1, minConcurrency / 2));
    }

    /**
     * Close the circuit breaker
     */
    private void closeCircuit() {
        circuitOpen = false;
        System.out.println("Circuit breaker CLOSED - resuming normal operation");

        // Reset to minimum concurrency when circuit closes
        updateConcurrency(minConcurrency);
        successCount.set(0);
        failureCount.set(0);
        consecutiveFailures.set(0);
    }

    /**
     * Get current concurrency limit
     */
    public int getCurrentConcurrency() {
        return targetConcurrency.get();
    }

    /**
     * Check if circuit breaker is open
     */
    public boolean isCircuitOpen() {
        return circuitOpen;
    }

    /**
     * Get current success/failure statistics
     */
    public String getStats() {
        return String.format("Concurrency: %d, Circuit: %s, Success: %d, Failures: %d, Consecutive: %d",
            getCurrentConcurrency(),
            circuitOpen ? "OPEN" : "CLOSED",
            successCount.get(),
            failureCount.get(),
            consecutiveFailures.get());
    }
}
