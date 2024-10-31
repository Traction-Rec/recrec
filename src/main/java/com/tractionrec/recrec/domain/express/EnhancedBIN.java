package com.tractionrec.recrec.domain.express;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnhancedBIN extends ExpressEntity {
    @JacksonXmlProperty(localName = "Status")
    public String binStatus;
    @JacksonXmlProperty(localName = "CreditCard")
    public String isCreditCard;
}
