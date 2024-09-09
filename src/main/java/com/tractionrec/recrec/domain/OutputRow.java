package com.tractionrec.recrec.domain;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.tractionrec.recrec.domain.express.ExpressEntity;
import com.tractionrec.recrec.domain.express.PaymentAccount;
import com.tractionrec.recrec.domain.express.Transaction;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@JsonPropertyOrder({
        "merchant", "id", "status", "message", "recordId", "vantivId", "hostTransactionId", "networkTransactionId",
        "retrievalReferenceNumber", "systemTraceAuditNumber", "trackingId", "ticketNumber", "terminalId", "setupId",
        "transactionStatus", "amount", "approvalNumber", "billingName", "billingEmail", "paymentAccountId",
        "cardNumber", "cardType", "cardLogo", "expirationMonth", "expirationYear", "transactionDate", "transactionType",
        "multipleResults"
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
    public final String billingEmail;
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
        this.billingEmail = null;
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

    private OutputRow(QueryResult result, ExpressEntity tx, boolean multipleResults) {
        this.merchant = result.item().merchant();
        this.status = result.status().name();
        this.message = result.expressResponseMessage();
        if(tx instanceof Transaction) {
            Transaction castedTx = (Transaction) tx;
            this.id = result.item().id();
            this.recordId = castedTx.recordId;
            this.vantivId = castedTx.vantivId;
            this.hostTransactionId = castedTx.hostTransactionId;
            this.networkTransactionId = castedTx.networkTransactionId;
            this.retrievalReferenceNumber = castedTx.retrievalReferenceNumber;
            this.systemTraceAuditNumber = castedTx.systemTraceAuditNumber;
            this.trackingId = castedTx.trackingId;
            this.ticketNumber = castedTx.ticketNumber;
            this.terminalId = castedTx.terminalId;
            this.setupId = castedTx.setupId;
            this.transactionStatus = castedTx.status;
            this.amount = castedTx.amount.toPlainString();
            this.approvalNumber = castedTx.approvalNumber;
            this.billingName = castedTx.billingName;
            this.billingEmail = castedTx.billingEmail;
            this.paymentAccountId = castedTx.paymentAccountId;
            this.cardNumber = castedTx.cardNumberMasked;
            this.cardType = castedTx.cardType;
            this.cardLogo = castedTx.cardLogo;
            this.expirationMonth = castedTx.expirationMonth;
            this.expirationYear = castedTx.expirationYear;
            this.transactionDate = LocalDateTime.of(castedTx.transactionDate, castedTx.transactionTime).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            this.transactionType = castedTx.transactionType;
            this.multipleResults = multipleResults;
        } else {
            this.id = ((PaymentAccount) tx).paymentAccountID;
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
            this.billingEmail = null;
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

    }

    public static List<OutputRow> from(QueryResult result) {
        final Optional<List<ExpressEntity>> optTxes = result.expressEntities();
        if(optTxes.isEmpty()) {
            return List.of(new OutputRow(result));
        }
        final List<ExpressEntity> txes = optTxes.get();
        final boolean multipleResults = txes.size() > 1;
        return txes.stream().map(tx -> new OutputRow(result, tx, multipleResults)).toList();
    }
}
