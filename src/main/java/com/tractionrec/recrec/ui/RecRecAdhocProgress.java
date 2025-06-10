package com.tractionrec.recrec.ui;

import com.tractionrec.recrec.RecRecState;
import com.tractionrec.recrec.domain.AdhocQueryItem;
import com.tractionrec.recrec.domain.QueryItem;
import com.tractionrec.recrec.domain.ResultStatus;
import com.tractionrec.recrec.domain.result.TransactionQueryResult;
import com.tractionrec.recrec.service.TransactionQueryService;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.DirectoryCodeResolver;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/**
 * Simple progress form for ad-hoc queries.
 * Shows "Searching..." while API call is in progress, then displays results.
 */
public class RecRecAdhocProgress extends RecRecForm {

    private JPanel rootPanel;
    private JLabel statusLabel;
    private JLabel detailLabel;
    private JProgressBar progressBar;
    private JButton viewResultsButton;
    private JButton backButton;

    private TransactionQueryResult queryResult;
    private boolean queryComplete = false;

    // Static service instance for reuse
    private static TransactionQueryService transactionService;
    private static final Object serviceLock = new Object();

    public RecRecAdhocProgress(RecRecState state, NavigationAction navigationAction) {
        super(state, navigationAction);
        setupUI();
        // Don't start query here - wait for willDisplay()
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
        rootPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Progress Section
        JPanel progressSection = createProgressSection();
        progressSection.setAlignmentX(Component.CENTER_ALIGNMENT);
        rootPanel.add(Box.createVerticalGlue());
        rootPanel.add(progressSection);
        rootPanel.add(Box.createVerticalGlue());

        // Navigation Section
        JPanel navigationSection = createNavigationSection();
        navigationSection.setAlignmentX(Component.CENTER_ALIGNMENT);
        rootPanel.add(navigationSection);
    }

    private JPanel createProgressSection() {
        JPanel section = StyleUtils.createElevatedCard();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setPreferredSize(new Dimension(500, 300));
        section.setMaximumSize(new Dimension(500, 300));

        // Title
        JLabel titleLabel = StyleUtils.createFormTitle("Transaction Search");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        section.add(titleLabel);
        StyleUtils.addVerticalSpacing(section, StyleUtils.SPACING_LARGE);

        // Progress bar
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setStringPainted(false);
        progressBar.setPreferredSize(new Dimension(400, 8));
        progressBar.setMaximumSize(new Dimension(400, 8));
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        section.add(progressBar);
        StyleUtils.addVerticalSpacing(section, StyleUtils.SPACING_LARGE);

        // Status label
        statusLabel = new JLabel("Searching for transactions...");
        statusLabel.setFont(TypographyConstants.FONT_BODY_BOLD);
        statusLabel.setForeground(TractionRecTheme.TEXT_PRIMARY);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        section.add(statusLabel);
        StyleUtils.addVerticalSpacing(section, StyleUtils.SPACING_SMALL);

        // Detail label
        detailLabel = new JLabel("Please wait while we query the Element Express API");
        detailLabel.setFont(TypographyConstants.FONT_CAPTION);
        detailLabel.setForeground(TractionRecTheme.TEXT_SECONDARY);
        detailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        section.add(detailLabel);

        return section;
    }

