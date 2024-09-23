package com.tractionrec.recrec.domain.result;

import com.tractionrec.recrec.domain.QueryItem;
import com.tractionrec.recrec.domain.ResultStatus;
import com.tractionrec.recrec.domain.express.PaymentAccount;
import com.tractionrec.recrec.domain.output.PaymentAccountQueryOutputRow;

import java.util.ArrayList;
import java.util.List;

public class PaymentAccountQueryResult extends QueryResult<PaymentAccount, PaymentAccountQueryOutputRow> {
    public PaymentAccountQueryResult(QueryItem item, ResultStatus status, String expressResponseMessage) {
        this(item, status, expressResponseMessage, new ArrayList<>());
    }
    public PaymentAccountQueryResult(QueryItem item, ResultStatus status, String expressResponseMessage, List<PaymentAccount> expressEntities) {
        super(item, status, expressResponseMessage, expressEntities, PaymentAccountQueryOutputRow.class);
    }

    @Override
    public List<PaymentAccountQueryOutputRow> getOutputRows() {
        if(this.expressEntities.isEmpty()) {
            return List.of(new PaymentAccountQueryOutputRow(this));
        }
        List<PaymentAccountQueryOutputRow> rows = new ArrayList<>(this.expressEntities.size());
        for (PaymentAccount current : this.expressEntities) {
            rows.add(new PaymentAccountQueryOutputRow(this, current));
        }
        return rows;
    }

}
