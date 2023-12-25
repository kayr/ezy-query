package io.github.kayr.ezyquery.gen;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import java.util.EnumSet;
import javax.lang.model.element.Modifier;

public class CodeElf {

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
}
