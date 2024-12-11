package com.tractionrec.recrec.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.tractionrec.recrec.domain.QueryItem;
import com.tractionrec.recrec.domain.ResultStatus;
import com.tractionrec.recrec.domain.express.PaymentAccount;
import com.tractionrec.recrec.domain.express.PaymentAccountQueryResponse;
import com.tractionrec.recrec.domain.result.PaymentAccountQueryResult;
import gg.jte.TemplateEngine;
import gg.jte.TemplateOutput;
import gg.jte.output.StringOutput;
import org.apache.commons.text.StringEscapeUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class PaymentAccountQueryService extends QueryService {

    private PaymentAccountQueryService(Boolean isProduction, TemplateEngine templateEngine) {
        super(isProduction, templateEngine);
    }

    public PaymentAccountQueryResult queryForPaymentAccount(String accountId, String accountToken, QueryItem item) {
        try (HttpClient client = HttpClient.newHttpClient()) {
            String requestBody = getPaBody(accountId, accountToken, item);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(getServicesUri())
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .header("SOAPAction", "https://services.elementexpress.com/PaymentAccountQuery")
                    .header("Content-Type", "text/xml")
                    .build();
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 200) {
                    return new PaymentAccountQueryResult(item, ResultStatus.ERROR, "Status Code: " + response.statusCode());
                }
                final String responseBody = response.body();
                final String responseElement = responseBody.substring(responseBody.indexOf("<response>"), responseBody.indexOf("</PaymentAccountQueryResponse>")).trim();

                final PaymentAccountQueryResponse queryResponse = mapper.readValue(responseElement, PaymentAccountQueryResponse.class);
                if (queryResponse.responseCode == 90) {
                    return new PaymentAccountQueryResult(item, ResultStatus.NOT_FOUND, queryResponse.responseMessage);
                }
                if (queryResponse.responseCode != 0) {
                    return new PaymentAccountQueryResult(item, ResultStatus.ERROR, queryResponse.responseMessage);
                }
                final String unescapedQueryData = StringEscapeUtils.unescapeXml(queryResponse.queryData);
                final List<PaymentAccount> results = mapper.readValue(unescapedQueryData, new TypeReference<>() {
                });
                return new PaymentAccountQueryResult(item, ResultStatus.SUCCESS, queryResponse.responseMessage, results);
            } catch (Exception ex) {
                ex.printStackTrace();
                return new PaymentAccountQueryResult(item, ResultStatus.ERROR, ex.getMessage());
            }
        }
    }

    public static PaymentAccountQueryService forProduction(TemplateEngine templateEngine) {
        return new PaymentAccountQueryService(true, templateEngine);
    }

    public static PaymentAccountQueryService forTest(TemplateEngine templateEngine) {
        return new PaymentAccountQueryService(false, templateEngine);
    }

    private String getPaBody(String accountId, String accountToken, QueryItem item) {
        TemplateOutput output = new StringOutput();
        templateEngine.render("paymentAccountQueryPOSTBody.jte", Map.of(
                "accountId", accountId,
                "accountToken", accountToken,
                "queryItem", item
        ), output);
        return output.toString();
    }

    private URI getServicesUri() {
        return isProduction ? URI.create("https://services.elementexpress.com/express.asmx") : URI.create("https://certservices.elementexpress.com/express.asmx");
    }

}
