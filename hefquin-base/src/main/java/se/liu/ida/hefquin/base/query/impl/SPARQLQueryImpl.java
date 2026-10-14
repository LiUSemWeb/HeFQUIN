package se.liu.ida.hefquin.base.query.impl;

import java.util.Set;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.SPARQLQuery;
import se.liu.ida.hefquin.base.query.utils.QueryPatternUtils;
import se.liu.ida.hefquin.jenaext.query.QueryUtils;

public class SPARQLQueryImpl implements SPARQLQuery
{
	protected final Query jenaQuery;

	// We materialize the ExpectedVariables in this case to avoid
	// producing it over and over again whenever it is used.
	protected final ExpectedVariables expectedVars;

	public SPARQLQueryImpl( final Query jenaQuery ) {
		assert jenaQuery != null;
		this.jenaQuery = jenaQuery;

		expectedVars = QueryUtils.determineExpectedVariables(jenaQuery);
	}

	public SPARQLQueryImpl( final SPARQLGraphPattern p,
	                        final Set<Var> projectionVars,
	                        final boolean distinctRequired,
	                        final int offset,
	                        final int limit ) {
		this( QueryPatternUtils.convertToJenaElement(p), projectionVars, distinctRequired, offset, limit );
	}

	protected SPARQLQueryImpl( final Element jenaElement,
	                           final Set<Var> projectionVars,
	                           final boolean distinctRequired,
	                           final int offset,
	                           final int limit ) {
		assert jenaElement != null;
		assert offset >= 0;

		jenaQuery = QueryFactory.create();
		jenaQuery.setQuerySelectType();
		if ( projectionVars != null ) {
			jenaQuery.setQueryResultStar( false );
			jenaQuery.getProject().clear();
			projectionVars.forEach(jenaQuery::addResultVar);
		}
		else
			jenaQuery.setQueryResultStar( true );


		jenaQuery.setDistinct(distinctRequired);
		jenaQuery.setQueryPattern(jenaElement);

		if ( offset == 0 )
			jenaQuery.setOffset( Query.NOLIMIT );
		else
			jenaQuery.setOffset(offset);

		if ( limit < 0 )
			jenaQuery.setLimit( Query.NOLIMIT );
		else
			jenaQuery.setLimit(limit);

		expectedVars = QueryUtils.determineExpectedVariables(jenaQuery);
	}

	@Override
	public ExpectedVariables getExpectedVariables() {
		return expectedVars;
	}

	@Override
	public boolean equals( final Object o ) {
		if ( o == this ) return true;

		return o instanceof SPARQLQuery oo && oo.asJenaQuery().equals(jenaQuery);
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
		return jenaQuery.toString();
	}

}
