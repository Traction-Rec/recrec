package com.tractionrec.recrec.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.tractionrec.recrec.domain.QueryItem;
import com.tractionrec.recrec.domain.ResultStatus;
import com.tractionrec.recrec.domain.express.Transaction;
import com.tractionrec.recrec.domain.express.TransactionQueryResponse;
import com.tractionrec.recrec.domain.result.TransactionQueryResult;
import gg.jte.TemplateEngine;
import gg.jte.TemplateOutput;
import gg.jte.output.StringOutput;
import org.apache.commons.text.StringEscapeUtils;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class TransactionQueryService extends QueryService {

    private TransactionQueryService(Boolean isProduction, TemplateEngine templateEngine) {
        super(isProduction, templateEngine);
    }

    public TransactionQueryResult queryForTransaction(String accountId, String accountToken, QueryItem item) {
        String requestBody = getTxBody(accountId, accountToken, item);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(getReportingUri())
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .header("SOAPAction", "https://reporting.elementexpress.com/TransactionQuery")
                .header("Content-Type", "text/xml")
                .timeout(Duration.ofSeconds(60)) // Request timeout
                .build();
        try {
            HttpResponse<String> response = executeRequestWithRetry(request);
            if(response.statusCode() != 200) {
                return new TransactionQueryResult(item, ResultStatus.ERROR, "Status Code: " + response.statusCode());
            }
            final String responseBody = response.body();
            final String responseElement = responseBody.substring(responseBody.indexOf("<response>"), responseBody.indexOf("</TransactionQueryResponse>")).trim();

            final TransactionQueryResponse queryResponse = mapper.readValue(responseElement, TransactionQueryResponse.class);
            if(queryResponse.responseCode == 90) {
                return new TransactionQueryResult(item, ResultStatus.NOT_FOUND, queryResponse.responseMessage);
            }
            if(queryResponse.responseCode != 0) {
                return new TransactionQueryResult(item, ResultStatus.ERROR, queryResponse.responseMessage);
            }
            final String unescapedReportingData = StringEscapeUtils.unescapeXml(queryResponse.reportingData);
            final List<Transaction> results = mapper.readValue(unescapedReportingData, new TypeReference<>() {
            });
            return new TransactionQueryResult(item, ResultStatus.SUCCESS, queryResponse.responseMessage, results);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new TransactionQueryResult(item, ResultStatus.ERROR, ex.getMessage());
        }
    }

    public static TransactionQueryService forProduction(TemplateEngine templateEngine) {
        return new TransactionQueryService(true, templateEngine);
    }

    public static TransactionQueryService forTest(TemplateEngine templateEngine) {
        return new TransactionQueryService(false, templateEngine);
    }

    private String getTxBody(String accountId, String accountToken, QueryItem item) {
        TemplateOutput output = new StringOutput();
        templateEngine.render("transactionQueryPOSTBody.jte", Map.of(
                "accountId", accountId,
                "accountToken", accountToken,
                "queryItem", item
        ), output);
        return output.toString();
    }

    private URI getReportingUri() {
        return isProduction ? URI.create("https://reporting.elementexpress.com/express.asmx") : URI.create("https://certreporting.elementexpress.com/express.asmx");
    }

}
