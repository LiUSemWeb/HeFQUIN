package se.liu.ida.hefquin.engine.federation.access.impl.req;

import java.util.Iterator;
import java.util.Set;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.federation.access.BindingsRestrictedTriplePatternRequest;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.QueryPatternUtils;
import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;

public class BindingsRestrictedTriplePatternRequestImpl implements BindingsRestrictedTriplePatternRequest
{
	protected final TriplePattern tp;
	protected final Set<SolutionMapping> solMaps;

	@SuppressWarnings("unused")
	public BindingsRestrictedTriplePatternRequestImpl(
			final TriplePattern tp,
			final Set<SolutionMapping> solMaps ) {
		assert tp != null;
		assert solMaps != null;

		this.tp = tp;
		this.solMaps = solMaps;

		if ( false ) {
			// check that the given solution mappings do not contain any blank nodes
			for ( final SolutionMapping sm : solMaps ) {
				final Binding b = sm.asJenaBinding();
				final Iterator<Var> it = b.vars();
				while ( it.hasNext() ) {
					assert ! b.get( it.next() ).isBlank();
				}
			}
		}
	}

	public TriplePattern getTriplePattern() {
		return tp;
	}

	public Set<SolutionMapping> getSolutionMappings() {
		return solMaps;
	}

	@Override
	public ExpectedVariables getExpectedVariables() {
		return QueryPatternUtils.getExpectedVariablesInPattern(tp);
	}

}
