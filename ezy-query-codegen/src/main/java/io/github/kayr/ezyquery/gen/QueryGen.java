/* (C)2022 */
package io.github.kayr.ezyquery.gen;

import com.squareup.javapoet.*;
import io.github.kayr.ezyquery.EzyQueryWithResult;
import io.github.kayr.ezyquery.api.*;
import io.github.kayr.ezyquery.gen.walkers.DynamicQueriesFinder;
import io.github.kayr.ezyquery.gen.walkers.WalkContext;
import io.github.kayr.ezyquery.parser.QueryAndParams;
import io.github.kayr.ezyquery.parser.SqlParts;
import io.github.kayr.ezyquery.util.Elf;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.lang.model.element.Modifier;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;

public class QueryGen {

  private final String sql;
  private final String className;
  private final String packageName;
  private final Properties config;

  public QueryGen(String packageName, String className, String sql, Properties config) {
    this.sql = sql;
    this.className = className;
    this.packageName = packageName;
    this.config = config;
  }

  public Path writeTo(String path) {
    return writeTo(Paths.get(path));
  }

  @lombok.SneakyThrows
  public Path writeTo(Path path) {
    JavaFile javaFile = javaCode();
    return javaFile.writeToPath(path);
  }

  public JavaFile javaCode() throws JSQLParserException {
    Statement statement = CCJSqlParserUtil.parse(sql);

    if (!(statement instanceof Select)) {
      throw new IllegalArgumentException("Only SELECT statements are supported");
    }

    Select select = (Select) statement;

    if (!(select instanceof PlainSelect)) {
      throw new IllegalArgumentException("Only SELECT statements are supported");
    }

    PlainSelect plainSelect = select.getPlainSelect();
    List<EzyQueryFieldSpec> fieldList = extractFields(plainSelect);

    return buildCode(fieldList, plainSelect);
  }

  private JavaFile buildCode(List<EzyQueryFieldSpec> fieldList, PlainSelect plainSelect) {

    List<FieldSpec> fConstants = fieldConstants(fieldList);

    Pair<FieldSpec, SqlParts> fSchemaAndParts = fieldSchema(plainSelect);
    FieldSpec fSchema = fSchemaAndParts.getOne();

    ClassName thisClassName = ClassName.get(packageName, className);
    FieldSpec fSingleton =
        FieldSpec.builder(
                thisClassName,
                StringCaseUtil.toScreamingSnakeCase(className),
                Modifier.PUBLIC,
                Modifier.STATIC,
                Modifier.FINAL)
            .initializer("new $T()", thisClassName)
            .build();

    FieldSpec fFields = fieldAllFields();

    MethodSpec mConstructor = methodConstructor();

    MethodSpec mInit = methodInit(fConstants, fFields);

    TypeSpec resultClass = resultClass(fieldList);
    ClassName resultClassName = ClassName.get(packageName, className, resultClass.name);

    /* main query method
    public QueryAndParams query(EzyCriteria criteria) {
        return SqlBuilder.buildSql(this, criteria);
    }*/
    MethodSpec queryMethod =
        MethodSpec.methodBuilder("query")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(EzyCriteria.class, "criteria")
            .addStatement("return $T.buildSql(this, criteria)", SqlBuilder.class)
            .returns(QueryAndParams.class)
            .build();

    /*result class override method
    @Override
    public Class<Result> resultClass() {
        return Result.class;
    }
     */
    MethodSpec resultClassMethod =
        MethodSpec.methodBuilder("resultClass")
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Override.class)
            .returns(ParameterizedTypeName.get(ClassName.get(Class.class), resultClassName))
            .addStatement("return $T.class", resultClassName)
            .build();

