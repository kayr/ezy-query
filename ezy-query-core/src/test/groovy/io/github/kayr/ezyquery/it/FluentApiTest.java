package io.github.kayr.ezyquery.it;

import io.github.kayr.ezyquery.EzySql;
import io.github.kayr.ezyquery.testqueries.CustomerReps;

public class FluentApiTest {

  Db db;
  EzySql ezySql;

  void setup() {
    db = new Db();
    ezySql = db.ezySql();
  }

  public void testList() {
    ezySql.from(CustomerReps.Q).list();
  }

  public void testCount() {
    ezySql
        .from(CustomerReps.Q)
        .where(CustomerReps.FIELD_CUSTOMER_NAME.eq("John"))
        .limit(10, 20)
        .count();
  }
}
