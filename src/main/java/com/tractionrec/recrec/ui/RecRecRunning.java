package com.tractionrec.recrec.ui;

import com.tractionrec.recrec.RecRecState;
import com.tractionrec.recrec.domain.QueryItem;
import com.tractionrec.recrec.domain.QueryResult;
import com.tractionrec.recrec.domain.express.Transaction;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.TimerTask;
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
        this.queryExecutorService = Executors.newFixedThreadPool(5);
        nextButton.addActionListener(e -> {
            JFileChooser outputChooser = new JFileChooser();
            int result = outputChooser.showDialog(rootPanel, "Save results");
            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    File outputFile = outputChooser.getSelectedFile();
                    FileWriter fileWriter = new FileWriter(outputFile);
                    fileWriter.write("Merchant,Id,Result,Message,Record Id,Vantiv Id,Setup Id,Status,Amount,Billing Name,Stored Account,Card Number,Card Type,DateTime\n");
                    futureResults.stream().forEachOrdered(f -> {
                        try {
                            QueryResult resultToWrite = f.get();
                            Optional<Transaction> optTx = resultToWrite.expressTransaction();
                            fileWriter.write(String.format(
                                    "%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                                    resultToWrite.item().merchant(),
                                    resultToWrite.item().id(),
                                    resultToWrite.status().name(),
                                    resultToWrite.expressResponseMessage(),
                                    optTx.isEmpty() ? "" : optTx.get().recordId,
                                    optTx.isEmpty() ? "" : optTx.get().vantivId,
                                    optTx.isEmpty() ? "" : optTx.get().setupId,
                                    optTx.isEmpty() ? "" : optTx.get().status,
                                    optTx.isEmpty() ? "" : optTx.get().amount.toPlainString(),
                                    optTx.isEmpty() ? "" : optTx.get().billingName,
                                    optTx.isEmpty() ? "" : optTx.get().paymentAccountId,
                                    optTx.isEmpty() ? "" : optTx.get().cardNumberMasked,
                                    optTx.isEmpty() ? "" : optTx.get().cardType,
                                    optTx.isEmpty() ? "" : LocalDateTime.of(optTx.get().transactionDate, optTx.get().transactionTime).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                            ));
                        } catch (InterruptedException | ExecutionException ex) {
                            ex.printStackTrace();
                            try {
                                fileWriter.write("Error during write");
                            } catch (IOException ioex) {
                                ex.printStackTrace();
                                ioex.printStackTrace();
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    });
                    fileWriter.close();
                    state.reset();
                    navAction.onNext();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
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
                    .map(item -> (Callable<QueryResult>) () -> state.queryService.queryForTransaction(
                            state.accountId,
                            state.accountToken,
                            item
                    ))
                    .map(queryExecutorService::submit)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
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
