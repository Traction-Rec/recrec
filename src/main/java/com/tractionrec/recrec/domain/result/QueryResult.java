package com.tractionrec.recrec.domain.result;

import com.tractionrec.recrec.domain.QueryItem;
import com.tractionrec.recrec.domain.ResultStatus;
import com.tractionrec.recrec.domain.express.ExpressEntity;
import com.tractionrec.recrec.domain.output.OutputRow;

import java.util.List;

public abstract class QueryResult<E extends ExpressEntity, O extends OutputRow> {

    protected final QueryItem item;
    protected final ResultStatus status;
    protected final String expressResponseMessage;
    protected final List<E> expressEntities;
    protected final Class<O> outputRowType;

    protected QueryResult(QueryItem item, ResultStatus status, String expressResponseMessage, List<E> expressEntities, Class<O> outputRowType) {
        this.item = item;
        this.status = status;
        this.expressResponseMessage = expressResponseMessage;
        this.expressEntities = expressEntities;
        this.outputRowType = outputRowType;
    }

    public Class<O> getOutputRowType() {
        return outputRowType;
    }

    public List<E> getExpressEntities() {
        return expressEntities;
    }

    public String getExpressResponseMessage() {
        return expressResponseMessage;
    }

    public ResultStatus getStatus() {
        return status;
    }

    public QueryItem getItem() {
        return item;
    }

    public abstract List<O> getOutputRows();

}