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
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
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
    private SegmentedProgressBar segmentedProgressBar;
    private JLabel progressLabel;
    private StatisticsTable statisticsTable;
    private JLabel systemInfoLabel;
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

    public RecRecRunning(RecRecState state, NavigationAction navigationAction) {
        super(state, navigationAction);
        this.queryExecutorService = Executors.newVirtualThreadPerTaskExecutor();
        setupUI();
        setupEventHandlers();
    }

    private void setupEventHandlers() {
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
                navigationAction.onNext();
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

            // Calculate progress percentage
            int total = totalCount.get();
            int completed = total - pendingCount.get();
            int progressPercent = total > 0 ? (completed * 100) / total : 0;

            // Update UI components
            SwingUtilities.invokeLater(() -> {
                // Update segmented progress bar
                segmentedProgressBar.updateProgress(
                    totalCount.get(),
                    successCount.get(),
                    notFoundCount.get(),
                    errorCount.get(),
                    pendingCount.get()
                );

                // Update tooltip for progress bar
                segmentedProgressBar.setToolTipText(segmentedProgressBar.getTooltipText());

                // Update progress label with enhanced feedback
                if (pendingCount.get() == 0 && total > 0) {
                    progressLabel.setText("✓ Processing complete! " + total + " queries processed.");
                    progressLabel.setForeground(TractionRecTheme.SUCCESS_GREEN);
                    nextButton.setEnabled(true);
                } else if (total > 0) {
                    progressLabel.setText(String.format("Processing %d of %d queries...", completed, total));
                    progressLabel.setForeground(TractionRecTheme.PRIMARY_BLUE);
                } else {
                    progressLabel.setText("Initializing...");
                    progressLabel.setForeground(TractionRecTheme.TEXT_SECONDARY);
                }

                // Update statistics table
                statisticsTable.updateStatistics(
                    totalCount.get(),
                    successCount.get(),
                    notFoundCount.get(),
                    errorCount.get(),
                    pendingCount.get()
                );

                // Update system info label if in dev mode
                if (isDevEnv()) {
                    String rateLimiterStatus = rateLimiter.getStats();
                    String connectionInfo = getConnectionInfo();
                    systemInfoLabel.setText(String.format(
                        "<html><div style='font-family: monospace; font-size: 10px; color: #6B7280;'><p><strong>System Info:</strong></p><p>• %s</p><p>• %s</p></div></html>",
                        rateLimiterStatus, connectionInfo
                    ));
                    systemInfoLabel.setVisible(true);
                } else {
                    systemInfoLabel.setVisible(false);
                }
            });
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
        rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.Y_AXIS));
        rootPanel.setBorder(BorderFactory.createEmptyBorder(
            StyleUtils.SPACING_XXLARGE,
            StyleUtils.SPACING_XXLARGE,
            StyleUtils.SPACING_XXLARGE,
            StyleUtils.SPACING_XXLARGE
        ));
        rootPanel.setBackground(Color.WHITE);
        rootPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Form Title
        JLabel titleLabel = StyleUtils.createFormTitle("Processing Queries");
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rootPanel.add(titleLabel);

        JLabel subtitleLabel = new JLabel("Please wait while we process your data queries...");
        subtitleLabel.setFont(TypographyConstants.FONT_SMALL);
        subtitleLabel.setForeground(TractionRecTheme.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rootPanel.add(subtitleLabel);
        StyleUtils.addVerticalSpacing(rootPanel, StyleUtils.SPACING_XLARGE); // Less spacing to make room for table

        // Progress Section
        JPanel progressSection = createProgressSection();
        progressSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        rootPanel.add(progressSection);
        StyleUtils.addVerticalSpacing(rootPanel, StyleUtils.SPACING_XXLARGE);

        // Navigation Section
        JPanel navigationSection = createNavigationSection();
        navigationSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        rootPanel.add(navigationSection);
    }

    private JPanel createProgressSection() {
        JPanel section = StyleUtils.createElevatedCard();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));

        // Section title with icon
        JLabel sectionTitle = StyleUtils.createSectionTitle(StyleUtils.Icons.REFRESH + "  Query Progress");
        section.add(sectionTitle);
        StyleUtils.addVerticalSpacing(section, StyleUtils.SPACING_LARGE);

        // Progress label
        progressLabel = new JLabel("Initializing...");
        progressLabel.setFont(TypographyConstants.FONT_BODY_BOLD);
        progressLabel.setForeground(TractionRecTheme.PRIMARY_BLUE);
        progressLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(progressLabel);
        StyleUtils.addVerticalSpacing(section, StyleUtils.SPACING_MEDIUM);

        // Segmented progress bar in a container to prevent resizing
        segmentedProgressBar = new SegmentedProgressBar();
        // Initialize with test data to make it visible
        segmentedProgressBar.updateProgress(100, 60, 20, 10, 10);

        JPanel progressBarContainer = new JPanel();
        progressBarContainer.setLayout(new BoxLayout(progressBarContainer, BoxLayout.X_AXIS));
        progressBarContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        progressBarContainer.add(segmentedProgressBar);
        progressBarContainer.add(Box.createHorizontalGlue()); // Push to left

        section.add(progressBarContainer);
        StyleUtils.addVerticalSpacing(section, StyleUtils.SPACING_LARGE);

        // Statistics section title
        JLabel statisticsTitle = StyleUtils.createSectionTitle("Detailed Statistics");
        section.add(statisticsTitle);
        StyleUtils.addVerticalSpacing(section, StyleUtils.SPACING_MEDIUM);

        // Statistics table
        statisticsTable = new StatisticsTable();
        statisticsTable.setFillsViewportHeight(true);

        JScrollPane tableScrollPane = new JScrollPane(statisticsTable);
        tableScrollPane.setPreferredSize(new Dimension(500, 140)); // Wider, but shorter since no scrolling needed
        tableScrollPane.setMinimumSize(new Dimension(500, 140));
        tableScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        tableScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        tableScrollPane.setBorder(StyleUtils.createInputBorder());
        tableScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER); // No vertical scroll
        tableScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        section.add(tableScrollPane);
        StyleUtils.addVerticalSpacing(section, StyleUtils.SPACING_MEDIUM);

        // System info label (only visible in dev mode)
        systemInfoLabel = new JLabel();
        systemInfoLabel.setFont(TypographyConstants.FONT_CAPTION);
        systemInfoLabel.setForeground(TractionRecTheme.TEXT_SECONDARY);
        systemInfoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        systemInfoLabel.setVisible(false); // Initially hidden
        section.add(systemInfoLabel);

        return section;
    }



    private JPanel createNavigationSection() {
        JPanel navigationPanel = new JPanel(new BorderLayout());
        navigationPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Create button panel for right alignment
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(Box.createHorizontalGlue()); // Push button to the right

        nextButton = StyleUtils.createIconButton("View Results", StyleUtils.Icons.ARROW_RIGHT);
        nextButton.setEnabled(false); // Initially disabled until all requests complete
        StyleUtils.styleButtonPrimary(nextButton, true); // Use large button
        buttonPanel.add(nextButton);

        navigationPanel.add(buttonPanel, BorderLayout.CENTER);

        return navigationPanel;
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
