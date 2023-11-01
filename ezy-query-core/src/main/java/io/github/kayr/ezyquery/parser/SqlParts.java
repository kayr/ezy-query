package io.github.kayr.ezyquery.parser;

import io.github.kayr.ezyquery.EzyQuery;
import io.github.kayr.ezyquery.api.EzyCriteria;
import io.github.kayr.ezyquery.api.NamedParamValue;
import io.github.kayr.ezyquery.util.Elf;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@lombok.With(AccessLevel.PRIVATE)
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class SqlParts {

  public interface IPart {

    String asString();

    @lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
    class Text implements IPart {
      private String sql;

      @Override
      public String toString() {
        return sql;
      }

      @Override
      public String asString() {
        return sql;
      }
    }

    @lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
    class Param implements IPart {
      private String name;

      public String toString() {
        return ":" + name;
      }

      @Override
      public String asString() {
        return name;
      }
    }

    class SubQuery implements IPart {
      private String name;
      private EzyQuery query;

      public String toString() {
        throw new UnsupportedOperationException(
            "NotImplemented:SubQuery cannot be converted to String");
      }

      @Override
      public String asString() {
        throw new UnsupportedOperationException(
            "NotImplemented: make SubQuery cannot be converted to String");
      }
    }
  }

  private List<IPart> parts;
  private Map<String, IPart.Param> paramParts = new HashMap<>();
  private Map<String, IPart.SubQuery> subQueries = new HashMap<>();
  private Map<String, Object> paramValues = new HashMap<>();
  private Map<String, EzyCriteria> criteria = new HashMap<>();

  public static SqlParts of(IPart... parts) {
    return new SqlParts(Arrays.asList(parts));
  }

  public static SqlParts of(List<IPart> parts) {
    return new SqlParts(parts);
  }

  public static SqlParts of(String sql) {
    return NamedParamParser.buildParts(sql);
  }

  public static SqlParts empty() {
    return new SqlParts(Collections.emptyList());
  }

  public static IPart textPart(String fragment) {
    return new IPart.Text(fragment);
  }

  public static IPart paramPart(String name) {
    return new IPart.Param(name);
  }

  public SqlParts(List<IPart> parts) {
    this.parts = Elf.copyList(parts);

    for (IPart part : parts) {
      if (part instanceof IPart.Param) {
        paramParts.put(((IPart.Param) part).name, (IPart.Param) part);
      } else if (part instanceof IPart.SubQuery) {
        subQueries.put(((IPart.SubQuery) part).name, (IPart.SubQuery) part);
      }
    }
  }

  public SqlParts setParam(String paramName, Object value) {
    if (!paramParts.containsKey(paramName)) {
      throw new IllegalArgumentException("Param [" + paramName + "] does not exist");
    }
    return withParamValues(Elf.put(paramValues, paramName, value));
  }

  public SqlParts setCriteria(String subQueryName, EzyCriteria criteria) {
    if (!subQueries.containsKey(subQueryName)) {
      throw new IllegalArgumentException("SubQuery [" + subQueryName + "] does not exist");
    }
    return withCriteria(Elf.put(this.criteria, subQueryName, criteria));
  }

  public SqlParts withParams(List<NamedParamValue> values) {
    Map<String, Object> newValues = new HashMap<>(paramValues);
    for (NamedParamValue value : values) {
      newValues.put(value.getParam().getName(), value.getValue());
    }
    return withParamValues(newValues);
  }

  public QueryAndParams getQuery(List<NamedParamValue> values) {
    return withParams(values).getQuery();
  }

  public QueryAndParams getQuery() {
    QueryAndParams qp = QueryAndParams.of("");
    for (IPart part : parts) {
      if (part instanceof IPart.Text) {
        qp = qp.append(((IPart.Text) part).sql);
      } else if (part instanceof IPart.Param) {
        qp = toQuery((IPart.Param) part, qp);
      }
    }
    return qp;
  }

  public List<IPart> getParts() {
    return Elf.copyList(parts);
  }

  String getRawSql() {
    StringBuilder sb = new StringBuilder();
    for (IPart part : parts) {
      if (part instanceof IPart.Text) {
        sb.append(((IPart.Text) part).sql);
      } else if (part instanceof IPart.Param) {
        sb.append(":").append(((IPart.Param) part).name);
      } else if (part instanceof IPart.SubQuery) {
        sb.append("SubQuery");
      }
    }
    return sb.toString();
  }

  private QueryAndParams toQuery(IPart.Param part, QueryAndParams leftQuery) {
    boolean containsKey = paramValues.containsKey(part.name);

    if (!containsKey) throw new IllegalStateException("Param [" + part.name + "] is not set");

    List<Object> actualValue = convertToValueParam(paramValues.get(part.name));
    String sql = String.join(",", Collections.nCopies(actualValue.size(), "?"));

    return leftQuery.append(QueryAndParams.of(sql, actualValue));
  }

  private String buildSql(IPart.SubQuery subQuery) {
    throw new UnsupportedOperationException("NotImplemented: buildSql");
    //    SqlBuilder.buildSql(subQuery.query, criteria);

  }

  static List<Object> convertToValueParam(Object value) {

    if (value == null) return Collections.singletonList(null);

    if (value instanceof Collection) {
      //noinspection unchecked
      return Elf.copyList((Collection<Object>) value);
    }

    if (value.getClass().isArray()) {
      return Arrays.asList((Object[]) value);
    }

    if (value instanceof Iterable) {
      return Elf.toList(((Iterable<?>) value).iterator());
    }

    if (value instanceof Iterator) {
      return Elf.toList((Iterator<?>) value);
    }

    if (value instanceof Enumeration) {
      return Elf.toList((Enumeration<?>) value);
    }

    if (value instanceof Stream) {
      return ((Stream<?>) value).collect(Collectors.toList());
    }

    return Collections.singletonList(value);
  }
}
