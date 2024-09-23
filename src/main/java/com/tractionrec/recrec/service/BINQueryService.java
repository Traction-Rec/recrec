package com.tractionrec.recrec.service;

import com.tractionrec.recrec.domain.QueryItem;
import com.tractionrec.recrec.domain.ResultStatus;
import com.tractionrec.recrec.domain.express.BINQueryResponse;
import com.tractionrec.recrec.domain.express.EnhancedBIN;
import com.tractionrec.recrec.domain.result.BINQueryResult;
import gg.jte.TemplateEngine;
import gg.jte.TemplateOutput;
import gg.jte.output.StringOutput;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BINQueryService extends QueryService {

    private BINQueryService(Boolean isProduction, TemplateEngine templateEngine) {
        super(isProduction, templateEngine);
    }

    public BINQueryResult queryForBINInfo(String accountId, String accountToken, QueryItem item) {
        HttpClient client = HttpClient.newHttpClient();
        String requestBody = getBINBody(accountId, accountToken, item);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(getTransactionURI())
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .header("SOAPAction", "https://transaction.elementexpress.com/EnhancedBINQuery")
                .header("Content-Type", "text/xml")
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() != 200) {
                return new BINQueryResult(item, ResultStatus.ERROR, "Status Code: " + response.statusCode());
            }
            final String responseBody = response.body();
            final String responseElement = responseBody.substring(responseBody.indexOf("<response>"), responseBody.indexOf("</EnhancedBINQueryResponse>")).trim();

            final BINQueryResponse queryResponse = mapper.readValue(responseElement, BINQueryResponse.class);
            if(queryResponse.responseCode == 90) {
                return new BINQueryResult(item, ResultStatus.NOT_FOUND, queryResponse.responseMessage);
            }
            if(queryResponse.responseCode != 0) {
                return new BINQueryResult(item, ResultStatus.ERROR, queryResponse.responseMessage);
            }
            final List<EnhancedBIN> entities = new ArrayList<>(List.of(queryResponse.enhancedBin));
            return new BINQueryResult(item, ResultStatus.SUCCESS, queryResponse.responseMessage, entities);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new BINQueryResult(item, ResultStatus.ERROR, ex.getMessage());
        }
    }

    public static BINQueryService forProduction(TemplateEngine templateEngine) {
        return new BINQueryService(true, templateEngine);
    }

    public static BINQueryService forTest(TemplateEngine templateEngine) {
        return new BINQueryService(false, templateEngine);
    }

    private String getBINBody(String accountId, String accountToken, QueryItem item) {
        TemplateOutput output = new StringOutput();
        templateEngine.render("binQueryPOSTBody.jte", Map.of(
                "accountId", accountId,
                "accountToken", accountToken,
                "queryItem", item
        ), output);
        return output.toString();
    }

    private URI getTransactionURI() {
        return isProduction ? URI.create("https://transaction.elementexpress.com/express.asmx") : URI.create("https://certtransaction.elementexpress.com/express.asmx");
    }

}
