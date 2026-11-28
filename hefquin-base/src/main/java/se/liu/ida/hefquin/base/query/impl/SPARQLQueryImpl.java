package se.liu.ida.hefquin.base.query.impl;

import java.util.List;
import java.util.Set;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.SortCondition;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
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
	                        final List<SortCondition> sortConditions,
	                        final boolean distinctRequired ) {
		if ( p instanceof GenericSPARQLGraphPatternImpl2 p2 ) {
			// GenericSPARQLGraphPatternImpl2 needs to be handled in
			// a special way because it contains a Jena Op object that
			// represents the graph pattern, but Op objects may actually
			// represent more than just graph patterns (think OpProject,
			// OpDistinct, etc).
			jenaQuery = createJenaQuery( p2.asJenaOp(),
			                             projectionVars,
			                             sortConditions,
			                             distinctRequired );
		}
		else {
			jenaQuery = createJenaQuery( QueryPatternUtils.convertToJenaElement(p),
			                             projectionVars,
			                             sortConditions,
			                             distinctRequired );
		}

		expectedVars = QueryUtils.determineExpectedVariables(jenaQuery);
	}

	protected static Query createJenaQuery( final Op jenaOp,
	                                        final Set<Var> projectionVars,
	                                        final List<SortCondition> sortConditions,
	                                        final boolean distinctRequired ) {
		// Convert the Jena 'Op' object directly into a Jena 'Query' object.
		final Query q = OpAsQuery.asQuery(jenaOp);

		// But now we also need to check that the arguments
		// are compatible with the resulting 'Query' object.

		// - The query should contain DISTINCT if the given distinct flag is
		//   true or the given 'Op' object contained a distinct operator (in
		//   the latter case, we do not actually need to do anything because
		//   the distinct operator should have been turned into DISTINCT in
		//   the resulting 'Query' object).
		if ( distinctRequired ) {
			q.setDistinct(true);
		}

		// - check the projection
		if ( q.isQueryResultStar() ) {
			if ( projectionVars != null ) {
				q.setQueryResultStar( false );
				q.getProject().clear();
				projectionVars.forEach(q::addResultVar);
			}
		}
		else if ( projectionVars != null ) {
			final List<Var> projectionVarsInQ = q.getProjectVars();
			if (    ! projectionVarsInQ.containsAll(projectionVars)
			     || ! projectionVars.containsAll(projectionVarsInQ) ) {
				final String msg = "The given projection variables (" +
						projectionVars.toString() + ") differ from the " +
						"variables of the project in the given Op (" +
						projectionVarsInQ.toString() + ").";
				throw new IllegalArgumentException(msg);
			}
		}

		// - check ORDER BY
		if ( sortConditions !=null ) {
			if ( ! q.hasOrderBy() ) {
				sortConditions.forEach(q::addOrderBy);
			}
			else if ( ! q.getOrderBy().equals(sortConditions) ) {
				final String msg = "The given sort conditions (" +
						sortConditions.toString() + ") differ from the " +
						"sort conditions of the order-by in the given " +
						"Op (" + q.getOrderBy().toString() + ").";
				throw new IllegalArgumentException(msg);
			}
		}

		return q;
	}

	protected static Query createJenaQuery( final Element jenaElement,
	                                        final Set<Var> projectionVars,
	                                        final List<SortCondition> sortConditions,
	                                        final boolean distinctRequired ) {
		final Query q = QueryFactory.create();
		q.setQuerySelectType();
		if ( projectionVars != null ) {
			q.setQueryResultStar( false );
			q.getProject().clear();
			projectionVars.forEach(q::addResultVar);
		}
		else
			q.setQueryResultStar( true );

		if ( sortConditions != null )
			sortConditions.forEach(q::addOrderBy);

		q.setDistinct( distinctRequired );
		q.setQueryPattern( jenaElement );

		return q;
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
		return this.asJenaQuery().toString();
	}

}
