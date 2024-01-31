package io.github.kayr.ezyquery.api.cnd;

import java.util.Collection;

public interface IOperand {

  // Utility condition methods
  default Cond eq(Object value) {
    return Cnd.eq(this, value);
  }

  default Cond neq(Object value) {
    return Cnd.neq(this, value);
  }

  default Cond gt(Object value) {
    return Cnd.gt(this, value);
  }

  default Cond lt(Object value) {
    return Cnd.lt(this, value);
  }

  default Cond gte(Object value) {
    return Cnd.gte(this, value);
  }

  default Cond lte(Object value) {
    return Cnd.lte(this, value);
  }

  default Cond like(Object value) {
    return Cnd.like(this, value);
  }

  default Cond notLike(Object value) {
    return Cnd.notLike(this, value);
  }

  default Cond in(Collection<?> value) {
    return Cnd.in(this, value);
  }

  default Cond in(Object value, Object... values) {
    return Cnd.in(this, value, values);
  }

  default Cond notIn(Object value, Object... values) {
    return Cnd.notIn(this, value, values);
  }

  default Cond notIn(Collection<?> values) {
    return Cnd.notIn(this, values);
  }

  default Conds and(Object value) {
    return Cnd.and(this, value);
  }

  default Conds or(Object value) {
    return Cnd.or(this, value);
  }

  default UnaryCond negate() {
    return Cnd.negate(this);
  }

  default UnaryCond positive() {
    return Cnd.positive(this);
  }

  default UnaryCond isNull() {
    return Cnd.isNull(this);
  }

  default UnaryCond isNotNull() {
    return Cnd.isNotNull(this);
  }

  default BetweenCond between(Object value1, Object value2) {
    return Cnd.between(this, value1, value2);
  }

  default BetweenCond notBetween(Object value1, Object value2) {
    return Cnd.notBetween(this, value1, value2);
  }
}
