package com.tractionrec.recrec.ui;

import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.tractionrec.recrec.RecRecState;
import com.tractionrec.recrec.domain.QueryItem;
import com.tractionrec.recrec.domain.QueryTargetVisitor;
import com.tractionrec.recrec.domain.ResultStatus;
import com.tractionrec.recrec.domain.output.BINQueryOutputRow;
import com.tractionrec.recrec.domain.output.PaymentAccountQueryOutputRow;
import com.tractionrec.recrec.domain.output.TransactionQueryOutputRow;
import com.tractionrec.recrec.domain.result.BINQueryResult;
import com.tractionrec.recrec.domain.result.PaymentAccountQueryResult;
import com.tractionrec.recrec.domain.result.QueryResult;
import com.tractionrec.recrec.domain.result.TransactionQueryResult;
import com.tractionrec.recrec.service.AdaptiveRateLimiter;
import com.tractionrec.recrec.service.BINQueryService;
import com.tractionrec.recrec.service.PaymentAccountQueryService;
import com.tractionrec.recrec.service.TransactionQueryService;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.DirectoryCodeResolver;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.tractionrec.recrec.RecRecApplication.isDevEnv;
import static com.tractionrec.recrec.RecRecApplication.isProduction;

public class RecRecRunning extends RecRecForm {
    private final ExecutorService queryExecutorService;
    private final ScheduledExecutorService timeExecutorService = Executors.newScheduledThreadPool(1);
    private JPanel rootPanel;
    private JLabel txtProgress;
    private JButton nextButton;
    private List<Future<QueryResult>> futureResults;

    // Cached service instances to reuse HttpClient connections and avoid port exhaustion
    private static volatile TransactionQueryService transactionService;
    private static volatile PaymentAccountQueryService paymentAccountService;
    private static volatile BINQueryService binService;
    private static final Object serviceLock = new Object();

    private static final int INITIAL_CONCURRENT_REQUESTS = 20;
    private static final int MIN_CONCURRENT_REQUESTS = 5;
    private static final int MAX_CONCURRENT_REQUESTS = 50;
    private final AdaptiveRateLimiter rateLimiter = new AdaptiveRateLimiter(
            INITIAL_CONCURRENT_REQUESTS, MIN_CONCURRENT_REQUESTS, MAX_CONCURRENT_REQUESTS);

