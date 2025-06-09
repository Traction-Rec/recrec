package com.tractionrec.recrec.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import gg.jte.TemplateEngine;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

public abstract class QueryService {

    protected final boolean isProduction;
    protected final TemplateEngine templateEngine;
    protected final ObjectMapper mapper;
    protected final HttpClient httpClient;

    protected QueryService(boolean isProduction, TemplateEngine templateEngine) {
        this.isProduction = isProduction;
        this.templateEngine = templateEngine;
        this.mapper = new XmlMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyyMMdd")));
        module.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern("HHmmss")));
        mapper.registerModule(module);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        // Configure shared HttpClient with connection pooling and timeouts
        // Use HTTP/1.1 to avoid HTTP/2 GOAWAY issues with high concurrency
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1) // Use HTTP/1.1 for better connection stability
                .connectTimeout(Duration.ofSeconds(30)) // Connection timeout
                .followRedirects(HttpClient.Redirect.NORMAL) // Handle redirects
                .build();

        // Apply Windows-specific connection optimizations
        configureForWindows();

        // Pre-resolve DNS for known endpoints to avoid resolution delays
        preResolveDNS();
    }

    /**
     * Pre-resolve DNS for known endpoints to populate DNS cache
     */
    private void preResolveDNS() {
        // Asynchronously resolve DNS for all known endpoints
        CompletableFuture.runAsync(() -> {
            try {
                // Production endpoints
                InetAddress.getByName("reporting.elementexpress.com");
                InetAddress.getByName("transaction.elementexpress.com");
                InetAddress.getByName("services.elementexpress.com");

                // Test/Cert endpoints
                InetAddress.getByName("certreporting.elementexpress.com");
                InetAddress.getByName("certtransaction.elementexpress.com");
                InetAddress.getByName("certservices.elementexpress.com");
            } catch (UnknownHostException e) {
                // Log but don't fail - DNS resolution will happen on first request
                System.err.println("Warning: Could not pre-resolve DNS for some endpoints: " + e.getMessage());
            }
        });
    }

    /**
     * Execute HTTP request with retry logic for handling rate limiting and timeouts.
     * Uses enhanced retry logic that can handle Windows connection exhaustion and HTTP status codes.
     */
    protected HttpResponse<String> executeRequestWithRetry(HttpRequest request) throws Exception {
        Callable<HttpResponse<String>> requestOperation = () -> {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        };

        try {
            return RetryUtil.retryHttpRequestWithBackoff(requestOperation);
        } catch (Exception e) {
            // If we get connection exhaustion errors, try with longer delays
            if (isConnectionExhaustion(e)) {
                System.err.println("Detected connection exhaustion, retrying with longer delays: " + e.getMessage());
                return RetryUtil.retryHttpRequestWithBackoff(requestOperation, 5, Duration.ofSeconds(5), Duration.ofSeconds(60));
            }
            throw e;
        }
    }

    /**
     * Check if an exception indicates connection exhaustion (port exhaustion)
     */
    private boolean isConnectionExhaustion(Exception e) {
        if (e.getCause() != null) {
            String message = e.getCause().getMessage();
            if (message != null) {
                message = message.toLowerCase();
                return message.contains("address already in use") ||
                       message.contains("getsockopt") ||
                       message.contains("bind");
            }
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
     * Configure Windows-specific connection optimizations
     */
    private void configureForWindows() {
        String osName = System.getProperty("os.name", "").toLowerCase();
        if (osName.contains("windows")) {
            // Configure connection pool settings for Windows
            System.setProperty("jdk.httpclient.keepalive.timeout", "30");

            // Optimize for Windows connection handling
            System.setProperty("sun.net.useExclusiveBind", "false");

            System.out.println("Applied Windows-specific HTTP connection optimizations");
        }
    }

}
