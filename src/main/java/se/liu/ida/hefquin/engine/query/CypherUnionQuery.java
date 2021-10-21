package se.liu.ida.hefquin.engine.query;

import java.util.List;

public interface CypherUnionQuery extends CypherQuery {
    List<CypherMatchQuery> getUnion();
}
