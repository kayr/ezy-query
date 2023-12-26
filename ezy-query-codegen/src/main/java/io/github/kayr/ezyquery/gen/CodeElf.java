package io.github.kayr.ezyquery.gen;

import static io.github.kayr.ezyquery.gen.StringCaseUtil.toScreamingSnakeCase;

import com.squareup.javapoet.*;
import io.github.kayr.ezyquery.api.Field;
import java.util.EnumSet;
import javax.lang.model.element.Modifier;

public class CodeElf {

  private CodeElf() {}

  public static MethodSpec.Builder publicMethod(
      String name, Class<?> method, Class<?>... annotations) {

    return method(modifiers(Modifier.PUBLIC), name, TypeName.get(method), annotations);
  }

  public static MethodSpec.Builder publicMethod(
      String name, TypeName returnType, Class<?>... annotations) {

    MethodSpec.Builder method = MethodSpec.methodBuilder(name).addModifiers(Modifier.PUBLIC);

    if (returnType != TypeName.VOID) method.returns(returnType);

    addNotations(annotations, method);
    return method;
  }

  public static MethodSpec.Builder method(
      EnumSet<Modifier> modifiers, String name, TypeName returnType, Class<?>... annotations) {

    MethodSpec.Builder method = MethodSpec.methodBuilder(name).addModifiers(modifiers);
    addNotations(annotations, method);

    if (returnType != TypeName.VOID) method.returns(returnType);

    return method;
  }

  public static EnumSet<Modifier> modifiers(Modifier... modifiers) {
    return EnumSet.of(modifiers[0], modifiers);
  }

  private static void addNotations(Class<?>[] annotations, MethodSpec.Builder method) {
    for (Class<?> annotation : annotations) {
      method.addAnnotation(annotation);
    }
  }

  static FieldSpec createField(EzyQueryFieldSpec f, Modifier... modifiers) {
    return FieldSpec.builder(
            paramType(Field.class, f.getDataType()), toScreamingSnakeCase(f.getAlias()), modifiers)
        .initializer(
            "$T.of($S, $S, $T.class,$T.$L)",
            Field.class,
            f.getSqlField(),
            f.getAlias(),
            f.getDataType(),
            Field.ExpressionType.class,
            f.getExpressionType().name())
        .build();
  }

  public static ParameterizedTypeName paramType(Class<?> clazz, TypeName dataType) {
    return ParameterizedTypeName.get(ClassName.get(clazz), dataType);
  }

  public static ParameterizedTypeName paramType(Class<?> clazz, Class<?> dataType) {
    return ParameterizedTypeName.get(ClassName.get(clazz), ClassName.get(dataType));
  }
}
