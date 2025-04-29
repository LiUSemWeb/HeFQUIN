package se.liu.ida.hefquin.base.query.impl;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.syntax.Element;

import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.SPARQLQuery;
import se.liu.ida.hefquin.base.query.utils.QueryPatternUtils;

public class SPARQLQueryImpl implements SPARQLQuery
{
	protected final Query jenaQuery;

	public SPARQLQueryImpl( final Query jenaQuery ) {
		assert jenaQuery != null;
		this.jenaQuery = jenaQuery;
	}

	public SPARQLQueryImpl( final SPARQLGraphPattern p ) {
		this( QueryPatternUtils.convertToJenaElement(p) );
	}

	protected SPARQLQueryImpl( final Element jenaElement ) {
		assert jenaElement != null;
		
		jenaQuery = QueryFactory.create();
		jenaQuery.setQuerySelectType();
		jenaQuery.setQueryResultStar(true);
		jenaQuery.setQueryPattern( jenaElement );
	}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof SPARQLQuery && ((SPARQLQuery) o).asJenaQuery().equals(jenaQuery);
	}

	@Override
	public int hashCode() {
		return jenaQuery.hashCode();
	}

	@Override
	public Query asJenaQuery() {
		return jenaQuery;
	}

	@Override
	public String toString(){
		return this.asJenaQuery().toString();
	}

}
