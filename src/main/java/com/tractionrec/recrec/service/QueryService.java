package com.tractionrec.recrec.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.tractionrec.recrec.domain.QueryItem;
import com.tractionrec.recrec.domain.QueryResult;
import com.tractionrec.recrec.domain.ResultStatus;
import com.tractionrec.recrec.domain.express.Transaction;
import com.tractionrec.recrec.domain.express.TransactionQueryResponse;
import gg.jte.TemplateEngine;
import gg.jte.TemplateOutput;
import gg.jte.output.StringOutput;
import org.apache.commons.text.StringEscapeUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class QueryService {

    private final boolean isProduction;
    private final TemplateEngine templateEngine;
    private final ObjectMapper mapper;

    private QueryService(Boolean isProduction, TemplateEngine templateEngine) {
        this.isProduction = isProduction;
        this.templateEngine = templateEngine;
        this.mapper = new XmlMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyyMMdd")));
        module.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern("HHmmss")));
        mapper.registerModule(module);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    public QueryResult queryForTransaction(String accountId, String accountToken, QueryItem item) {
        HttpClient client = HttpClient.newHttpClient();
        String requestBody = getBody(accountId, accountToken, item);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(getUri())
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .header("SOAPAction", "https://reporting.elementexpress.com/TransactionQuery")
                .header("Content-Type", "text/xml")
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() != 200) {
                return new QueryResult(item, ResultStatus.ERROR, "Status Code: " + response.statusCode(), Optional.empty());
            }
            final String responseBody = response.body();
            final String responseElement = responseBody.substring(responseBody.indexOf("<response>"), responseBody.indexOf("</TransactionQueryResponse>")).trim();

            final TransactionQueryResponse queryResponse = mapper.readValue(responseElement, TransactionQueryResponse.class);
            if(queryResponse.responseCode == 90) {
                return new QueryResult(item, ResultStatus.NOT_FOUND, queryResponse.responseMessage, Optional.empty());
            }
            if(queryResponse.responseCode != 0) {
                return new QueryResult(item, ResultStatus.ERROR, queryResponse.responseMessage, Optional.empty());
            }
            final String unescapedReportingData = StringEscapeUtils.unescapeXml(queryResponse.reportingData);
            final List<Transaction> results = mapper.readValue(unescapedReportingData, new TypeReference<List<Transaction>>() {});
            return new QueryResult(item, ResultStatus.SUCCESS, queryResponse.responseMessage, Optional.of(results));
        } catch (Exception ex) {
            ex.printStackTrace();
            return new QueryResult(item, ResultStatus.ERROR, ex.getMessage(), Optional.empty());
        }
    }

    public static QueryService forProduction(TemplateEngine templateEngine) {
        return new QueryService(true, templateEngine);
    }

    public static QueryService forTest(TemplateEngine templateEngine) {
        return new QueryService(false, templateEngine);
    }

    private String getBody(String accountId, String accountToken, QueryItem item) {
        TemplateOutput output = new StringOutput();
        templateEngine.render("transactionQueryPOSTBody.jte", Map.of(
                "accountId", accountId,
                "accountToken", accountToken,
                "queryItem", item
        ), output);
        return output.toString();
    }

    private URI getUri() {
        return isProduction ? URI.create("https://reporting.elementexpress.com/express.asmx") : URI.create("https://certreporting.elementexpress.com/express.asmx");
    }

}
