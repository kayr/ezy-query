package io.github.kayr.ezyquery.gen;

import com.squareup.javapoet.*;
import io.github.kayr.ezyquery.EzyQuery;
import io.github.kayr.ezyquery.api.Field;
import io.github.kayr.ezyquery.api.FilterParams;
import io.github.kayr.ezyquery.api.SqlBuilder;
import io.github.kayr.ezyquery.parser.QueryAndParams;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;

import javax.lang.model.element.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class QueryGen {

  private final String sql;
  private final List<Field<?>> fields = new ArrayList<>();
  private String className;
  private String packageName = "io.github.kayr.ezyquery.sample";

  public QueryGen(String sql, String className) {
    this.sql = sql;
    this.className = className;
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
    FromItem fromItem = plainSelect.getFromItem();
    Expression defaultWhere = plainSelect.getWhere();

    return buildCode(fieldList, plainSelect);
  }

  private JavaFile buildCode(List<Field<?>> fieldList, PlainSelect plainSelect) {

    // Constant fields
    List<FieldSpec> constants =
        fieldList.stream()
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

    // schema bolock
    CodeBlock.Builder schemaString =
        CodeBlock.builder().add("$S\n", plainSelect.getFromItem().toString());

    plainSelect.getJoins().forEach(j -> schemaString.add("          + $S\n", j.toString()));

    // schema
    FieldSpec schema =
        FieldSpec.builder(String.class, "schema", Modifier.PRIVATE, Modifier.FINAL)
            .initializer(schemaString.build())
            .build();

    // all fields list
    FieldSpec allFieldsList =
        FieldSpec.builder(
                ParameterizedTypeName.get(List.class, Field.class),
                "fields",
                Modifier.PRIVATE,
                Modifier.FINAL)
            .initializer("new $T<$T<?>>()", ArrayList.class, Field.class)
            .build();

    // constructor
    MethodSpec constructor =
        MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addStatement("init()")
            .build();

    // init()
    MethodSpec.Builder initBuilder =
        MethodSpec.methodBuilder("init").addModifiers(Modifier.PRIVATE);
    for (FieldSpec field : constants) {
      initBuilder.addStatement("$N.add($N)", allFieldsList, field);
    }
    MethodSpec init = initBuilder.build();

    // inner Result static class
    TypeSpec.Builder resultClassBuilder =
        TypeSpec.classBuilder("Result").addModifiers(Modifier.PUBLIC, Modifier.STATIC);
    for (Field<?> f : fieldList) {
      resultClassBuilder.addField(
          FieldSpec.builder(f.getDataType(), f.getAlias(), Modifier.PUBLIC).build());
    }
    TypeSpec resultClass = resultClassBuilder.build();

    // main query method
    MethodSpec queryMethod =
        MethodSpec.methodBuilder("query")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(FilterParams.class, "criteria")
            .returns(QueryAndParams.class)
            .addStatement(
                "$T builder = $T.with(fields, criteria)", SqlBuilder.class, StringBuilder.class)
            .addStatement("$T s = builder.selectStmt()", String.class)
            .addStatement("$T w = builder.whereStmt()", QueryAndParams.class)
            .addStatement("$T sb = new StringBuilder()", StringBuilder.class)
            .addStatement(
                "sb.append(\"SELECT \").append(s)\n"
                    + "  .append(\" FROM \").append(schema))\n"
                    + "  .append(\" WHERE \").append(w)")
            .beginControlFlow("if (!criteria.isCount())")
            .addStatement("sb.append(\" LIMIT \").append(criteria.getLimit())")
            .addStatement("sb.append(\" OFFSET \").append(criteria.getOffset())")
            .endControlFlow()
            .addStatement("return new $T(sb.toString(), w.getParams())", QueryAndParams.class)
            .build();

    TypeSpec finalClazz =
        TypeSpec.classBuilder(className)
            .addModifiers(Modifier.PUBLIC)
            .addSuperinterface(
                ParameterizedTypeName.get(
                    ClassName.get(EzyQuery.class), ClassName.get(packageName, resultClass.name)))
            .addFields(constants)
            .addField(schema)
            .addField(allFieldsList)
            .addMethod(constructor)
            .addMethod(init)
            .addMethod(queryMethod)
            .addMethod(
                MethodSpec.methodBuilder("getFields")
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override.class)
                    .returns(ParameterizedTypeName.get(List.class, Field.class))
                    .addStatement("return $T.emptyList()", Collections.class)
                    .build())
            .addType(resultClass)
            .build();

    packageName = "io.github.kayr.ezyquery.gen";
    return JavaFile.builder(packageName, finalClazz).build();
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
    Class<?> type = resolveType(parts[1]);
    String sqlField = selectItem.getExpression().toString();
    return Field.of(sqlField, aliasName, type);
  }

  Class<?> resolveType(String type) {
    switch (type) {
      case "int":
        return Integer.class;
      case "long":
        return Long.class;
      case "float":
        return Float.class;
      case "double":
        return Double.class;
      case "boolean":
        return Boolean.class;
      case "string":
        return String.class;
      case "date":
        return java.util.Date.class;
      case "time":
        return java.sql.Timestamp.class;
      case "decimal":
        return java.math.BigDecimal.class;
      case "bigint":
        return java.math.BigInteger.class;
      case "byte":
        return Byte.class;
      case "object":
        return Object.class;
      default:
        throw new IllegalArgumentException("Unsupported type: " + type);
    }
  }

  String unquote(String s) {
    return s.replaceAll("^\"|\"$", "").replaceAll("^'|'$", "");
  }
}
