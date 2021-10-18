package se.liu.ida.hefquin.engine.query;

import java.util.Set;

public interface CypherUnionQuery extends CypherQuery {
    Set<CypherMatchQuery> getUnion();
}
