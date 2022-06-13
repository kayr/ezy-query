package io.github.kayr.ezyquery.ast;

import net.sf.jsqlparser.expression.Expression;

@lombok.Setter
@lombok.AllArgsConstructor
public class VariableExpr implements EzyExpr {

    private String variable;

    public String toString() {
        return String.format("%s", variable);
    }

}
