/* (C)2022 */
package io.github.kayr.ezyquery.gen;

import com.squareup.javapoet.*;
import io.github.kayr.ezyquery.EzyQuery;
import io.github.kayr.ezyquery.api.Field;
import io.github.kayr.ezyquery.api.FilterParams;
import io.github.kayr.ezyquery.api.SqlBuilder;
import io.github.kayr.ezyquery.parser.QueryAndParams;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Generated;
import javax.lang.model.element.Modifier;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;

public class QueryGen {

  private final String sql;
  private final String className;
  private final String packageName;

  public QueryGen(String sql, String className, String packageName) {
    this.sql = sql;
    this.className = className;
    this.packageName = packageName;
  }

  public JavaFile javaCode() throws JSQLParserException {
    Statement statement = CCJSqlParserUtil.parse(sql);

    if (!(statement instanceof Select)) {
      throw new IllegalArgumentException("Only SELECT statements are supported");
    }

    Select select = (Select) statement;

    if (!(select.getSelectBody() instanceof PlainSelect)) {
      throw new IllegalArgumentException("Only SELECT statements are supported");
    }

    PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
    List<Field<?>> fieldList = extractFields(plainSelect);

    return buildCode(fieldList, plainSelect);
  }

  private JavaFile buildCode(List<Field<?>> fieldList, PlainSelect plainSelect) {

    List<FieldSpec> fConstants = fieldConstants(fieldList);

    FieldSpec fSchema = fieldSchema(plainSelect);

    FieldSpec fFields = fieldAllFields();

    MethodSpec mConstructor = methodConstructor();

    MethodSpec mInit = methodInit(fConstants, fFields);

    TypeSpec resultClass = resultClass(fieldList);
    ClassName resultClassName = ClassName.get(packageName, className, resultClass.name);

    // main query method
    MethodSpec queryMethod =
        MethodSpec.methodBuilder("query")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(FilterParams.class, "criteria")
            .returns(QueryAndParams.class)
            .addStatement(
                "$T builder = $T.with(fields, criteria)", SqlBuilder.class, SqlBuilder.class)
            .addStatement("$T s = builder.selectStmt()", String.class)
            .addStatement("$T w = builder.whereStmt()", QueryAndParams.class)
            .addStatement("$T sb = new StringBuilder()", StringBuilder.class)
            .addStatement(
                "sb.append(\"SELECT \").append(s)\n"
                    + "  .append(\" FROM \").append(schema)\n"
                    + "  .append(\" WHERE \").append(w)")
            .beginControlFlow("if (!criteria.isCount())")
            .addStatement("sb.append(\" LIMIT \").append(criteria.getLimit())")
            .addStatement("sb.append(\" OFFSET \").append(criteria.getOffset())")
            .endControlFlow()
            .addStatement("return new $T(sb.toString(), w.getParams())", QueryAndParams.class)
            .build();

    // result class override method
    MethodSpec resultClassMethod =
        MethodSpec.methodBuilder("resultClass")
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Override.class)
            .returns(ParameterizedTypeName.get(ClassName.get(Class.class), resultClassName))
            .addStatement("    return $T.class", resultClassName)
            .build();

