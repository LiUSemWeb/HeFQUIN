package se.liu.ida.hefquin.engine.federation.access.impl.req;

import se.liu.ida.hefquin.engine.federation.access.Neo4jRequest;
import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;

public class Neo4jRequestImpl implements Neo4jRequest {

    protected final String cypherQuery;

    public Neo4jRequestImpl(final String cypherQuery) {
        assert cypherQuery != null;
        this.cypherQuery =  cypherQuery;
    }

	@Override
	public boolean equals( final Object o ) {
		return o instanceof Neo4jRequest && ((Neo4jRequest) o).getCypherQuery().equals(cypherQuery);
	}

    @Override
    public ExpectedVariables getExpectedVariables() {
        return null;
    }

    @Override
    public String getCypherQuery() {
        return cypherQuery;
    }
}
