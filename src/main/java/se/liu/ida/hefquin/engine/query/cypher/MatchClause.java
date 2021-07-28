package se.liu.ida.hefquin.engine.query.cypher;

public interface MatchClause {
    boolean isRedundantWith(final MatchClause match);
}
