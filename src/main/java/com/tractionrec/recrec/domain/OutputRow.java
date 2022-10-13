package com.tractionrec.recrec.domain;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.tractionrec.recrec.domain.express.Transaction;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@JsonPropertyOrder({
        "merchant", "id", "status", "message", "recordId", "vantivId", "hostTransactionId", "networkTransactionId",
        "retrievalReferenceNumber", "systemTraceAuditNumber", "trackingId", "ticketNumber", "terminalId", "setupId",
        "transactionStatus", "amount", "approvalNumber", "billingName", "paymentAccountId", "cardNumber", "cardType",
        "cardLogo", "expirationMonth", "expirationYear", "transactionDate", "transactionType", "multipleResults"
})
public class OutputRow {
    public final String merchant;
    public final String id;
    public final String status;
    public final String message;
    public final String recordId;
    public final String vantivId;
    public final String hostTransactionId;
    public final String networkTransactionId;
    public final String retrievalReferenceNumber;
    public final String systemTraceAuditNumber;
    public final String trackingId;
    public final String ticketNumber;
    public final String terminalId;
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
    public final String transactionType;
    public final boolean multipleResults;

    private OutputRow(QueryResult result) {
        this.merchant = result.item().merchant();
        this.id = result.item().id();
        this.status = result.status().name();
        this.message = result.expressResponseMessage();
        this.recordId = null;
        this.vantivId = null;
        this.hostTransactionId = null;
        this.networkTransactionId = null;
        this.retrievalReferenceNumber = null;
        this.systemTraceAuditNumber = null;
        this.trackingId = null;
        this.ticketNumber = null;
        this.terminalId = null;
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
        this.transactionType = null;
        this.multipleResults = false;
    }

    private OutputRow(QueryResult result, Transaction tx, boolean multipleResults) {
        this.merchant = result.item().merchant();
        this.id = result.item().id();
        this.status = result.status().name();
        this.message = result.expressResponseMessage();
        this.recordId = tx.recordId;
        this.vantivId = tx.vantivId;
        this.hostTransactionId = tx.hostTransactionId;
        this.networkTransactionId = tx.networkTransactionId;
        this.retrievalReferenceNumber = tx.retrievalReferenceNumber;
        this.systemTraceAuditNumber = tx.systemTraceAuditNumber;
        this.trackingId = tx.trackingId;
        this.ticketNumber = tx.ticketNumber;
        this.terminalId = tx.terminalId;
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
        this.transactionType = tx.transactionType;
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
