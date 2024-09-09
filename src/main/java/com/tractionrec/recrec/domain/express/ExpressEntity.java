package com.tractionrec.recrec.domain.express;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.time.LocalDate;
import java.time.LocalTime;

@JacksonXmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class ExpressEntity {
    @JacksonXmlProperty(localName = "ExpressTransactionDate")
    public LocalDate transactionDate;
    @JacksonXmlProperty(localName = "ExpressTransactionTime")
    public LocalTime transactionTime;
}