    // fields override method
    MethodSpec fieldsMethod =
        MethodSpec.methodBuilder("fields")
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Override.class)
            .returns(typeListOfFields())
            .addStatement("return $T.emptyList()", Collections.class)
            .build();

    // the class
    TypeSpec finalClazz =
        TypeSpec.classBuilder(className)
            .addModifiers(Modifier.PUBLIC)
            .addJavadoc(sql)
            .addSuperinterface(
                ParameterizedTypeName.get(ClassName.get(EzyQuery.class), resultClassName))
            .addFields(fConstants)
            .addField(fSchema)
            .addField(fFields)
            .addMethod(mConstructor)
            .addMethod(mInit)
            .addMethod(queryMethod)
            .addAnnotation(
                AnnotationSpec.builder(Generated.class)
                    .addMember("value", "$S", QueryGen.class.getName())
                    .addMember(
                        "date", "$S", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                    .build())
            .addMethod(fieldsMethod)
            .addMethod(resultClassMethod)
            .addType(resultClass)
            .build();

    return JavaFile.builder(packageName, finalClazz).build();
  }

  private TypeSpec resultClass(List<Field<?>> fieldList) {
    TypeSpec.Builder resultClassBuilder =
        TypeSpec.classBuilder("Result").addModifiers(Modifier.PUBLIC, Modifier.STATIC);
    for (Field<?> f : fieldList) {
      resultClassBuilder.addField(
          FieldSpec.builder(f.getDataType(), f.getAlias(), Modifier.PUBLIC).build());
    }
    TypeSpec resultClass = resultClassBuilder.build();
    return resultClass;
  }

  /** Init() method */
  private MethodSpec methodInit(List<FieldSpec> fConstants, FieldSpec fFields) {
    MethodSpec.Builder initBuilder =
        MethodSpec.methodBuilder("init").addModifiers(Modifier.PRIVATE);
    for (FieldSpec field : fConstants) {
      initBuilder.addStatement("$N.add($N)", fFields, field);
    }
    return initBuilder.build();
  }

  /** Constructor() method */
  private MethodSpec methodConstructor() {
    return MethodSpec.constructorBuilder()
        .addModifiers(Modifier.PUBLIC)
        .addStatement("init()")
        .build();
  }

  /** allFields field */
  private FieldSpec fieldAllFields() {

    ParameterizedTypeName fListOfFields = typeListOfFields();

    return FieldSpec.builder(fListOfFields, "fields", Modifier.PRIVATE, Modifier.FINAL)
        .initializer("new $T<$T<?>>()", ArrayList.class, Field.class)
        .build();
  }

  private static ParameterizedTypeName typeListOfFields() {
    ParameterizedTypeName wildField = typeFieldWildCard();
    return ParameterizedTypeName.get(ClassName.get(List.class), wildField);
  }

  private static ParameterizedTypeName typeFieldWildCard() {
    WildcardTypeName wildcardTypeName = WildcardTypeName.subtypeOf(Object.class);
    return ParameterizedTypeName.get(ClassName.get(Field.class), wildcardTypeName);
  }

  /** schema field */
  private FieldSpec fieldSchema(PlainSelect plainSelect) {
    CodeBlock.Builder schemaString1 =
        CodeBlock.builder().add("$S\n", plainSelect.getFromItem().toString());
    for (Join j : plainSelect.getJoins()) {
      schemaString1.add("          + $S\n", j.toString());
    }

    return FieldSpec.builder(String.class, "schema", Modifier.PRIVATE, Modifier.FINAL)
        .initializer(schemaString1.build())
        .build();
  }

  private List<FieldSpec> fieldConstants(List<Field<?>> fieldList) {
    // Constant fields
    return fieldList.stream()
        .map(
            f ->
                FieldSpec.builder(
                        ParameterizedTypeName.get(Field.class, f.getDataType()),
                        constantName(f.getAlias()),
                        Modifier.PUBLIC,
                        Modifier.STATIC)
                    .initializer(
                        "$T.of($S, $S, $T.class)",
                        Field.class,
                        f.getSqlField(),
                        f.getAlias(),
                        f.getDataType())
                    .build())
        .collect(Collectors.toList());
  }

  String constantName(String name) {
    return "FIELD_" + toSnakeCase(name);
  }

  String toSnakeCase(String name) {
    return name.replaceAll("([A-Z])", "_$1").toUpperCase();
  }

  public List<Field<?>> extractFields(PlainSelect plainSelect) {

    return plainSelect.getSelectItems().stream()
        .map(selectItem -> toField((SelectExpressionItem) selectItem))
        .collect(Collectors.toList());
  }

  private Field<?> toField(SelectExpressionItem selectItem) {
    String alias =
        Objects.requireNonNull(
                selectItem.getAlias(), "Alias if required for filed [" + selectItem + "]")
            .getName();
    alias = unquote(alias);

    String[] parts = alias.contains("_") ? alias.split("_") : new String[] {alias, "object"};

    String aliasName = parts[0];
    String typeName = parts[1];

    Class<?> type = resolveType(typeName);
    String sqlField = selectItem.getExpression().toString();
    return Field.of(sqlField, aliasName, type);
  }

  Class<?> resolveType(String type) {
    Map<String, Class<?>> typeMap = new HashMap<>();

    typeMap.put("int", Integer.class);
    typeMap.put("long", Long.class);
    typeMap.put("float", Float.class);
    typeMap.put("double", Double.class);
    typeMap.put("boolean", Boolean.class);
    typeMap.put("string", String.class);
    typeMap.put("date", java.util.Date.class);
    typeMap.put("time", java.sql.Timestamp.class);
    typeMap.put("decimal", java.math.BigDecimal.class);
    typeMap.put("bigint", java.math.BigInteger.class);
    typeMap.put("byte", Byte.class);
    typeMap.put("object", Object.class);

    Class<?> aClass = typeMap.get(type);
    if (aClass == null) throw new IllegalArgumentException("Unsupported type: " + type);
    return aClass;
  }

  String unquote(String s) {
    return s.replaceAll("^\"|\"$", "").replaceAll("^'|'$", "");
  }
}