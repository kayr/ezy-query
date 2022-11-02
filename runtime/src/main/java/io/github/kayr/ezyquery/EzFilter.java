package io.github.kayr.ezyquery;

import io.github.kayr.ezyquery.api.FilterParams;
import io.github.kayr.ezyquery.api.cnd.Cnd;
import io.github.kayr.ezyquery.sample.MyQuery;
import java.util.List;

public class EzFilter {

  public static void main(String[] args) {
    EzySql sql = EzySql.withProvider(null);

    List<MyQuery.Result> results =
        sql.list(
            MyQuery.Q,
            new FilterParams()
                .where(
                    Cnd.eq(MyQuery.FIELD_BIGDECIMALVAL, 303),
                    Cnd.eq(MyQuery.FIELD_LONGVAL, 303),
                    Cnd.eq(MyQuery.FIELD_BOOLEANVAL, 303)));
  }
}
