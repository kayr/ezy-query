package io.github.kayr.ezyquery.gen.sample;

import io.github.kayr.ezyquery.EzySql;
import io.github.kayr.ezyquery.api.FilterParams;
import io.github.kayr.ezyquery.api.cnd.Cnd;
import io.github.kayr.ezyquery.api.cnd.Conds;

import java.util.List;

public class ApiUsage {

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

    public static void main2(String[] args) {

        EzySql ez = EzySql.withProvider(null);

        ez.list(
          TransactionQuery.Q,
          FilterParams.selectCount()
            .where(
              Cnd.or(
                Cnd.andAll(
                  Cnd.eq(TransactionQuery.NAME, "Ronald"),
                  Cnd.eq(TransactionQuery.NAME, "Ronald"),
                  Cnd.eq(TransactionQuery.NAME, "Ronald"),
                  Cnd.eq(TransactionQuery.NAME, "Ronald")),
                Cnd.orAll(
                  Cnd.eq(TransactionQuery.NAME, "Ronald"),
                  Cnd.eq(TransactionQuery.NAME, "Ronald"),
                  Cnd.eq(TransactionQuery.NAME, "Ronald"),
                  Cnd.eq(TransactionQuery.NAME, "Ronald")))));

        Conds conds = Cnd.orAll(Cnd.not("#reversed"), Cnd.orAll("#reversed", Cnd.isNotNull("#id")));

        System.out.println(conds.asExpr());
    }
}
