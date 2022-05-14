package com.tractionrec.recrec.domain;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.tractionrec.recrec.domain.express.Transaction;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@JsonPropertyOrder({
        "merchant", "id", "status", "message", "recordId", "vantivId", "setupId", "transactionStatus", "amount",
        "approvalNumber", "billingName", "paymentAccountId", "cardNumber", "cardType", "cardLogo", "expirationMonth",
        "expirationYear", "transactionDate", "multipleResults"
})
public class OutputRow {
    public final String merchant;
    public final String id;
    public final String status;
    public final String message;
    public final String recordId;
    public final String vantivId;
    public final String setupId;
    public final String transactionStatus;
    public final String amount;
    public final String approvalNumber;
    public final String billingName;
    public final String paymentAccountId;
    public final String cardNumber;
    public final String cardType;
    public final String cardLogo;
    public final String expirationMonth;
    public final String expirationYear;
    public final String transactionDate;
    public final boolean multipleResults;

    private OutputRow(QueryResult result) {
        this.merchant = result.item().merchant();
        this.id = result.item().id();
        this.status = result.status().name();
        this.message = result.expressResponseMessage();
        this.recordId = null;
        this.vantivId = null;
        this.setupId = null;
        this.transactionStatus = null;
        this.amount = null;
        this.approvalNumber = null;
        this.billingName = null;
        this.paymentAccountId = null;
        this.cardNumber = null;
        this.cardType = null;
        this.cardLogo = null;
        this.expirationMonth = null;
        this.expirationYear = null;
        this.transactionDate = null;
        this.multipleResults = false;
    }

    private OutputRow(QueryResult result, Transaction tx, boolean multipleResults) {
        this.merchant = result.item().merchant();
        this.id = result.item().id();
        this.status = result.status().name();
        this.message = result.expressResponseMessage();
        this.recordId = tx.recordId;
        this.vantivId = tx.vantivId;
        this.setupId = tx.setupId;
        this.transactionStatus = tx.status;
        this.amount = tx.amount.toPlainString();
        this.approvalNumber = tx.approvalNumber;
        this.billingName = tx.billingName;
        this.paymentAccountId = tx.paymentAccountId;
        this.cardNumber = tx.cardNumberMasked;
        this.cardType = tx.cardType;
        this.cardLogo = tx.cardLogo;
        this.expirationMonth = tx.expirationMonth;
        this.expirationYear = tx.expirationYear;
        this.transactionDate = LocalDateTime.of(tx.transactionDate, tx.transactionTime).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        this.multipleResults = multipleResults;
    }

    public static List<OutputRow> from(QueryResult result) {
        final Optional<List<Transaction>> optTxes = result.expressTransactions();
        if(optTxes.isEmpty()) {
            return List.of(new OutputRow(result));
        }
        final List<Transaction> txes = optTxes.get();
        final boolean multipleResults = txes.size() > 1;
        return txes.stream().map(tx -> new OutputRow(result, tx, multipleResults)).toList();
    }
}
