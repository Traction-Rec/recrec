package com.tractionrec.recrec.domain.output;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.tractionrec.recrec.domain.express.EnhancedBIN;
import com.tractionrec.recrec.domain.result.QueryResult;

@JsonPropertyOrder({
        "merchant", "id", "status", "message", "binStatus", "isCreditCard"
})
public class BINQueryOutputRow extends OutputRow {
    public String binStatus;
    public String isCreditCard;

    public BINQueryOutputRow(QueryResult<EnhancedBIN, BINQueryOutputRow> result) {
        super(result);
    }

    public BINQueryOutputRow(QueryResult<EnhancedBIN, BINQueryOutputRow> result, EnhancedBIN entity) {
        this(result);
        this.binStatus = entity.binStatus;
        this.isCreditCard = entity.isCreditCard;
    }

}
