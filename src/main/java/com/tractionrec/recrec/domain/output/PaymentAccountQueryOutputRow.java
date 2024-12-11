package com.tractionrec.recrec.domain.output;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.tractionrec.recrec.domain.express.PaymentAccount;
import com.tractionrec.recrec.domain.result.QueryResult;

@JsonPropertyOrder({
        "merchant", "id", "status", "message", "recordId", "paymentAccountId", "paymentAccountType",
        "amount", "billingName", "billingEmail", "billingAddress1", "billingCity", "billingState", "billingZip",
        "cardNumber", "truncatedAccountNumber", "truncatedRoutingNumber", "cardLogo",
        "expirationMonth", "expirationYear", "updaterBatchStatus", "updaterResultStatus"
})
public class PaymentAccountQueryOutputRow extends OutputRow {
    public String recordId;
    public String paymentAccountId;
    public String billingAddress1;
    public String billingCity;
    public String billingState;
    public String billingZip;
    public String paymentAccountType;
    public String amount;
    public String billingName;
    public String billingEmail;
    public String cardNumber;
    public String truncatedAccountNumber;
    public String truncatedRoutingNumber;
    public String cardLogo;
    public String expirationMonth;
    public String expirationYear;
    public String updaterBatchStatus;
    public String updaterResultStatus;

    public PaymentAccountQueryOutputRow(QueryResult<PaymentAccount, PaymentAccountQueryOutputRow> result) {
        super(result);
    }

    public PaymentAccountQueryOutputRow(QueryResult<PaymentAccount, PaymentAccountQueryOutputRow> result, PaymentAccount entity) {
        this(result);
        this.paymentAccountId = entity.paymentAccountID;
        this.recordId = entity.paymentAccountReferenceNumber;
        this.paymentAccountType = entity.paymentAccountType;
        this.cardNumber = entity.truncatedCardNumber;
        this.truncatedAccountNumber = entity.truncatedAccountNumber;
        this.truncatedRoutingNumber = entity.truncatedRoutingNumber;
        this.billingName = entity.billingName;
        this.billingEmail = entity.billingEmail;
        this.billingAddress1 = entity.billingAddress1;
        this.billingCity = entity.billingCity;
        this.billingState = entity.billingState;
        this.billingZip = entity.billingZipcode;
        this.cardLogo = entity.paymentBrand;
        this.expirationMonth = entity.expiryMonth;
        this.expirationYear = entity.expiryYear;
        this.updaterBatchStatus = entity.updaterBatchStatus.name();
        this.updaterResultStatus = entity.updaterResultStatus.name();
    }

}
