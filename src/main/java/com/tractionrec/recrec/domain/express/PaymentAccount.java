package com.tractionrec.recrec.domain.express;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@JacksonXmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentAccount extends ExpressEntity {
    @JacksonXmlProperty(localName = "PaymentAccountID")
    public String paymentAccountID;
    @JacksonXmlProperty(localName = "PaymentAccountType")
    public String paymentAccountType;
    @JacksonXmlProperty(localName = "TruncatedAccountNumber")
    public String truncatedAccountNumber;
    @JacksonXmlProperty(localName = "TruncatedRoutingNumber")
    public String truncatedRoutingNumber;
    @JacksonXmlProperty(localName = "PaymentAccountReferenceNumber")
    public String paymentAccountReferenceNumber;
    @JacksonXmlProperty(localName = "PaymentBrand")
    public String paymentBrand;
}
