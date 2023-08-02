package io.github.kayr.ezyquery.parser;

import io.github.kayr.ezyquery.util.Elf;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@lombok.With(AccessLevel.PRIVATE)
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class Schema {

  public interface IPart {
    @ToString
    @lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
    class Text implements IPart {
      private String sql;
    }

    @ToString
    @lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
    class Param implements IPart {
      private String name;
    }
  }

  @Getter private List<IPart> parts;
  private Map<String, IPart.Param> paramParts = new HashMap<>();
  private Map<String, Object> paramValues = new HashMap<>();

  static Schema of(IPart... parts) {
    return new Schema(Arrays.asList(parts));
  }

  public static Schema of(List<IPart> parts) {
    return new Schema(parts);
  }

  public static Schema of(String sql) {
    return NamedParamParser.buildParts(sql);
  }

  static IPart textPart(String fragment) {
    return new IPart.Text(fragment);
  }

  static IPart paramPart(String name) {
    return new IPart.Param(name);
  }

  public Schema(List<IPart> parts) {
    this.parts = Elf.copyList(parts);
    for (IPart part : parts) {
      if (part instanceof IPart.Param) {
        paramParts.put(((IPart.Param) part).name, (IPart.Param) part);
      }
    }
  }

  public Schema setParam(String paramName, Object value) {
    if (!paramParts.containsKey(paramName)) {
      throw new IllegalArgumentException("Param [" + paramName + "] does not exist");
    }
    return withParamValues(Elf.put(paramValues, paramName, value));
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
