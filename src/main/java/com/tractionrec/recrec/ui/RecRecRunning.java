package com.tractionrec.recrec.ui;

import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.tractionrec.recrec.RecRecState;
import com.tractionrec.recrec.domain.QueryItem;
import com.tractionrec.recrec.domain.QueryTargetVisitor;
import com.tractionrec.recrec.domain.output.BINQueryOutputRow;
import com.tractionrec.recrec.domain.output.PaymentAccountQueryOutputRow;
import com.tractionrec.recrec.domain.output.TransactionQueryOutputRow;
import com.tractionrec.recrec.domain.result.BINQueryResult;
import com.tractionrec.recrec.domain.result.PaymentAccountQueryResult;
import com.tractionrec.recrec.domain.result.QueryResult;
import com.tractionrec.recrec.domain.result.TransactionQueryResult;
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

    public RecRecRunning(RecRecState state, NavigationAction navAction) {
        super(state, navAction);
        this.queryExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
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
                        List<?> rows = f.get().getOutputRows();
                        sequenceWriter.writeAll(rows);
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
            txtProgress.setText(String.format("<html><p>Total: %d</p><p>Pending: %d</p><p>Error: %d</p><p>Not Found: %d</p><p>Success: %d</p></html>", totalCount.get(), pendingCount.get(), errorCount.get(), notFoundCount.get(), successCount.get()));
            if (pendingCount.get() == 0 && totalCount.get() > 0) {
                nextButton.setEnabled(true);
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
                    .map(l -> {
                        String[] cols = l.split(",", 2);
                        return new QueryItem(cols[0], cols[1], state.queryMode);
                    })
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
                return buildTransactionQueryService().queryForTransaction(
                        state.accountId,
                        state.accountToken,
                        item
                );
            }

            @Override
            public PaymentAccountQueryResult visitPaymentAccountQuery() {
                return buildPaymentAccountQueryService().queryForPaymentAccount(
                        state.accountId,
                        state.accountToken,
                        item
                );
            }

            @Override
            public BINQueryResult visitBINQuery() {
                return buildBINQueryService().queryForBINInfo(
                        state.accountId,
                        state.accountToken,
                        item
                );
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
        nextButton.setEnabled(false);
        nextButton.setText("Next >");
        navigationPanel.add(nextButton);
        rootPanel.add(navigationPanel);
    }

    private static TransactionQueryService buildTransactionQueryService() {
        TemplateEngine templateEngine = isDevEnv() ? TemplateEngine.create(new DirectoryCodeResolver(Path.of("src", "main", "jte")), ContentType.Plain) : TemplateEngine.createPrecompiled(ContentType.Plain);
        return isProduction() ? TransactionQueryService.forProduction(templateEngine) : TransactionQueryService.forTest(templateEngine);
    }

    private static PaymentAccountQueryService buildPaymentAccountQueryService() {
        TemplateEngine templateEngine = isDevEnv() ? TemplateEngine.create(new DirectoryCodeResolver(Path.of("src", "main", "jte")), ContentType.Plain) : TemplateEngine.createPrecompiled(ContentType.Plain);
        return isProduction() ? PaymentAccountQueryService.forProduction(templateEngine) : PaymentAccountQueryService.forTest(templateEngine);
    }

    private static BINQueryService buildBINQueryService() {
        TemplateEngine templateEngine = isDevEnv() ? TemplateEngine.create(new DirectoryCodeResolver(Path.of("src", "main", "jte")), ContentType.Plain) : TemplateEngine.createPrecompiled(ContentType.Plain);
        return isProduction() ? BINQueryService.forProduction(templateEngine) : BINQueryService.forTest(templateEngine);
    }

}
