package io.github.kayr.ezyquery.gen;

import com.squareup.javapoet.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.lang.model.element.Modifier;
import lombok.SneakyThrows;
import net.sf.jsqlparser.JSQLParserException;

@lombok.RequiredArgsConstructor(staticName = "of")
public class EzySqlQueryGen implements WritesCode {

  private final String packageName;
  private final String mainClassName;
  private final String sql;
  private final Properties properties;

  public JavaFile generate() {
    List<SectionsParser.Section> sections = SectionsParser.splitUp(sql);

    List<MethodSpec> mSections = new ArrayList<>();

    List<TypeSpec> types = new ArrayList<>();
    for (SectionsParser.Section section : sections) {
      try {
        Pair<QueryKind, TypeSpec> kindAndClass = toJavaClass(section);

        // add the types
        TypeSpec theType = kindAndClass.getTwo();
        QueryKind theTypeKind = kindAndClass.getOne();
        types.add(theType);

        // add the method

        String methodName = StringCaseUtil.toCamelCase(theType.name);
        MethodSpec.Builder method =
            MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ClassName.get(packageName, mainClassName, theType.name));

        if (theTypeKind == QueryKind.DYNAMIC) {
          String staticName = StringCaseUtil.toScreamingSnakeCase(theType.name);
          String className = StringCaseUtil.toPascalCase(theType.name);
          method.addStatement("return $L.$L", className, staticName);
        } else {
          method.addStatement("return new $N()", theType);
        }

        mSections.add(method.build());

      } catch (Exception e) {
        throw new CodeGenException("Failed to parse section :" + section.name(), e);
      }
    }

    TypeSpec.Builder builder =
        TypeSpec.classBuilder(ClassName.get(packageName, mainClassName))
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(
                AnnotationSpec.builder(CodeElf.resolveGeneratedAnnotation())
                    .addMember("value", "$S", EzySqlQueryGen.class.getName())
                    .addMember("date", "$S", TimeElf.nowStr())
                    .build())
            .addTypes(types)
            .addMethods(mSections);

    return JavaFile.builder(packageName, builder.build()).build();
  }

  @SneakyThrows
  private Pair<QueryKind, TypeSpec> toJavaClass(SectionsParser.Section section) {

    Pair<QueryKind, String> typeAndName = extractName(section.name());

    if (typeAndName.getOne() == QueryKind.STATIC) {
      return Pair.of(typeAndName.getOne(), generateStaticClass(typeAndName.getTwo(), section));
    } else {
      return Pair.of(QueryKind.DYNAMIC, generateDynamicClass(typeAndName.getTwo(), section));
    }
  }

  private TypeSpec generateDynamicClass(String className, SectionsParser.Section section)
      throws JSQLParserException {
    return new QueryGen(
            packageName + "." + mainClassName,
            StringCaseUtil.toPascalCase(className),
            section.sql(),
            new Properties())
        .buildClass()
        .toBuilder()
        .addModifiers(Modifier.STATIC)
        .build();
  }

  private TypeSpec generateStaticClass(String sectionName, SectionsParser.Section section) {
    return StaticQueryGen.of(packageName, mainClassName, section.sql())
        .createSectionClass(sectionName, section.sql())
        .toBuilder()
        .addModifiers(Modifier.STATIC)
        .build();
  }

  private static Pair<QueryKind, String> extractName(String name) {
    String trimmed = name.trim();
    if (trimmed.startsWith("static")) {
      return Pair.of(QueryKind.STATIC, trimmed.substring(7).trim());
    } else if (trimmed.startsWith("dynamic")) {
      return Pair.of(QueryKind.DYNAMIC, trimmed.substring(8).trim());
    } else {
      return Pair.of(QueryKind.STATIC, trimmed);
    }
  }

  @Override
  @lombok.SneakyThrows
  public Path writeTo(Path path) {
    return generate().writeToPath(path, StandardCharsets.UTF_8);
  }

  private enum QueryKind {
    STATIC,
    DYNAMIC
  }
}
