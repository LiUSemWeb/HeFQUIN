package se.liu.ida.hefquin.engine.query.impl;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.syntax.Element;

import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.SPARQLQuery;

public class SPARQLQueryImpl implements SPARQLQuery
{
	protected final Query jenaQuery;

	public SPARQLQueryImpl( final Query jenaQuery ) {
		assert jenaQuery != null;
		this.jenaQuery = jenaQuery;
	}

	public SPARQLQueryImpl( final SPARQLGraphPattern p ) {
		this( p.asJenaOp() );
	}

	public SPARQLQueryImpl( final Op jenaOp ) {
		assert jenaOp != null;

		final Element queryPattern = OpAsQuery.asQuery(jenaOp).getQueryPattern();
		
		jenaQuery = QueryFactory.create();
		jenaQuery.setQuerySelectType();
		jenaQuery.setQueryResultStar(true);
		jenaQuery.setQueryPattern( queryPattern );
	}

	@Override
	public Query asJenaQuery() {
		return jenaQuery;
	}

}