    public RecRecRunning(RecRecState state, NavigationAction navAction) {
        super(state, navAction);
        this.queryExecutorService = Executors.newVirtualThreadPerTaskExecutor();
        nextButton.addActionListener(e -> {
            JFileChooser outputChooser = new JFileChooser();
            int result = outputChooser.showDialog(rootPanel, "Save results");
            if (result == JFileChooser.APPROVE_OPTION) {
                File outputFile = outputChooser.getSelectedFile();
                try (FileWriter fileWriter = new FileWriter(outputFile)) {
                    CsvMapper mapper = new CsvMapper();
                    CsvSchema schema = getResultSchema(mapper);
                    SequenceWriter sequenceWriter = mapper.writer(schema)
                            .writeValues(fileWriter);
                    for (Future<QueryResult> f : futureResults) {
                        if(f.get() != null) {
                            List<?> rows = f.get().getOutputRows();
                            sequenceWriter.writeAll(rows);
                        }
                    }
                } catch (ExecutionException | InterruptedException | IOException ex) {
                    ex.printStackTrace();
                }
                state.reset();
                navAction.onNext();
            }
        });
        timeExecutorService.scheduleAtFixedRate(() -> {
            AtomicInteger totalCount = new AtomicInteger();
            AtomicInteger pendingCount = new AtomicInteger();
            AtomicInteger errorCount = new AtomicInteger();
            AtomicInteger notFoundCount = new AtomicInteger();
            AtomicInteger successCount = new AtomicInteger();
            if (futureResults != null) {
                futureResults.stream().forEach(f -> {
                    totalCount.getAndIncrement();
                    if (!f.isDone()) {
                        pendingCount.getAndIncrement();
                    } else {
                        try {
                            final QueryResult result = f.get();
                            switch (result.getStatus()) {
                                case ERROR -> errorCount.getAndIncrement();
                                case NOT_FOUND -> notFoundCount.getAndIncrement();
                                case SUCCESS -> successCount.getAndIncrement();
                            }
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                            errorCount.getAndIncrement();
                        }
                    }
                });
            }

            // Enable next button only when pending requests are zero
            SwingUtilities.invokeLater(() -> {
                nextButton.setEnabled(pendingCount.get() == 0);
            });

            // Only show rate limiter info in dev mode
            if (isDevEnv()) {
                String rateLimiterStatus = rateLimiter.getStats();
                String connectionInfo = getConnectionInfo();
                txtProgress.setText(String.format("<html><p>Total: %d</p><p>Pending: %d</p><p>Error: %d</p><p>Not Found: %d</p><p>Success: %d</p><p><b>%s</b></p><p><i>%s</i></p></html>",
                    totalCount.get(), pendingCount.get(), errorCount.get(), notFoundCount.get(), successCount.get(), rateLimiterStatus, connectionInfo));
            } else {
                txtProgress.setText(String.format("<html><p>Total: %d</p><p>Pending: %d</p><p>Error: %d</p><p>Not Found: %d</p><p>Success: %d</p></html>",
                    totalCount.get(), pendingCount.get(), errorCount.get(), notFoundCount.get(), successCount.get()));
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public JComponent getRootComponent() {
        return rootPanel;
    }

    public void willDisplay() {
        try {
            this.futureResults = Files.lines(state.inputFile.toPath()).skip(1)
                    .filter(line -> !line.trim().isEmpty()) // Filter out empty or whitespace-only lines
                    .map(l -> {
                        String[] cols = l.split(",", 2);
                        if (cols.length < 2) {
                            System.err.println("Warning: Skipping malformed CSV line (expected 2 columns): " + l);
                            return null; // Return null for malformed lines
                        }
                        return new QueryItem(cols[0].trim(), cols[1].trim(), state.queryMode);
                    })
                    .filter(item -> item != null) // Filter out null items from malformed lines
                    .map(this::getCallable)
                    .map(queryExecutorService::submit)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Callable<QueryResult> getCallable(QueryItem item) {
        return () -> state.queryMode.accept(new QueryTargetVisitor<QueryResult>() {
            @Override
            public TransactionQueryResult visitTransactionQuery() {
                try {
                    rateLimiter.acquire();
                    TransactionQueryResult r = getTransactionQueryService().queryForTransaction(
                            state.accountId,
                            state.accountToken,
                            item
                    );

                    // Record success/failure for adaptive rate limiting
                    // Only record failure for ERROR status, NOT_FOUND is acceptable
                    if (r != null && (r.getStatus() == ResultStatus.SUCCESS || r.getStatus() == ResultStatus.NOT_FOUND)) {
                        rateLimiter.recordSuccess();
                    } else {
                        rateLimiter.recordFailure();
                    }

                    return r;
                } catch (Exception e) {
                    rateLimiter.recordFailure();
                    e.printStackTrace();
                    return new TransactionQueryResult(item, ResultStatus.ERROR, "Request failed: " + e.getMessage());
                } finally {
                    rateLimiter.release();
                }
            }

            @Override
            public PaymentAccountQueryResult visitPaymentAccountQuery() {
                try {
                    rateLimiter.acquire();
                    PaymentAccountQueryResult r = getPaymentAccountQueryService().queryForPaymentAccount(
                            state.accountId,
                            state.accountToken,
                            item
                    );

                    // Record success/failure for adaptive rate limiting
                    // Only record failure for ERROR status, NOT_FOUND is acceptable
                    if (r != null && (r.getStatus() == ResultStatus.SUCCESS || r.getStatus() == ResultStatus.NOT_FOUND)) {
                        rateLimiter.recordSuccess();
                    } else {
                        rateLimiter.recordFailure();
                    }

                    return r;
                } catch (Exception e) {
                    rateLimiter.recordFailure();
                    e.printStackTrace();
                    return new PaymentAccountQueryResult(item, ResultStatus.ERROR, "Request failed: " + e.getMessage());
                } finally {
                    rateLimiter.release();
                }
            }

            @Override
            public BINQueryResult visitBINQuery() {
                try {
                    rateLimiter.acquire();
                    BINQueryResult r = getBINQueryService().queryForBINInfo(
                            state.accountId,
                            state.accountToken,
                            item
                    );

                    // Record success/failure for adaptive rate limiting
                    // Only record failure for ERROR status, NOT_FOUND is acceptable
                    if (r != null && (r.getStatus() == ResultStatus.SUCCESS || r.getStatus() == ResultStatus.NOT_FOUND)) {
                        rateLimiter.recordSuccess();
                    } else {
                        rateLimiter.recordFailure();
                    }

                    return r;
                } catch (Exception e) {
                    rateLimiter.recordFailure();
                    e.printStackTrace();
                    return new BINQueryResult(item, ResultStatus.ERROR, "Request failed: " + e.getMessage());
                } finally {
                    rateLimiter.release();
                }
            }
        });
    }

    private CsvSchema getResultSchema(CsvMapper mapper) {
        return state.queryMode.accept(new QueryTargetVisitor<CsvSchema>() {
            @Override
            public CsvSchema visitTransactionQuery() {
                return mapper.schemaFor(TransactionQueryOutputRow.class)
                        .withHeader();
            }

            @Override
            public CsvSchema visitPaymentAccountQuery() {
                return mapper.schemaFor(PaymentAccountQueryOutputRow.class)
                        .withHeader();
            }

            @Override
            public CsvSchema visitBINQuery() {
                return mapper.schemaFor(BINQueryOutputRow.class)
                        .withHeader();
            }
        });
    }

    protected void setupUI() {
        rootPanel = new JPanel();
        rootPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.Y_AXIS));
        rootPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rootPanel.setAlignmentY(Component.TOP_ALIGNMENT);
        txtProgress = new JLabel();
        txtProgress.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtProgress.setText("Running....");
        rootPanel.add(txtProgress);

        JPanel navigationPanel = new JPanel();
        navigationPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        navigationPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        nextButton = new JButton();
        nextButton.setText("Next >");
        nextButton.setEnabled(false); // Initially disabled until all requests complete
        navigationPanel.add(nextButton);
        rootPanel.add(navigationPanel);
    }

    /**
     * Get cached TransactionQueryService instance to reuse HttpClient connections.
     * Thread-safe singleton pattern to avoid creating multiple HttpClient instances.
     */
    private static TransactionQueryService getTransactionQueryService() {
        if (transactionService == null) {
            synchronized (serviceLock) {
                if (transactionService == null) {
                    TemplateEngine templateEngine = isDevEnv() ?
                        TemplateEngine.create(new DirectoryCodeResolver(Path.of("src", "main", "jte")), ContentType.Plain) :
                        TemplateEngine.createPrecompiled(ContentType.Plain);
                    transactionService = isProduction() ?
                        TransactionQueryService.forProduction(templateEngine) :
                        TransactionQueryService.forTest(templateEngine);
                    System.out.println("Created shared TransactionQueryService instance");
                }
            }
        }
        return transactionService;
    }

    /**
     * Get cached PaymentAccountQueryService instance to reuse HttpClient connections.
     * Thread-safe singleton pattern to avoid creating multiple HttpClient instances.
     */
    private static PaymentAccountQueryService getPaymentAccountQueryService() {
        if (paymentAccountService == null) {
            synchronized (serviceLock) {
                if (paymentAccountService == null) {
                    TemplateEngine templateEngine = isDevEnv() ?
                        TemplateEngine.create(new DirectoryCodeResolver(Path.of("src", "main", "jte")), ContentType.Plain) :
                        TemplateEngine.createPrecompiled(ContentType.Plain);
                    paymentAccountService = isProduction() ?
                        PaymentAccountQueryService.forProduction(templateEngine) :
                        PaymentAccountQueryService.forTest(templateEngine);
                    System.out.println("Created shared PaymentAccountQueryService instance");
                }
            }
        }
        return paymentAccountService;
    }

    /**
     * Get cached BINQueryService instance to reuse HttpClient connections.
     * Thread-safe singleton pattern to avoid creating multiple HttpClient instances.
     */
    private static BINQueryService getBINQueryService() {
        if (binService == null) {
            synchronized (serviceLock) {
                if (binService == null) {
                    TemplateEngine templateEngine = isDevEnv() ?
                        TemplateEngine.create(new DirectoryCodeResolver(Path.of("src", "main", "jte")), ContentType.Plain) :
                        TemplateEngine.createPrecompiled(ContentType.Plain);
                    binService = isProduction() ?
                        BINQueryService.forProduction(templateEngine) :
                        BINQueryService.forTest(templateEngine);
                    System.out.println("Created shared BINQueryService instance");
                }
            }
        }
        return binService;
    }

    /**
     * Get connection information for monitoring (dev mode only)
     */
    private String getConnectionInfo() {
        String osName = System.getProperty("os.name", "Unknown");
        String poolSize = System.getProperty("jdk.httpclient.connectionPoolSize", "default");

        // Count active service instances
        int activeServices = 0;
        if (transactionService != null) activeServices++;
        if (paymentAccountService != null) activeServices++;
        if (binService != null) activeServices++;

        return String.format("OS: %s | Pool: %s | Services: %d",
            osName.contains("Windows") ? "Windows" : osName, poolSize, activeServices);
    }

    /**
     * Clean up cached service instances (for testing or shutdown)
     */
    public static void resetServiceInstances() {
        synchronized (serviceLock) {
            transactionService = null;
            paymentAccountService = null;
            binService = null;
            System.out.println("Reset all cached service instances");
        }
    }

}
