/* (C)2022 */
package io.github.kayr.ezyquery.gen;

import static io.github.kayr.ezyquery.gen.CodeElf.*;

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
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;

public class QueryGen {

  public static final ParameterizedTypeName OPTIONAL_SQL_PARTS =
      ParameterizedTypeName.get(Optional.class, SqlParts.class);
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

    FieldSpec fFields = fieldAllFields();

    TypeSpec resultClass = resultClass(fieldList);
    ClassName resultClassName = ClassName.get(packageName, className, resultClass.name);

    List<WithItem> withItemsList = plainSelect.getWithItemsList();
    Pair<MethodSpec, List<SqlParts>> withMethodAndParts = buildWithMethod(withItemsList);

    SqlParts whereParts = toSqlPart(plainSelect.getWhere());

    SqlParts orderByElements =
        toOrderByStatement(plainSelect.getOrderByElements())
            .map(SqlParts::of)
            .orElseGet(SqlParts::empty);

    // the params

    List<SqlParts.IPart.Param> params =
        collectAllParams(
            SqlParts.merge(withMethodAndParts.getTwo()).getParts(),
            fSchemaAndParts.getTwo().getParts(),
            whereParts.getParts(),
            orderByElements.getParts());

    TypeSpec paramsClass = createParamsClass(params);

    List<TypeSpec> nestedQueryClasses = buildNestedQueryClasses(plainSelect);

    TypeSpec criteriaWrapperClass =
        createCriteriaWrapperClass(fieldsForNestedQueries(nestedQueryClasses));

    TypeSpec.Builder finalClassBuilder =
        TypeSpec.classBuilder(className)
            .addModifiers(Modifier.PUBLIC)
            .addJavadoc(sql)
            .addSuperinterface(
                ParameterizedTypeName.get(ClassName.get(EzyQueryWithResult.class), resultClassName))
            .addFields(fConstants)
            .addField(fSchemaAndParts.getOne())
            .addField(fFields)
            .addField(fieldMainSingleton(ClassName.get(packageName, className)))
            .addMethod(methodConstructor())
            .addMethod(methodInit(fConstants, fFields))
            .addMethod(methodQueryMethod())
            .addMethod(methodSchema())
            .addMethod(methodWhereClause(whereParts))
            .addMethod(methodOrderByClause(orderByElements))
            .addAnnotation(
                AnnotationSpec.builder(resolveGeneratedAnnotation())
                    .addMember("value", "$S", QueryGen.class.getName())
                    .addMember("date", "$S", timeStamp())
                    .build())
            .addMethod(methodFields())
            .addMethod(methodResultClass(resultClassName))
            .addType(resultClass);

    if (!params.isEmpty()) {
      finalClassBuilder.addType(paramsClass);
      finalClassBuilder.addField(createStaticField(paramsClass, "PARAMS"));
    }

    if (!nestedQueryClasses.isEmpty()) {
      finalClassBuilder.addType(criteriaWrapperClass);
      finalClassBuilder.addField(createStaticField(criteriaWrapperClass, "CRITERIA"));
    }

    if (!Elf.isEmpty(withItemsList)) {
      finalClassBuilder.addMethod(withMethodAndParts.getOne());
    }

    for (TypeSpec nestedQueryClass : nestedQueryClasses) {
      finalClassBuilder.addType(nestedQueryClass);
    }

    TypeSpec finalClazz = finalClassBuilder.build();

