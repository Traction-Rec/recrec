package com.tractionrec.recrec.domain;

import com.tractionrec.recrec.domain.express.Transaction;

import java.util.Optional;

public record QueryResult(QueryItem item, ResultStatus status, String expressResponseMessage, Optional<Transaction> expressTransaction) {
}
