@import com.tractionrec.recrec.domain.QueryBy
@import com.tractionrec.recrec.domain.QueryItem

@param String accountId
@param String accountToken
@param QueryItem queryItem
<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope v="https://services.elementexpress.com" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
    <soap:Header/>
    <soap:Body>
        <PaymentAccountQuery xmlns="https://services.elementexpress.com">
            <credentials>
                <AccountID>${accountId}</AccountID>
                <AccountToken>${accountToken}</AccountToken>
                <AcceptorID>${queryItem.merchant()}</AcceptorID>
            </credentials>
            <application>
                <ApplicationID>4138</ApplicationID>
                <ApplicationName>Traction</ApplicationName>
                <ApplicationVersion>0.0.1</ApplicationVersion>
            </application>
            <paymentAccountParameters>
                <PaymentAccountID>${queryItem.id()}</PaymentAccountID>
            </paymentAccountParameters>
            <arrayOfExtendedParameters/>
        </PaymentAccountQuery>
    </soap:Body>
</soap:Envelope>