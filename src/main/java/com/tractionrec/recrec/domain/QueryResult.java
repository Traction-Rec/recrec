package com.tractionrec.recrec.domain;

import com.tractionrec.recrec.domain.express.ExpressEntity;

import java.util.List;
import java.util.Optional;

public record QueryResult<E extends ExpressEntity>(QueryItem item, ResultStatus status, String expressResponseMessage, Optional<List<E>> expressEntities) {
}