    private JPanel createNavigationSection() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);

        backButton = StyleUtils.createIconButton("Back", StyleUtils.Icons.ARROW_LEFT);
        StyleUtils.styleButtonSecondary(backButton);

        viewResultsButton = StyleUtils.createIconButton("View Results", StyleUtils.Icons.SEARCH);
        viewResultsButton.setEnabled(false);
        StyleUtils.styleButtonPrimary(viewResultsButton, true);

        buttonPanel.add(backButton);
        buttonPanel.add(Box.createHorizontalStrut(StyleUtils.SPACING_MEDIUM));
        buttonPanel.add(viewResultsButton);

        // Event handlers
        backButton.addActionListener(e -> navigationAction.onBack());
        viewResultsButton.addActionListener(e -> {
            if (queryComplete && queryResult != null) {
                // TODO: Navigate to results preview when implemented
                navigationAction.onNext();
            }
        });

        return buttonPanel;
    }

    private void startQuery() {
        if (state.adhocQueryItem == null) {
            showError("No search criteria provided");
            return;
        }

        AdhocQueryItem adhocQuery = state.adhocQueryItem;

        if (!adhocQuery.hasValidSearchCriteria()) {
            showError("No search criteria provided");
            return;
        }

        QueryItem queryItem = adhocQuery.toQueryItem();

        // Clear any previous results before starting new search
        state.queryResults = new java.util.ArrayList<>();

        // Start the query in a background thread
        CompletableFuture.supplyAsync(() -> {
            try {
                TransactionQueryService service = getTransactionQueryService();
                return service.queryForTransaction(
                    state.accountId,
                    state.accountToken,
                    queryItem,
                    adhocQuery
                );
            } catch (Exception e) {
                e.printStackTrace();
                return new TransactionQueryResult(queryItem, ResultStatus.ERROR, "Query failed: " + e.getMessage());
            }
        }).thenAccept(result -> {
            // Update UI on EDT
            SwingUtilities.invokeLater(() -> {
                this.queryResult = result;
                this.queryComplete = true;

                // Store result in state for results preview
                state.queryResults.add(result);

                updateUIForCompletion(result);
            });
        });
    }

    private void updateUIForCompletion(TransactionQueryResult result) {
        // Stop progress bar
        progressBar.setIndeterminate(false);
        progressBar.setVisible(false);

        if (result.getStatus() == ResultStatus.SUCCESS) {
            int resultCount = result.getExpressEntities() != null ? result.getExpressEntities().size() : 0;
            statusLabel.setText("✓ Search completed successfully");
            statusLabel.setForeground(TractionRecTheme.SUCCESS_GREEN);
            detailLabel.setText(String.format("Found %d transaction%s", resultCount, resultCount == 1 ? "" : "s"));
            viewResultsButton.setEnabled(true);
        } else if (result.getStatus() == ResultStatus.NOT_FOUND) {
            statusLabel.setText("⚠ No transactions found");
            statusLabel.setForeground(TractionRecTheme.WARNING_ORANGE);
            detailLabel.setText("No transactions match your search criteria");
            viewResultsButton.setEnabled(false);
        } else {
            statusLabel.setText("✗ Search failed");
            statusLabel.setForeground(TractionRecTheme.ERROR_RED);
            detailLabel.setText(result.getExpressResponseMessage() != null ? result.getExpressResponseMessage() : "An error occurred during the search");
            viewResultsButton.setEnabled(false);
        }
    }

    private void showError(String message) {
        progressBar.setIndeterminate(false);
        progressBar.setVisible(false);
        statusLabel.setText("✗ Error");
        statusLabel.setForeground(TractionRecTheme.ERROR_RED);
        detailLabel.setText(message);
        viewResultsButton.setEnabled(false);
    }

    @Override
    public void willDisplay() {
        // Start the query when the form is actually displayed
        startQuery();
    }

    @Override
    public void willHide() {
        // Nothing to save
    }

    @Override
    public RecRecForm whatIsNext() {
        return new RecRecResultsPreview(state, navigationAction);
    }

    @Override
    public JComponent getRootComponent() {
        return rootPanel;
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
                    System.out.println("Created shared TransactionQueryService instance for ad-hoc queries");
                }
            }
        }
        return transactionService;
    }

    /**
     * Check if running in development environment
     */
    private static boolean isDevEnv() {
        // Check if we're running from IDE or gradle run (development)
        String classPath = System.getProperty("java.class.path");
        return classPath.contains("build/classes") || classPath.contains("out/production");
    }

    /**
     * Check if running in production environment
     */
    private static boolean isProduction() {
        return "production".equals(System.getProperty("environment"));
    }
}
