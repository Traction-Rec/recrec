package com.tractionrec.recrec.service;

import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

/**
 * Utility class for implementing retry logic with exponential backoff
 * for handling API rate limiting and connection timeouts.
 */
public class RetryUtil {
    
    private static final Random RANDOM = new Random();
    private static final int DEFAULT_MAX_RETRIES = 3;
    private static final Duration DEFAULT_INITIAL_DELAY = Duration.ofSeconds(1);
    private static final Duration DEFAULT_MAX_DELAY = Duration.ofSeconds(30);
    private static final double DEFAULT_BACKOFF_MULTIPLIER = 2.0;
    private static final double DEFAULT_JITTER_FACTOR = 0.1;
    
    /**
     * Retry a callable operation with exponential backoff for connection timeouts
     */
    public static <T> T retryWithBackoff(Callable<T> operation) throws Exception {
        return retryWithBackoff(operation, DEFAULT_MAX_RETRIES, DEFAULT_INITIAL_DELAY, DEFAULT_MAX_DELAY);
    }
    
    /**
     * Retry a callable operation with custom parameters
     */
    public static <T> T retryWithBackoff(
            Callable<T> operation, 
            int maxRetries, 
            Duration initialDelay, 
            Duration maxDelay) throws Exception {
        
        Exception lastException = null;
        Duration currentDelay = initialDelay;
        
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastException = e;
                
                // Only retry on specific timeout/connection exceptions
                if (!shouldRetry(e) || attempt == maxRetries) {
                    throw e;
                }
                
                // Calculate delay with jitter
                long delayMs = calculateDelayWithJitter(currentDelay);
                
                System.err.printf("Request failed (attempt %d/%d), retrying in %dms: %s%n", 
                    attempt + 1, maxRetries + 1, delayMs, e.getMessage());
                
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted", ie);
                }
                
                // Exponential backoff with max limit
                currentDelay = Duration.ofMillis(
                    Math.min(
                        (long) (currentDelay.toMillis() * DEFAULT_BACKOFF_MULTIPLIER),
                        maxDelay.toMillis()
                    )
                );
            }
        }
        
        throw lastException;
    }
    
    /**
     * Determine if an exception should trigger a retry
     */
    private static boolean shouldRetry(Exception e) {
        // Retry on connection timeouts and HTTP timeouts
        return e instanceof HttpConnectTimeoutException ||
               e instanceof HttpTimeoutException ||
               (e.getCause() instanceof HttpConnectTimeoutException) ||
               (e.getCause() instanceof HttpTimeoutException) ||
               // Also retry on general connection issues that might be rate limiting
               (e.getMessage() != null && (
                   e.getMessage().contains("Connection refused") ||
                   e.getMessage().contains("Connection reset") ||
                   e.getMessage().contains("timeout") ||
                   e.getMessage().contains("Too many requests")
               ));
    }
    
    /**
     * Calculate delay with jitter to prevent thundering herd
     */
    private static long calculateDelayWithJitter(Duration baseDelay) {
        long baseMs = baseDelay.toMillis();
        double jitter = (RANDOM.nextDouble() - 0.5) * 2 * DEFAULT_JITTER_FACTOR;
        return Math.max(100, (long) (baseMs * (1 + jitter))); // Minimum 100ms delay
    }
    
    /**
     * Create a predicate for checking if we should retry based on exception type
     */
    public static Predicate<Exception> timeoutRetryPredicate() {
        return RetryUtil::shouldRetry;
    }
}
