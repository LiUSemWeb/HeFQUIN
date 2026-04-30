package se.liu.ida.hefquin.base.query.impl;

import java.util.Set;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.Var;
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

	public SPARQLQueryImpl( final SPARQLGraphPattern p,
	                        final Set<Var> projectionVars,
	                        final boolean isDistinct ) {
		this( QueryPatternUtils.convertToJenaElement(p) );

		// Apply request-level projection if specified and safe.
		// This replaces the SELECT clause with the given variables.
		// Note: This is only done when it does not interfere with query semantics
		// (e.g., no aggregation or grouping present).
		if ( projectionVars != null && ! projectionVars.isEmpty() ) {
			jenaQuery.setQueryResultStar(false);
			jenaQuery.getProject().clear();
			projectionVars.forEach(jenaQuery::addResultVar);
		}

		// Apply DISTINCT if requested.
		// This enforces duplicate elimination at the endpoint level.
		if ( isDistinct )
			jenaQuery.setDistinct( true );
	}

	protected SPARQLQueryImpl( final Element jenaElement ) {
		assert jenaElement != null;

		jenaQuery = QueryFactory.create();
		jenaQuery.setQuerySelectType();
		jenaQuery.setQueryResultStar( true );
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
