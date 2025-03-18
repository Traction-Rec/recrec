package com.tractionrec.recrec.domain.output;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.tractionrec.recrec.domain.express.Transaction;
import com.tractionrec.recrec.domain.result.QueryResult;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@JsonPropertyOrder({
        "merchant", "id", "status", "message", "recordId", "paymentAccountId", "vantivId", "hostTransactionId", "networkTransactionId",
        "retrievalReferenceNumber", "systemTraceAuditNumber", "trackingId", "ticketNumber", "terminalId", "setupId",
        "transactionStatus", "amount", "approvalNumber", "billingName", "billingAddress1", "billingCity", "billingState", "billingZip",
        "billingEmail", "cardNumber", "cardType", "cardLogo",
        "expirationMonth", "expirationYear", "transactionDate", "transactionType", "terminalData", "multipleResults"
})
public class TransactionQueryOutputRow extends OutputRow {
    // Always available
    public String recordId;
    public String paymentAccountId;
    public boolean multipleResults;
    public String billingAddress1;
    public String billingCity;
    public String billingState;
    public String billingZip;
    // Optional based on entity
    public String vantivId;
    public String hostTransactionId;
    public String networkTransactionId;
    public String retrievalReferenceNumber;
    public String systemTraceAuditNumber;
    public String trackingId;
    public String ticketNumber;
    public String terminalId;
    public String setupId;
    public String transactionStatus;
    public String amount;
    public String approvalNumber;
    public String billingName;
    public String billingEmail;
    public String cardNumber;
    public String cardType;
    public String cardLogo;
    public String expirationMonth;
    public String expirationYear;
    public String transactionDate;
    public String transactionType;
    public String terminalData;

    public TransactionQueryOutputRow(QueryResult<Transaction, TransactionQueryOutputRow> result) {
        super(result);
    }

    public TransactionQueryOutputRow(QueryResult<Transaction, TransactionQueryOutputRow> result, Transaction tx, boolean multipleResults) {
        this(result);
        this.multipleResults = multipleResults;
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
        this.billingAddress1 = tx.billingAddress1;
        this.billingCity = tx.billingCity;
        this.billingState = tx.billingState;
        this.billingZip = tx.billingZipCode;
        this.billingEmail = tx.billingEmail;
        this.paymentAccountId = tx.paymentAccountId;
        this.cardNumber = tx.cardNumberMasked;
        this.cardType = tx.cardType;
        this.cardLogo = tx.cardLogo;
        this.expirationMonth = tx.expirationMonth;
        this.expirationYear = tx.expirationYear;
        this.transactionDate = LocalDateTime.of(tx.transactionDate, tx.transactionTime).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        this.transactionType = tx.transactionType;
        this.terminalData = tx.terminalData;
    }

}
