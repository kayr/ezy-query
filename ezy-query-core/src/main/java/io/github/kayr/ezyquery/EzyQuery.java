/* (C)2022 */
package io.github.kayr.ezyquery;

import io.github.kayr.ezyquery.api.EzyCriteria;
import io.github.kayr.ezyquery.api.Field;
import io.github.kayr.ezyquery.parser.QueryAndParams;
import java.util.List;
import java.util.Optional;

public interface EzyQuery<T> {

  QueryAndParams query(EzyCriteria params);

  Class<T> resultClass();

  List<Field<?>> fields();

  String schema();

  default Optional<String> whereClause() {
    return Optional.empty();
  }

  default Optional<String> orderByClause() {
    return Optional.empty();
  }
}
