package io.github.kayr.ezyquery.sample;

import io.github.kayr.ezyquery.api.Field;

import javax.annotation.Generated;

@Generated(value = "EzyQuery", date = "2020-01-01T00:00:00.000+0000")
public class TransactionQuery {

  public static final Field NAME = new Field("c.name", "name");
  public static final Field SEX = new Field("c.sex", "sex");
  public static final Field AGE = new Field("c.age", "age");

  private String shema = "m_share_account msa\n"
                           + "LEFT OUTER JOIN m_client cl ON cl.id = msa.client_id\n"
                           + "LEFT OUTER JOIN m_group grp ON grp.id = msa.group_id\n"
                           + "LEFT OUTER JOIN i_institution inst ON inst.client_id = cl.id\n"
                           + "LEFT OUTER JOIN m_share_product msp ON msp.id = msa.product_id\n"
                           + "LEFT OUTER JOIN m_share_product_market_price prod_price ON prod_price.product_id = msp.id\n"
                           + "LEFT OUTER JOIN m_office mo_cl ON cl.office_id = mo_cl.id\n"
                           + "LEFT OUTER JOIN m_office mo_grp ON grp.office_id = mo_grp.id\n";
}
