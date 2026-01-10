package io.github.kayr.ezyquery.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/** Interface to allow custom binding of parameters to a PreparedStatement. */
@FunctionalInterface
public interface ParameterBinder {
  void bind(PreparedStatement ps, int index, Object value) throws SQLException;
}
