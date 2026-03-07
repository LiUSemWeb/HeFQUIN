package se.liu.ida.hefquin.federation.access.impl.req;

import java.util.Iterator;
import java.util.Set;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

import se.liu.ida.hefquin.base.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.federation.access.BindingsRestrictedTriplePatternRequest;

public class BindingsRestrictedTriplePatternRequestImpl implements BindingsRestrictedTriplePatternRequest
{
	protected final TriplePattern tp;
	protected final Set<Binding> solMaps;

	@SuppressWarnings("unused")
	public BindingsRestrictedTriplePatternRequestImpl(
			final TriplePattern tp,
			final Set<Binding> solMaps ) {
		assert tp != null;
		assert solMaps != null;

		this.tp = tp;
		this.solMaps = solMaps;

		if ( false ) {
			// check that the given solution mappings do not contain any blank nodes
			for ( final Binding sm : solMaps ) {
				final Iterator<Var> it = sm.vars();
				while ( it.hasNext() ) {
					assert ! sm.get( it.next() ).isBlank();
				}
			}
		}
	}

	@Override
	public boolean equals( final Object o ) {
		if ( ! (o instanceof BindingsRestrictedTriplePatternRequest) )
			return false;

		final BindingsRestrictedTriplePatternRequest oo = (BindingsRestrictedTriplePatternRequest) o;
		return oo.getTriplePattern().equals(tp)
				&& SolutionMappingUtils.equalSets(oo.getSolutionMappings(), solMaps);
	}

	@Override
	public int hashCode(){
		int code = tp.hashCode();
		for( final Binding sm : solMaps )
			code = code ^ sm.hashCode();
		return code;
	}

	@Override
	public TriplePattern getTriplePattern() {
		return tp;
	}

	@Override
	public Set<Binding> getSolutionMappings() {
		return solMaps;
	}

	@Override
	public ExpectedVariables getExpectedVariables() {
		return tp.getExpectedVariables();
	}

	@Override
	public String toString(){
		return tp.toString();
	}

}
