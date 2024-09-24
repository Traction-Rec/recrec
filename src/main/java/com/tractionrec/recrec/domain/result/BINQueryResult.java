package com.tractionrec.recrec.domain.result;

import com.tractionrec.recrec.domain.QueryItem;
import com.tractionrec.recrec.domain.ResultStatus;
import com.tractionrec.recrec.domain.express.EnhancedBIN;
import com.tractionrec.recrec.domain.output.BINQueryOutputRow;

import java.util.ArrayList;
import java.util.List;

public class BINQueryResult extends QueryResult<EnhancedBIN, BINQueryOutputRow> {
    public BINQueryResult(QueryItem item, ResultStatus status, String expressResponseMessage) {
        this(item, status, expressResponseMessage, new ArrayList<>());
    }
    public BINQueryResult(QueryItem item, ResultStatus status, String expressResponseMessage, List<EnhancedBIN> expressEntities) {
        super(item, status, expressResponseMessage, expressEntities, BINQueryOutputRow.class);
    }

    @Override
    public List<BINQueryOutputRow> getOutputRows() {
        if(this.expressEntities.isEmpty()) {
            return List.of(new BINQueryOutputRow(this));
        }
        List<BINQueryOutputRow> rows = new ArrayList<>(this.expressEntities.size());
        for (EnhancedBIN current : this.expressEntities) {
            rows.add(new BINQueryOutputRow(this, current));
        }
        return rows;
    }

}
