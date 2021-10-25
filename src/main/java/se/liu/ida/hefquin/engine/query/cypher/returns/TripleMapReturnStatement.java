package se.liu.ida.hefquin.engine.query.cypher.returns;

import se.liu.ida.hefquin.engine.query.cypher.CypherVar;
import se.liu.ida.hefquin.engine.query.cypher.ReturnStatement;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a statement that returns a map that represents an RDF triple, with an optional alias
 * For example, RETURN {source: n1, edge: e, target: n2} AS t
 */
public class TripleMapReturnStatement implements ReturnStatement {

    private final CypherVar source;
    private final CypherVar edge;
    private final CypherVar target;
    private final CypherVar alias;

    public TripleMapReturnStatement(final CypherVar source, final CypherVar edge, final CypherVar target,
                                    final CypherVar alias) {
        assert source != null && edge != null && target != null;
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
    public CypherVar getAlias() {
        return alias;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append("{source: ")
                .append(source.getName())
                .append(", edge: ")
                .append(edge.getName())
                .append(", target: ")
                .append(target.getName())
                .append("}");
        if( alias != null ) {
            res.append(" AS ")
               .append(alias.getName());
        }
        return res.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TripleMapReturnStatement)) return false;
        TripleMapReturnStatement that = (TripleMapReturnStatement) o;
        return source.equals(that.source) && edge.equals(that.edge) && target.equals(that.target)
                && Objects.equals(alias, that.alias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, edge, target, alias);
    }
}
