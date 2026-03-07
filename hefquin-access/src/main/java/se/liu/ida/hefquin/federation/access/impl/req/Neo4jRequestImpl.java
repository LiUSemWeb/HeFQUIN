package se.liu.ida.hefquin.federation.access.impl.req;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.federation.access.Neo4jRequest;

import java.util.Objects;

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
    public int hashCode(){
        return Objects.hash(cypherQuery);
    }

    @Override
    public ExpectedVariables getExpectedVariables() {
        return null;
    }

    @Override
    public String getCypherQuery() {
        return cypherQuery;
    }

    @Override
    public String toString(){
        return "Neo4j Request TODO";
    }

}
