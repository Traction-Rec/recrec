package com.tractionrec.recrec.domain.express;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "response")
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentAccountQueryResponse {
    @JacksonXmlProperty(localName = "ExpressResponseCode")
    public int responseCode;
    @JacksonXmlProperty(localName = "ExpressResponseMessage")
    public String responseMessage;
    @JacksonXmlProperty(localName = "QueryData")
    public String queryData;
}
