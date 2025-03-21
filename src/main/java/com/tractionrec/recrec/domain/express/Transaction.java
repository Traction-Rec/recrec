package com.tractionrec.recrec.domain.express;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.math.BigDecimal;

@JacksonXmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class Transaction extends ExpressEntity {
    @JacksonXmlProperty(localName = "ReferenceNumber")
    public String recordId;
    @JacksonXmlProperty(localName = "TransactionID")
    public String vantivId;
    @JacksonXmlProperty(localName = "HostTransactionID")
    public String hostTransactionId;
    @JacksonXmlProperty(localName = "NetworkTransactionID")
    public String networkTransactionId;
    @JacksonXmlProperty(localName = "RetrievalReferenceNumber")
    public String retrievalReferenceNumber;
    @JacksonXmlProperty(localName = "SystemTraceAuditNumber")
    public String systemTraceAuditNumber;
    @JacksonXmlProperty(localName = "TrackingID")
    public String trackingId;
    @JacksonXmlProperty(localName = "TicketNumber")
    public String ticketNumber;
    @JacksonXmlProperty(localName = "TerminalID")
    public String terminalId;
    @JacksonXmlProperty(localName = "TransactionSetupID")
    public String setupId;
    @JacksonXmlProperty(localName = "TransactionAmount")
    public BigDecimal amount;
    @JacksonXmlProperty(localName = "TransactionStatus")
    public String status;
    @JacksonXmlProperty(localName = "BillingName")
    public String billingName;
    @JacksonXmlProperty(localName = "BillingAddress1")
    public String billingAddress1;
    @JacksonXmlProperty(localName = "BillingCity")
    public String billingCity;
    @JacksonXmlProperty(localName = "BillingState")
    public String billingState;
    @JacksonXmlProperty(localName = "BillingZipCode")
    public String billingZipCode;
    @JacksonXmlProperty(localName = "BillingEmail")
    public String billingEmail;
    @JacksonXmlProperty(localName = "PaymentAccountID")
    public String paymentAccountId;
    @JacksonXmlProperty(localName = "CardNumberMasked")
    public String cardNumberMasked;
    @JacksonXmlProperty(localName = "ExpirationMonth")
    public String expirationMonth;
    @JacksonXmlProperty(localName = "ExpirationYear")
    public String expirationYear;
    @JacksonXmlProperty(localName = "CardType")
    public String cardType;
    @JacksonXmlProperty(localName = "CardLogo")
    public String cardLogo;
    @JacksonXmlProperty(localName = "ApprovalNumber")
    public String approvalNumber;
    @JacksonXmlProperty(localName = "TransactionType")
    public String transactionType;
    @JacksonXmlProperty(localName = "TerminalData")
    public String terminalData;
}
