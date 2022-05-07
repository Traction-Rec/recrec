package com.tractionrec.recrec.domain.express;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@JacksonXmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class Transaction {
    @JacksonXmlProperty(localName = "ExpressTransactionDate")
    public LocalDate transactionDate;
    @JacksonXmlProperty(localName = "ExpressTransactionTime")
    public LocalTime transactionTime;
    @JacksonXmlProperty(localName = "ReferenceNumber")
    public String recordId;
    @JacksonXmlProperty(localName = "TransactionID")
    public String vantivId;
    @JacksonXmlProperty(localName = "TransactionSetupID")
    public String setupId;
    @JacksonXmlProperty(localName = "TransactionAmount")
    public BigDecimal amount;
    @JacksonXmlProperty(localName = "TransactionStatus")
    public String status;
    @JacksonXmlProperty(localName = "BillingName")
    public String billingName;
    @JacksonXmlProperty(localName = "PaymentAccountID")
    public String paymentAccountId;
    @JacksonXmlProperty(localName = "CardNumberMasked")
    public String cardNumberMasked;
    @JacksonXmlProperty(localName = "CardType")
    public String cardType;
}
