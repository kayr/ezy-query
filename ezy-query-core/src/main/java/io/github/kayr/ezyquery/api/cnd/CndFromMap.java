package io.github.kayr.ezyquery.api.cnd;

import io.github.kayr.ezyquery.util.Elf;
import io.github.kayr.ezyquery.util.StringElf;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class CndFromMap {

    interface OpHandler {

        ICond create(String name, String operator, List<?> operands);
    }

    private final static Map<String, OpHandler> DEFAULT_HANDLERS = new HashMap<>();


    private CndFromMap() {
    }


    public static ICond create(Map<String, List<?>> map) {
        return create().from(map);
    }

    static CndFromMap create() {
        return new CndFromMap();
    }


    private static ICond createCond(
      String operator, List<?> operands, Function<Object, ICond> fnCreateCnd) {
        return operands.stream()
                 .map(fnCreateCnd)
                 .reduce(ICond::and)
                 .orElseThrow(
                   () -> new IllegalArgumentException("No operands found for operator: " + operator));
    }

    public ICond from(Map<String, List<?>> map) {

        return map.entrySet().stream()
                 .map(
                   e -> {
                       Map.Entry<String, String> key = splitKey(e.getKey());
                       String operator = key.getValue();
                       String fieldName = key.getKey();

                       OpHandler opHandler = DEFAULT_HANDLERS.get(operator);

                       if (opHandler == null) {
                           throw new IllegalArgumentException("Unknown operator: " + operator);
                       }

                       return opHandler.create(fieldName, operator, e.getValue());
                   })
                 .reduce(ICond::and)
                 .orElseThrow(() -> new IllegalArgumentException("No conditions found"));
    }

    private static Map.Entry<String, String> splitKey(String name) {

        String[] split = name.split("\\.");
        if (split.length == 1) {
            return new AbstractMap.SimpleEntry<>("#" + name, "eq");
        }

        // field name is second last
        String fieldName = split[split.length - 2];
        String operator = split[split.length - 1];

        return new AbstractMap.SimpleEntry<>("#" + fieldName, operator);
    }


    static {
        handle("eq", eqHandler());
        handle("neq", neqHandler());
        handle("like", likeHandler());
        handle("notlike", notLikeHandler());
        handle("gt", gtHandler());
        handle("gte", gteHandler());
        handle("lt", ltHandler());
        handle("lte", lteHandler());
        handle("in", inHandler());
        handle("notin", notInHandler());
        handle("between", betweenHandler());
        handle("between", betweenHandler());
        handle("notbetween", notBetweenHandler());
        handle("isnull", isNullHandler());
        handle("isnotnull", isNotNullHandler());
    }


    private static OpHandler notInHandler() {
        return (name, operator, operands) ->
                 createCond(
                   operator,
                   operands,
                   o ->
                     o instanceof String
                       ? Cnd.notIn(name, StringElf.splitByComma(o.toString()))
                       : Cnd.notIn(name, o));
    }

    private static OpHandler inHandler() {
        return (name, operator, operands) ->
                 createCond(
                   operator,
                   operands,
                   o ->
                     o instanceof String
                       ? Cnd.in(name, StringElf.splitByComma(o.toString()))
                       : Cnd.in(name, o));
    }


    private static void handle(String eq, OpHandler value) {
        DEFAULT_HANDLERS.put(eq, value);
    }

    private static OpHandler lteHandler() {
        return (name, operator, operands) -> createCond(operator, operands, o -> Cnd.lte(name, o));
    }

    private static OpHandler ltHandler() {
        return (name, operator, operands) -> createCond(operator, operands, o -> Cnd.lt(name, o));
    }

    private static OpHandler gteHandler() {
        return (name, operator, operands) -> createCond(operator, operands, o -> Cnd.gte(name, o));
    }

    private static OpHandler gtHandler() {
        return (name, operator, operands) -> createCond(operator, operands, o -> Cnd.gt(name, o));
    }

    private static OpHandler notLikeHandler() {
        return (name, operator, operands) -> createCond(operator, operands, o -> Cnd.notLike(name, o));
    }

    private static OpHandler likeHandler() {
        return (name, operator, operands) -> createCond(operator, operands, o -> Cnd.like(name, o));
    }

    private static OpHandler neqHandler() {
        return (name, operator, operands) -> createCond(operator, operands, o -> Cnd.neq(name, o));
    }

    private static OpHandler eqHandler() {
        return (name, operator, operands) -> createCond(operator, operands, o -> Cnd.eq(name, o));
    }

    private static OpHandler isNullHandler() {
        return (name, operator, operands) -> {
            Elf.assertTrue(Elf.isEmpty(operands), "IsNull operator requires 0 operands");
            return Cnd.isNull(name);
        };
    }

    private static OpHandler betweenHandler() {
        return (name, operator, operands) -> {
            Elf.assertTrue(operands.size() == 2, "Between operator requires 2 operands");
            return Cnd.between(name, operands.get(0), operands.get(1));
        };
    }

    private static OpHandler notBetweenHandler() {
        return (name, operator, operands) -> {
            Elf.assertTrue(operands.size() == 2, "Not Between operator requires 2 operands");
            return Cnd.notBetween(name, operands.get(0), operands.get(1));
        };
    }

    private static OpHandler isNotNullHandler() {

        return (name, operator, operands) -> {
            Elf.assertTrue(Elf.isEmpty(operands), "IsNotNull operator requires 0 operands");
            return Cnd.isNotNull(name);
        };
    }

}
