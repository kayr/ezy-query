/* (C)2022 */
package io.github.kayr.ezyquery;

import io.github.kayr.ezyquery.api.Field;
import io.github.kayr.ezyquery.api.FilterParams;
import io.github.kayr.ezyquery.parser.QueryAndParams;
import java.util.List;

public interface EzyQuery<T> {

  QueryAndParams query(FilterParams params);

  Class<T> resultClass();

  List<Field<?>> fields();
}
