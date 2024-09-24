@import com.tractionrec.recrec.domain.QueryItem

@param String accountId
@param String accountToken
@param QueryItem queryItem
<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
               xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
    <soap:Body>
        <EnhancedBINQuery xmlns="https://transaction.elementexpress.com">
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
            <extendedParameters>
                <ExtendedParameters>
                    <Key>PaymentAccount</Key>
                    <Value xsi:type="PaymentAccount">
                        <PaymentAccountID>${queryItem.id()}</PaymentAccountID>
                        <PaymentAccountType>CreditCard</PaymentAccountType>
                    </Value>
                </ExtendedParameters>
                <ExtendedParameters/>
            </extendedParameters>
        </EnhancedBINQuery>
    </soap:Body>
</soap:Envelope>