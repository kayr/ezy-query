package io.github.kayr.ezyquery.gen;

import com.squareup.javapoet.TypeName;
import io.github.kayr.ezyquery.api.Field;

@lombok.Getter
@lombok.AllArgsConstructor(staticName = "of")
class EzyQueryFieldSpec {
  private String sqlField;
  private String alias;
  private TypeName dataType;
  private Field.ExpressionType expressionType;
}
