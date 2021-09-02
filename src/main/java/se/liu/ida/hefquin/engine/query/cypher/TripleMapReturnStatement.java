package se.liu.ida.hefquin.engine.query.cypher;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class TripleMapReturnStatement implements ReturnStatement{

    private final CypherVar source;
    private final CypherVar edge;
    private final CypherVar target;
    private final String alias;

    public TripleMapReturnStatement(final CypherVar source, final CypherVar edge, final CypherVar target,
                                    final String alias) {
        this.source = source;
        this.edge = edge;
        this.target = target;
        this.alias=alias;
    }

    @Override
    public Set<CypherVar> getVars() {
        Set<CypherVar> vars = new HashSet<>();
        vars.add(source);
        vars.add(edge);
        vars.add(target);
        return vars;
    }

    @Override
    public String getAlias() {
        return alias;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append("{source: nm(")
                .append(source.getName())
                .append("), edge: elm(")
                .append(edge.getName())
                .append("), target: nm(")
                .append(target.getName())
                .append(")} AS ")
                .append(alias);
        return res.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TripleMapReturnStatement)) return false;
        TripleMapReturnStatement that = (TripleMapReturnStatement) o;
        return source.equals(that.source) && edge.equals(that.edge) && target.equals(that.target) && alias.equals(that.alias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, edge, target, alias);
    }
}
