/* (C)2022 */
package io.github.kayr.ezyquery;

import io.github.kayr.ezyquery.api.EzyCriteria;
import io.github.kayr.ezyquery.api.Field;
import io.github.kayr.ezyquery.parser.QueryAndParams;
import io.github.kayr.ezyquery.parser.SqlParts;
import java.util.List;
import java.util.Optional;

public interface EzyQuery<T> {

  QueryAndParams query(EzyCriteria params);

  List<Field<?>> fields();

  SqlParts schema();

  default Class<T> resultClass() {
    return null;
  }

  default Optional<SqlParts> whereClause() {
    return Optional.empty();
  }

  default Optional<SqlParts> orderByClause() {
    return Optional.empty();
  }
}