    /* fields override method
    @Override
    public List<Field<?>> fields() {
        return this.fields;
    }
    */
    MethodSpec fieldsMethod =
        MethodSpec.methodBuilder("fields")
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Override.class)
            .returns(typeListOfFields())
            .addStatement("return this.fields")
            .build();

    /* schema override method
    @Override
    public SqlParts schema() {
        return this.schema;
    }
     */
    MethodSpec schemaMethod =
        MethodSpec.methodBuilder("schema")
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Override.class)
            .returns(SqlParts.class)
            .addStatement("return this.schema")
            .build();

    Optional<SqlParts> whereParts =
        Optional.ofNullable(plainSelect.getWhere()).map(Object::toString).map(SqlParts::of);
    CodeBlock whereStatement = toReturnSqlPartReturnStatement(whereParts.orElse(null));
    /* where override method
    @Override
    public Optional<SqlParts> whereClause() {
        return Optional.of(
            SqlParts.of(
                SqlParts.textPart("c.membership = "),
                SqlParts.paramPart("membership")
            ));
    }
     */
    MethodSpec whereMethod =
        MethodSpec.methodBuilder("whereClause")
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Override.class)
            .returns(ParameterizedTypeName.get(Optional.class, SqlParts.class))
            .addStatement(whereStatement)
            .build();

    /* orderBy override method
    @Override
    public Optional<SqlParts> orderByClause() {
        return Optional.of(
            SqlParts.of(
                SqlParts.textPart("c.membership ")
            ));
    }
    */
    Optional<SqlParts> orderByElements =
        toOrderByStatement(plainSelect.getOrderByElements()).map(SqlParts::of);
    CodeBlock orderByStatement = toReturnSqlPartReturnStatement(orderByElements.orElse(null));
    MethodSpec orderByMethod =
        MethodSpec.methodBuilder("orderByClause")
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Override.class)
            .returns(ParameterizedTypeName.get(Optional.class, SqlParts.class))
            .addStatement(orderByStatement)
            .build();

    // the params

    List<SqlParts.IPart.Param> params =
        Elf.combine(
                fSchemaAndParts.getTwo().getParts(),
                whereParts.map(SqlParts::getParts).orElse(Collections.emptyList()),
                orderByElements.map(SqlParts::getParts).orElse(Collections.emptyList()))
            .stream()
            .filter(isParamPart())
            .map(p -> (SqlParts.IPart.Param) p)
            .collect(Collectors.toList());

    /*
    //build a class to contain the params as static final fields
    class Params{
        ....
        public static final NamedParam FIRST_NAME = NamedParam.of("firstName");
        ....
    }
     */
    TypeSpec paramsClass =
        TypeSpec.classBuilder("Params")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addFields(
                params.stream()
                    .map(SqlParts.IPart.Param::asString)
                    .distinct()
                    .map(
                        name ->
                            FieldSpec.builder(
                                    NamedParam.class,
                                    StringCaseUtil.toScreamingSnakeCase(name),
                                    Modifier.PUBLIC,
                                    Modifier.FINAL)
                                .initializer("$T.of($S)", NamedParam.class, name)
                                .build())
                    .collect(Collectors.toList()))
            .build();

    /*
    Add PARAMS static field
     */
    ClassName typeParams = ClassName.get(packageName, className, paramsClass.name);
    FieldSpec fParams =
        FieldSpec.builder(typeParams, "PARAMS", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            .initializer("new $T()", typeParams)
            .build();

    /*
     build inner classes to hold fields of nested queries
    */
    List<TypeSpec> nestedQueryClasses = buildNestedQueryClasses(plainSelect);

    /*
    build the static final NestedQuery objects
    ....
    public static final NestedQuery CUSTOMERS = new NestedQuery();
    ....
     */
    List<FieldSpec> fNestedQueryFields =
        nestedQueryClasses.stream()
            .map(
                nestedQueryClass -> {
                  ClassName type = ClassName.get(packageName, className, nestedQueryClass.name);
                  return FieldSpec.builder(
                          type,
                          StringCaseUtil.toScreamingSnakeCase(nestedQueryClass.name),
                          Modifier.PUBLIC,
                          Modifier.FINAL)
                      .initializer("new $T()", type)
                      .build();
                })
            .collect(Collectors.toList());

    /*
    Wrap the nested query fields in an inner class. We wrap them to avoid name clashes
    public static final class Criteria{
        public static final NestedQuery CUSTOMERS = new NestedQuery();
        ....
    }
     */
    TypeSpec nestedQueryFieldsClass =
        TypeSpec.classBuilder("Criteria")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC)
            .addFields(fNestedQueryFields)
            .build();

    /*
    Create a field for the Criteria class
    public static final Criteria CRITERIA = new Criteria();
     */
    ClassName typeCriteria = ClassName.get(packageName, className, nestedQueryFieldsClass.name);
    FieldSpec fCriteria =
        FieldSpec.builder(
                typeCriteria, "CRITERIA", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            .initializer("new $T()", typeCriteria)
            .build();

    ClassName generatedAnnotation = resolveGeneratedAnnotation();

    TypeSpec.Builder finalClassBuilder =
        TypeSpec.classBuilder(className)
            .addModifiers(Modifier.PUBLIC)
            .addJavadoc(sql)
            .addSuperinterface(
                ParameterizedTypeName.get(ClassName.get(EzyQueryWithResult.class), resultClassName))
            .addFields(fConstants)
            .addField(fSchema)
            .addField(fFields)
            .addField(fSingleton)
            .addMethod(mConstructor)
            .addMethod(mInit)
            .addMethod(queryMethod)
            .addMethod(schemaMethod)
            .addMethod(whereMethod)
            .addMethod(orderByMethod)
            .addAnnotation(
                AnnotationSpec.builder(generatedAnnotation)
                    .addMember("value", "$S", QueryGen.class.getName())
                    .addMember("date", "$S", timeStamp())
                    .build())
            .addMethod(fieldsMethod)
            .addMethod(resultClassMethod)
            .addType(resultClass);

    if (!params.isEmpty()) {
      finalClassBuilder.addType(paramsClass);
      finalClassBuilder.addField(fParams);
    }

    if (!nestedQueryClasses.isEmpty()) {
      finalClassBuilder.addType(nestedQueryFieldsClass);
      finalClassBuilder.addField(fCriteria);
    }

    nestedQueryClasses.forEach(finalClassBuilder::addType);

    TypeSpec finalClazz = finalClassBuilder.build();

    return JavaFile.builder(packageName, finalClazz).build();
  }

  private List<TypeSpec> buildNestedQueryClasses(PlainSelect plainSelect) {
    Map<String, WalkContext.SelectExpr> nestedQueries = DynamicQueriesFinder.lookup(plainSelect);

    if (nestedQueries.isEmpty()) {
      return Collections.emptyList();
    }

    List<TypeSpec> nestedQueryClasses = new ArrayList<>();
    for (Map.Entry<String, WalkContext.SelectExpr> entry : nestedQueries.entrySet()) {
      String paramName = entry.getKey();
      String className = entry.getKey().substring(WalkContext.EZY_MARKER.length());

      WalkContext.SelectExpr selectExpr = entry.getValue();
      List<EzyQueryFieldSpec> fields = extractFields(selectExpr.getSelect().getPlainSelect());

      List<FieldSpec> classFields =
          fields.stream()
              .map(f -> createField(f, Modifier.PUBLIC, Modifier.FINAL))
              .collect(Collectors.toList());

      MethodSpec mConstructor = methodConstructor();

      FieldSpec fFields = fieldAllFields();

      MethodSpec mInit = methodInit(classFields, fFields);

      /*
       @Override
      public NamedCriteriaParam getName() {
        return NamedCriteriaParam.of("_ezy_customerSummary", fields);
      }
       */
      MethodSpec mGetName =
          MethodSpec.methodBuilder("getName")
              .addModifiers(Modifier.PUBLIC)
              .addAnnotation(Override.class)
              .returns(NamedCriteriaParam.class)
              .addStatement("return $T.of($S, fields)", NamedCriteriaParam.class, paramName)
              .build();

      TypeSpec ty =
          TypeSpec.classBuilder(StringCaseUtil.toPascalCase(className))
              .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
              .addSuperinterface(CriteriaHolder.class)
              .addFields(classFields)
              .addField(fFields)
              .addMethod(mConstructor)
              .addMethod(mInit)
              .addMethod(mGetName)
              .build();

      nestedQueryClasses.add(ty);
    }

    return nestedQueryClasses;
  }

  private static Predicate<SqlParts.IPart> isParamPart() {
    return p -> p instanceof SqlParts.IPart.Param;
  }

  private static CodeBlock toReturnSqlPartReturnStatement(SqlParts orderByElements) {
    return Optional.ofNullable(orderByElements)
        .map(s -> buildSqlParts(s).build())
        .map(c -> CodeBlock.builder().add("return Optional.of(\n$>").add(c).add(")$<").build())
        .orElse(CodeBlock.of("return Optional.empty()"));
  }

  private static Optional<String> toOrderByStatement(List<OrderByElement> orderByElements1) {
    if (Elf.isEmpty(orderByElements1)) {
      return Optional.empty();
    }
    StringBuilder sb = new StringBuilder();
    for (OrderByElement orderByElement : orderByElements1) {
      sb.append(orderByElement).append(", ");
    }
    sb.setLength(sb.length() - 2);

    return Optional.of(sb.toString());
  }

  protected String timeStamp() {
    return LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
  }

  private static ClassName resolveGeneratedAnnotation() {
    // if Generated annotation is available, add it
    ClassName generatedAnnotation;
    if (Elf.classExists("javax.annotation.Generated")) {
      generatedAnnotation = ClassName.get("javax.annotation", "Generated");
    } else {
      generatedAnnotation = ClassName.get("javax.annotation.processing", "Generated");
    }
    return generatedAnnotation;
  }

  private CodeBlock.Builder toStringMethodBody(List<EzyQueryFieldSpec> fieldList) {
    CodeBlock.Builder toStringMethodBody =
        CodeBlock.builder().add("return \"$L.Result{\"\n", className);

    boolean isFirst = true;
    for (EzyQueryFieldSpec f : fieldList) {
      if (isFirst) {
        toStringMethodBody.add("+ $S + $L\n", f.getAlias() + " = ", f.getAlias());
      } else {
        toStringMethodBody.add("+ $S + $L\n", ", " + f.getAlias() + " = ", f.getAlias());
      }
      isFirst = false;
    }

    toStringMethodBody.addStatement(" + $S", "}");
    return toStringMethodBody;
  }

  private TypeSpec resultClass(List<EzyQueryFieldSpec> fieldList) {
    TypeSpec.Builder resultClassBuilder =
        TypeSpec.classBuilder("Result").addModifiers(Modifier.PUBLIC, Modifier.STATIC);
    for (EzyQueryFieldSpec f : fieldList) {
      resultClassBuilder.addField(
          FieldSpec.builder(f.getDataType(), f.getAlias(), Modifier.PRIVATE).build());
    }

    // add getters
    for (EzyQueryFieldSpec f : fieldList) {
      resultClassBuilder.addMethod(
          MethodSpec.methodBuilder(toGetterName(f.getAlias()))
              .addModifiers(Modifier.PUBLIC)
              .returns(f.getDataType())
              .addStatement("return $L", f.getAlias())
              .build());
    }

    // to string methods
    CodeBlock.Builder toStringMethodBody = toStringMethodBody(fieldList);
    MethodSpec toStringMethod =
        MethodSpec.methodBuilder("toString")
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Override.class)
            .returns(String.class)
            .addCode(toStringMethodBody.build())
            .build();

    resultClassBuilder.addMethod(toStringMethod);

    return resultClassBuilder.build();
  }

  private static String toGetterName(String fieldName) {
    return "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
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
        .addModifiers(Modifier.PRIVATE)
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
  private Pair<FieldSpec, SqlParts> fieldSchema(PlainSelect plainSelect) {
    List<Join> joins = Optional.ofNullable(plainSelect.getJoins()).orElse(Collections.emptyList());

    StringBuilder sb = new StringBuilder();
    String fromTable =
        joins.isEmpty()
            ? plainSelect.getFromItem().toString()
            : plainSelect.getFromItem().toString() + "\n";

    sb.append(fromTable);

    for (int i = 0; i < joins.size(); i++) {
      Join j = joins.get(i);

      String joinString = i < joins.size() - 1 ? j.toString() + "\n" : j.toString();

      sb.append(joinString);
    }

    String finalFromClause = sb.toString();

    SqlParts sqlParts = SqlParts.of(finalFromClause);

    CodeBlock.Builder schema = buildSqlParts(sqlParts);

    FieldSpec schemaField =
        FieldSpec.builder(SqlParts.class, "schema", Modifier.PRIVATE, Modifier.FINAL)
            .initializer(schema.build())
            .build();
    return Pair.of(schemaField, sqlParts);
  }

  private static CodeBlock.Builder buildSqlParts(SqlParts sqlParts) {
    CodeBlock.Builder schemaBuilder = CodeBlock.builder();
    schemaBuilder.add("$T.of(", SqlParts.class);
    schemaBuilder.add("\n$>$>");
    List<SqlParts.IPart> parts = sqlParts.getParts();
    for (int i = 0, partsSize = parts.size(); i < partsSize; i++) {
      SqlParts.IPart sqlPart = parts.get(i);
      if (sqlPart instanceof SqlParts.IPart.Text) {
        schemaBuilder.add("$T.textPart($S)", SqlParts.class, sqlPart.asString());
      } else {
        schemaBuilder.add("$T.paramPart($S)", SqlParts.class, sqlPart.asString());
      }

      if (i < partsSize - 1) {
        schemaBuilder.add(",\n");
      }
    }

    schemaBuilder.add("\n$<$<)");
    return schemaBuilder;
  }

  private List<FieldSpec> fieldConstants(List<EzyQueryFieldSpec> fieldList) {
    // Constant fields
    return fieldList.stream()
        .map(f -> createField(f, Modifier.PUBLIC, Modifier.FINAL))
        .collect(Collectors.toList());
  }

  private FieldSpec createField(EzyQueryFieldSpec f, Modifier... modifiers) {
    return FieldSpec.builder(
            ParameterizedTypeName.get(ClassName.get(Field.class), f.getDataType()),
            constantName(f.getAlias()),
            modifiers)
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

  String constantName(String name) {
    return StringCaseUtil.toScreamingSnakeCase(name);
  }

  private List<EzyQueryFieldSpec> extractFields(PlainSelect plainSelect) {

    return plainSelect.getSelectItems().stream().map(this::toField).collect(Collectors.toList());
  }

  private EzyQueryFieldSpec toField(SelectItem<?> selectItem) {
    String alias =
        Objects.requireNonNull(
                selectItem.getAlias(), "Alias if required for filed [" + selectItem + "]")
            .getName();
    alias = unquote(alias);

    String[] parts = alias.contains("_") ? alias.split("_") : new String[] {alias, "object"};

    String aliasName = parts[0];
    String typeName = parts[1];

    TypeName type = resolveType(typeName);

    Expression expression = selectItem.getExpression();
    String sqlField = expression.toString();

    if (expression instanceof Column) {
      return EzyQueryFieldSpec.of(sqlField, aliasName, type, Field.ExpressionType.COLUMN);
    }

    if (expression instanceof BinaryExpression) {
      return EzyQueryFieldSpec.of(sqlField, aliasName, type, Field.ExpressionType.BINARY);
    }

    return EzyQueryFieldSpec.of(sqlField, aliasName, type, Field.ExpressionType.OTHER);
  }

  TypeName resolveType(String type) {
    String javaType = config.getProperty("type." + type);
    if (javaType != null) {
      return ClassName.bestGuess(javaType);
    }
    Class<?> aClass = TYPE_MAP.get(type);
    if (aClass == null) throw new IllegalArgumentException("Unsupported type: " + type);
    return ClassName.get(aClass);
  }

  private static final Map<String, Class<?>> TYPE_MAP = new HashMap<>();

  static {
    TYPE_MAP.put("int", Integer.class);
    TYPE_MAP.put("long", Long.class);
    TYPE_MAP.put("float", Float.class);
    TYPE_MAP.put("double", Double.class);
    TYPE_MAP.put("boolean", Boolean.class);
    TYPE_MAP.put("string", String.class);
    TYPE_MAP.put("date", Date.class);
    TYPE_MAP.put("time", java.sql.Timestamp.class);
    TYPE_MAP.put("decimal", java.math.BigDecimal.class);
    TYPE_MAP.put("bigint", java.math.BigInteger.class);
    TYPE_MAP.put("byte", Byte.class);
    TYPE_MAP.put("object", Object.class);
  }

  @lombok.Getter
  @lombok.AllArgsConstructor(staticName = "of")
  static class EzyQueryFieldSpec {
    private String sqlField;
    private String alias;
    private TypeName dataType;
    private Field.ExpressionType expressionType;
  }

  String unquote(String s) {
    return s.replaceAll("^\"|\"$", "").replaceAll("^'|'$", "");
  }
}
