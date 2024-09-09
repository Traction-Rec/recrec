package com.tractionrec.recrec.ui;

import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.tractionrec.recrec.RecRecState;
import com.tractionrec.recrec.domain.OutputRow;
import com.tractionrec.recrec.domain.QueryBy;
import com.tractionrec.recrec.domain.QueryItem;
import com.tractionrec.recrec.domain.QueryResult;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
                try(FileWriter fileWriter = new FileWriter(outputFile)) {
                    CsvMapper mapper = new CsvMapper();
                    CsvSchema schema = mapper.schemaFor(OutputRow.class)
                            .withHeader();
                    SequenceWriter sequenceWriter = mapper.writer(schema)
                            .writeValues(fileWriter);
                    for(Future<QueryResult> f : futureResults) {
                        List<OutputRow> rows = OutputRow.from(f.get());
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
                            switch (result.status()) {
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
        return (Callable<QueryResult>) () -> {
            if(state.queryMode != QueryBy.PAYMENT_ACCOUNT) {
                return state.queryService.queryForTransaction(
                        state.accountId,
                        state.accountToken,
                        item
                );
            } else {
                return state.queryService.queryForPaymentAccount(
                        state.accountId,
                        state.accountToken,
                        item
                );
            }
        };
    }

    protected void setupUI() {
        rootPanel = new JPanel();
        rootPanel.setBorder( BorderFactory.createEmptyBorder(20,20,20,20) );
        rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.Y_AXIS));
        rootPanel.setAlignmentX( Component.LEFT_ALIGNMENT );
        rootPanel.setAlignmentY( Component.TOP_ALIGNMENT );
        txtProgress = new JLabel();
        txtProgress.setAlignmentX( Component.LEFT_ALIGNMENT );
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

}
