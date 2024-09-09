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
        "merchant", "id", "status", "message", "recordId", "paymentAccountId", "paymentAccountType", "vantivId", "hostTransactionId", "networkTransactionId",
        "retrievalReferenceNumber", "systemTraceAuditNumber", "trackingId", "ticketNumber", "terminalId", "setupId",
        "transactionStatus", "amount", "approvalNumber", "billingName", "billingAddress1", "billingCity", "billingState", "billingZip",
        "billingEmail", "cardNumber", "truncatedAccountNumber", "truncatedRoutingNumber", "cardType", "cardLogo",
        "expirationMonth", "expirationYear", "transactionDate", "transactionType", "multipleResults"
})
public class OutputRow {
    // Always available
    public final String merchant;
    public final String id;
    public final String status;
    public final String message;
    public String recordId;
    public String paymentAccountId;
    public boolean multipleResults;
    public String billingAddress1;
    public String billingCity;
    public String billingState;
    public String billingZip;
    public String paymentAccountType;
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
    public String truncatedAccountNumber;
    public String truncatedRoutingNumber;
    public String cardType;
    public String cardLogo;
    public String expirationMonth;
    public String expirationYear;
    public String transactionDate;
    public String transactionType;

    private OutputRow(QueryResult result) {
        this.merchant = result.item().merchant();
        this.id = result.item().id();
        this.status = result.status().name();
        this.message = result.expressResponseMessage();
    }

    private OutputRow(QueryResult result, ExpressEntity entity, boolean multipleResults) {
        this.merchant = result.item().merchant();
        this.status = result.status().name();
        this.message = result.expressResponseMessage();
        this.multipleResults = multipleResults;
        if (entity instanceof Transaction) {
            Transaction tx = (Transaction) entity;
            this.id = result.item().id();
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
        } else {
            final PaymentAccount pa = ((PaymentAccount) entity);
            this.id = pa.paymentAccountID;
            this.paymentAccountId = pa.paymentAccountID;
            this.recordId = pa.paymentAccountReferenceNumber;
            this.paymentAccountType = pa.paymentAccountType;
            this.cardNumber = pa.truncatedCardNumber;
            this.truncatedAccountNumber = pa.truncatedAccountNumber;
            this.truncatedRoutingNumber = pa.truncatedRoutingNumber;
            this.billingName = pa.billingName;
            this.billingAddress1 = pa.billingAddress1;
            this.billingCity = pa.billingCity;
            this.billingState = pa.billingState;
            this.billingZip = pa.billingZipcode;
            this.cardLogo = pa.paymentBrand;
            this.expirationMonth = pa.expiryMonth;
            this.expirationYear = pa.expiryYear;
        }

    }

    public static List<OutputRow> from(QueryResult result) {
        final Optional<List<ExpressEntity>> optTxes = result.expressEntities();
        if (optTxes.isEmpty()) {
            return List.of(new OutputRow(result));
        }
        final List<ExpressEntity> txes = optTxes.get();
        final boolean multipleResults = txes.size() > 1;
        return txes.stream().map(tx -> new OutputRow(result, tx, multipleResults)).toList();
    }
}
