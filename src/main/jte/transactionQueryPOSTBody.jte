@import com.tractionrec.recrec.domain.QueryBy
@import com.tractionrec.recrec.domain.QueryItem
@import com.tractionrec.recrec.domain.AdhocQueryItem

@param String accountId
@param String accountToken
@param QueryItem queryItem
@param AdhocQueryItem adhocQueryItem = null
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
                @elseif(queryItem.mode() == QueryBy.ADHOC_SEARCH && adhocQueryItem != null)
                    @if(adhocQueryItem.hasDateRange())
                        <TransactionDateTimeBegin>${adhocQueryItem.getFormattedDateBegin()}</TransactionDateTimeBegin>
                        <TransactionDateTimeEnd>${adhocQueryItem.getFormattedDateEnd()}</TransactionDateTimeEnd>
                    @endif
                    @if(adhocQueryItem.hasTransactionType())
                        <TransactionType>${adhocQueryItem.transactionType()}</TransactionType>
                    @endif
                    @if(adhocQueryItem.hasTransactionAmount())
                        <TransactionAmount>${adhocQueryItem.getFormattedAmount()}</TransactionAmount>
                    @endif
                    @if(adhocQueryItem.hasApprovalNumber())
                        <ApprovalNumber>${adhocQueryItem.approvalNumber()}</ApprovalNumber>
                    @endif
                @endif
            </parameters>
            <arrayOfExtendedParameters/>
        </TransactionQuery>
    </soap:Body>
</soap:Envelope>