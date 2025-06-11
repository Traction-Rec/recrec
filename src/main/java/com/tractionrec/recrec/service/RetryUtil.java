package com.tractionrec.recrec.service;

import java.net.BindException;
import java.net.ConnectException;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpResponse;
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
     * Retry with longer delays for connection exhaustion scenarios (Windows port exhaustion)
     */
    public static <T> T retryWithBackoffForConnectionExhaustion(Callable<T> operation) throws Exception {
        // Use longer delays for connection exhaustion scenarios
        return retryWithBackoff(operation, 5, Duration.ofSeconds(5), Duration.ofSeconds(60));
    }

    /**
     * Retry HTTP requests with backoff, handling both exceptions and HTTP status codes
     */
    public static HttpResponse<String> retryHttpRequestWithBackoff(Callable<HttpResponse<String>> operation) throws Exception {
        return retryHttpRequestWithBackoff(operation, DEFAULT_MAX_RETRIES, DEFAULT_INITIAL_DELAY, DEFAULT_MAX_DELAY);
    }

    /**
     * Retry HTTP requests with custom parameters, handling both exceptions and HTTP status codes
     */
    public static HttpResponse<String> retryHttpRequestWithBackoff(
            Callable<HttpResponse<String>> operation,
            int maxRetries,
            Duration initialDelay,
            Duration maxDelay) throws Exception {

        Exception lastException = null;
        HttpResponse<String> lastResponse = null;
        Duration currentDelay = initialDelay;

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                HttpResponse<String> response = operation.call();
                lastResponse = response;

                // Check if we should retry based on status code
                if (!shouldRetryHttpResponse(response) || attempt == maxRetries) {
                    return response;
                }

                // Calculate delay with jitter for rate limiting
                long delayMs = calculateDelayWithJitter(currentDelay);

                System.err.printf("HTTP request failed with status %d (attempt %d/%d), retrying in %dms%n",
                    response.statusCode(), attempt + 1, maxRetries + 1, delayMs);

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

        // If we get here, we exhausted retries due to HTTP status codes
        if (lastResponse != null) {
            return lastResponse; // Return the last response even if it has a bad status code
        }
        throw lastException; // This shouldn't happen, but just in case
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
     * Determine if an HTTP response should trigger a retry based on status code
     */
    private static boolean shouldRetryHttpResponse(HttpResponse<String> response) {
        int statusCode = response.statusCode();

        // Retry on rate limiting (429) and server errors (5xx)
        return statusCode == 429 || // Too Many Requests
               statusCode == 502 || // Bad Gateway
               statusCode == 503 || // Service Unavailable
               statusCode == 504;   // Gateway Timeout
    }

    /**
     * Determine if an exception should trigger a retry
     */
    private static boolean shouldRetry(Exception e) {
        // Retry on connection timeouts and HTTP timeouts
        if (e instanceof HttpConnectTimeoutException ||
            e instanceof HttpTimeoutException ||
            e instanceof BindException ||
            e instanceof ConnectException) {
            return true;
        }

        // Check cause exceptions
        if (e.getCause() instanceof HttpConnectTimeoutException ||
            e.getCause() instanceof HttpTimeoutException ||
            e.getCause() instanceof BindException ||
            e.getCause() instanceof ConnectException) {
            return true;
        }

        // Check error messages for connection issues (especially Windows-specific)
        if (e.getMessage() != null) {
            String message = e.getMessage().toLowerCase();
            return message.contains("connection refused") ||
                   message.contains("connection reset") ||
                   message.contains("timeout") ||
                   message.contains("too many requests") ||
                   message.contains("address already in use") ||
                   message.contains("getsockopt") ||
                   message.contains("port") ||
                   message.contains("bind");
        }

        return false;
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
     * Check if an exception indicates connection exhaustion (port exhaustion)
     */
    private static boolean isConnectionExhaustion(Exception e) {
        if (e instanceof BindException || e instanceof ConnectException) {
            return true;
        }

        if (e.getCause() instanceof BindException || e.getCause() instanceof ConnectException) {
            return true;
        }

        if (e.getMessage() != null) {
            String message = e.getMessage().toLowerCase();
            return message.contains("address already in use") ||
                   message.contains("getsockopt") ||
                   message.contains("bind");
        }

        return false;
    }

    /**
     * Create a predicate for checking if we should retry based on exception type
     */
    public static Predicate<Exception> timeoutRetryPredicate() {
        return RetryUtil::shouldRetry;
    }
}
