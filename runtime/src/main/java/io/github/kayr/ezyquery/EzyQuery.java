package io.github.kayr.ezyquery;

import io.github.kayr.ezyquery.api.FilterParams;
import io.github.kayr.ezyquery.parser.QueryAndParams;

public interface EzyQuery<T> {

  QueryAndParams query(FilterParams params);

  Class<T> resultClass();
}