    return JavaFile.builder(packageName, finalClazz).build();
  }

  private List<FieldSpec> fieldsForNestedQueries(List<TypeSpec> nestedQueryClasses) {
    /*
    build the static final NestedQuery objects
    ....
    public static final NestedQuery CUSTOMERS = new NestedQuery();
    ....
     */
    return nestedQueryClasses.stream()
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
  }

  private static TypeSpec createCriteriaWrapperClass(List<FieldSpec> fNestedQueryFields) {
    return TypeSpec.classBuilder("Criteria")
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC)
        .addFields(fNestedQueryFields)
        .build();
  }

  private FieldSpec createStaticField(TypeSpec paramsClass, String fieldName) {

    ClassName typeParams = ClassName.get(packageName, className, paramsClass.name);
    return FieldSpec.builder(
            typeParams, fieldName, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
        .initializer("new $T()", typeParams)
        .build();
  }

  private static TypeSpec createParamsClass(List<SqlParts.IPart.Param> params) {
    /*
    //build a class to contain the params as static final fields
    class Params{
        ....
        public static final NamedParam FIRST_NAME = NamedParam.of("firstName");
        ....
    }
     */
    return TypeSpec.classBuilder("Params")
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
  }

  @SafeVarargs
  private static List<SqlParts.IPart.Param> collectAllParams(List<SqlParts.IPart>... parts) {

    return Elf.combine(parts).stream()
        .filter(isParamPart())
        .map(p -> (SqlParts.IPart.Param) p)
        .collect(Collectors.toList());
  }

  private static MethodSpec methodOrderByClause(SqlParts orderByParts) {
    /* orderBy override method
    @Override
    public Optional<SqlParts> orderByClause() {
        return Optional.of(
            SqlParts.of(
                SqlParts.textPart("c.membership ")));
    }
    */

    MethodSpec.Builder method = publicMethod("orderByClause", OPTIONAL_SQL_PARTS, Override.class);
    if (orderByParts.isEmpty()) {

      return method.addStatement("return Optional.empty()").build();
    }

    return method.addStatement(toReturnSqlPartReturnStatement(orderByParts)).build();
  }

  private static MethodSpec methodWhereClause(SqlParts whereParts) {
    /* where override method
    @Override
    public Optional<SqlParts> whereClause() {
        return Optional.of(
            SqlParts.of(
                SqlParts.textPart("c.membership = "),
                SqlParts.paramPart("membership")));
    }
     */
    MethodSpec.Builder method = publicMethod("whereClause", OPTIONAL_SQL_PARTS, Override.class);
    if (whereParts.isEmpty()) {
      return method.addStatement("return Optional.empty()").build();
    }

    return method.addStatement(toReturnSqlPartReturnStatement(whereParts)).build();
  }

  private SqlParts toSqlPart(Object sql) {
    if (sql == null) return SqlParts.empty();
    return SqlParts.of(sql.toString());
  }

  private static MethodSpec methodSchema() {
    return publicMethod("schema", SqlParts.class, Override.class)
        .addStatement("return this.schema")
        .build();
  }

  private static MethodSpec methodFields() {
    return publicMethod("fields", typeListOfFields(), Override.class)
        .addStatement("return this.fields")
        .build();
  }

  private static MethodSpec methodResultClass(ClassName resultClassName) {

    return publicMethod(
            "resultClass",
            ParameterizedTypeName.get(ClassName.get(Class.class), resultClassName),
            Override.class)
        .addStatement("return $T.class", resultClassName)
        .build();
  }

  private static MethodSpec methodQueryMethod() {

    return publicMethod("query", QueryAndParams.class)
        .addParameter(EzyCriteria.class, "criteria")
        .addStatement("return $T.buildSql(this, criteria)", SqlBuilder.class)
        .build();
  }

  private FieldSpec fieldMainSingleton(ClassName thisClassName) {
    return FieldSpec.builder(
            thisClassName,
            StringCaseUtil.toScreamingSnakeCase(className),
            Modifier.PUBLIC,
            Modifier.STATIC,
            Modifier.FINAL)
        .initializer("new $T()", thisClassName)
        .build();
  }

  private Pair<MethodSpec, List<SqlParts>> buildWithMethod(List<WithItem> withItems) {

    if (Elf.isEmpty(withItems)) {
      return Pair.of(null, Collections.emptyList());
    }

    MethodSpec.Builder mWith =
        publicMethod("withClauses", ParameterizedTypeName.get(List.class, SqlParts.class));

    List<String> withClauseNames = new ArrayList<>();
    List<SqlParts> withClauseParts = new ArrayList<>();

    for (WithItem withItem : withItems) {
      String name = StringCaseUtil.toCamelCase(unquote(withItem.getAlias().getName()));
      withClauseNames.add(name);

      String withClause = withItem.toString();
      SqlParts sqlParts = SqlParts.of(withClause);
      withClauseParts.add(sqlParts);

      CodeBlock.Builder withCodeBlock = buildSqlParts(sqlParts);

      mWith.addCode("SqlParts $L = ", name).addCode(withCodeBlock.build()).addCode(";\n");
    }

    mWith.addStatement(
        "$T<$T> withClauses = new $T<>()", List.class, SqlParts.class, ArrayList.class);
    for (String withClauseName : withClauseNames) {
      mWith.addStatement("withClauses.add($L)", withClauseName);
    }

    mWith.addStatement("return withClauses");

    return Pair.of(mWith.build(), withClauseParts);
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
          publicMethod("getName", NamedCriteriaParam.class, Override.class)
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

  private CodeBlock toStringMethodBody(List<EzyQueryFieldSpec> fieldList) {
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
    return toStringMethodBody.build();
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
          publicMethod(toGetterName(f.getAlias()), f.getDataType())
              .addStatement("return $L", f.getAlias())
              .build());
    }

    // to string methods
    MethodSpec toStringMethod =
        publicMethod("toString", String.class, Override.class)
            .addCode(toStringMethodBody(fieldList))
            .build();

    resultClassBuilder.addMethod(toStringMethod);

    return resultClassBuilder.build();
  }

  private static String toGetterName(String fieldName) {
    return "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
  }

  /** Init() method */
  private MethodSpec methodInit(List<FieldSpec> fConstants, FieldSpec fFields) {
    MethodSpec.Builder initBuilder = method(modifiers(Modifier.PRIVATE), "init", TypeName.VOID);
    for (FieldSpec field : fConstants) {
      initBuilder.addStatement("$N.add($N)", fFields, field);
    }
    return initBuilder.build();
  }

  /** Constructor() method */
  private MethodSpec methodConstructor() {
    return method(modifiers(Modifier.PRIVATE), "<init>", TypeName.VOID)
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

    Pair<String, String> pair = parseNameAndType(selectItem.getAlias());

    String aliasName = pair.getOne();
    String typeName = pair.getTwo();

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

  private Pair<String, String> parseNameAndType(Alias sqlAlias) {
    String alias =
        Objects.requireNonNull(sqlAlias, "Alias if required for filed [" + sqlAlias + "]")
            .getName();
    alias = unquote(alias);

    String[] parts = alias.contains("_") ? alias.split("_") : new String[] {alias, "object"};
      return Pair.of(parts[0], parts[1]);
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
