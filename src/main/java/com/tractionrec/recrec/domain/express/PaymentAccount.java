package com.tractionrec.recrec.domain.express;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

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
    @JacksonXmlProperty(localName = "TruncatedCardNumber")
    public String truncatedCardNumber;
    @JacksonXmlProperty(localName = "ExpirationMonth")
    public String expiryMonth;
    @JacksonXmlProperty(localName = "ExpirationYear")
    public String expiryYear;
    @JacksonXmlProperty(localName = "BillingName")
    public String billingName;
    @JacksonXmlProperty(localName = "BillingEmail")
    public String billingEmail;
    @JacksonXmlProperty(localName = "BillingAddress1")
    public String billingAddress1;
    @JacksonXmlProperty(localName = "BillingCity")
    public String billingCity;
    @JacksonXmlProperty(localName = "BillingState")
    public String billingState;
    @JacksonXmlProperty(localName = "BillingZipcode")
    public String billingZipcode;
    @JacksonXmlProperty(localName = "PASSUpdaterBatchStatus")
    public PASSUpdaterBatchStatus updaterBatchStatus;
    @JacksonXmlProperty(localName = "PASSUpdaterStatus")
    public PASSUpdaterStatus updaterResultStatus;
}
