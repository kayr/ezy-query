package io.github.kayr.ezyquery.parser;

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

    @lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
    class Text implements IPart {
      private String sql;

      @Override
      public String toString() {
        return sql;
      }
    }

    @lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
    class Param implements IPart {
      private String name;

      public String toString() {
        return ":" + name;
      }
    }
  }

  private List<IPart> parts;
  private Map<String, IPart.Param> paramParts = new HashMap<>();
  private Map<String, Object> paramValues = new HashMap<>();

  static SqlParts of(IPart... parts) {
    return new SqlParts(Arrays.asList(parts));
  }

  public static SqlParts of(List<IPart> parts) {
    return new SqlParts(parts);
  }

  public static SqlParts of(String sql) {
    return NamedParamParser.buildParts(sql);
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
      }
    }
  }

  public SqlParts setParam(String paramName, Object value) {
    if (!paramParts.containsKey(paramName)) {
      throw new IllegalArgumentException("Param [" + paramName + "] does not exist");
    }
    return withParamValues(Elf.put(paramValues, paramName, value));
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
