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
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2) // Prefer HTTP/2 for better performance
                .connectTimeout(Duration.ofSeconds(30)) // Connection timeout
                .followRedirects(HttpClient.Redirect.NORMAL) // Handle redirects
                .build();

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
     * Execute HTTP request with retry logic for handling rate limiting and timeouts
     */
    protected HttpResponse<String> executeRequestWithRetry(HttpRequest request) throws Exception {
        Callable<HttpResponse<String>> requestOperation = () -> {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        };

        return RetryUtil.retryWithBackoff(requestOperation);
    }

}
