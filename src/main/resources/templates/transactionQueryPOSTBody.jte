@import com.tractionrec.recrec.domain.QueryBy
@import com.tractionrec.recrec.domain.QueryItem

@param String accountId
@param String accountToken
@param QueryItem queryItem
<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope v="https://reporting.elementexpress.com" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
    <soap:Header/>
    <soap:Body>
        <TransactionQuery xmlns="https://reporting.elementexpress.com">
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
            <parameters>
                @if(queryItem.mode() == QueryBy.VANTIV_ID)
                    <TransactionID>${queryItem.id()}</TransactionID>
                @elseif(queryItem.mode() == QueryBy.RECORD_ID)
                    <ReferenceNumber>${queryItem.id()}</ReferenceNumber>
                @elseif(queryItem.mode() == QueryBy.SETUP_ID)
                    <TransactionSetupID>${queryItem.id()}</TransactionSetupID>
                @endif
            </parameters>
            <arrayOfExtendedParameters/>
        </TransactionQuery>
    </soap:Body>
</soap:Envelope>