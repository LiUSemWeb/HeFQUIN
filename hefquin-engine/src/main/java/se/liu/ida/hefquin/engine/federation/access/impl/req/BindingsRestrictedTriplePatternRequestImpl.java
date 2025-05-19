package se.liu.ida.hefquin.engine.federation.access.impl.req;

import java.util.Iterator;
import java.util.Set;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.engine.federation.access.BindingsRestrictedTriplePatternRequest;

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

	@Override
	public boolean equals( final Object o ) {
		if ( ! (o instanceof BindingsRestrictedTriplePatternRequest) )
			return false;

		final BindingsRestrictedTriplePatternRequest oo = (BindingsRestrictedTriplePatternRequest) o;
		return oo.getTriplePattern().equals(tp)
				&& SolutionMappingUtils.equals(oo.getSolutionMappings(), solMaps);
	}

	@Override
	public int hashCode(){
		int code = tp.hashCode();
		for( SolutionMapping solm: solMaps )
			code = code ^ solm.hashCode();
		return code;
	}

	@Override
	public TriplePattern getTriplePattern() {
		return tp;
	}

	@Override
	public Set<SolutionMapping> getSolutionMappings() {
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
