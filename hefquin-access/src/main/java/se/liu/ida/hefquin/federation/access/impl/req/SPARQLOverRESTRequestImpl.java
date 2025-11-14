package se.liu.ida.hefquin.federation.access.impl.req;

import java.util.List;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.federation.access.SPARQLOverRESTRequest;

public class SPARQLOverRESTRequestImpl implements SPARQLOverRESTRequest
{
	protected final SPARQLGraphPattern pattern;
	protected final List<Var> paramVars;
	protected final ExpectedVariables expectedVars;

	public SPARQLOverRESTRequestImpl( final SPARQLGraphPattern pattern,
	                                  final List<Var> paramVars ) {
		assert pattern != null;

		this.pattern = pattern;
		this.paramVars = paramVars;

		// we materialize the ExpectedVariables in this case
		// to avoid producing it again whenever it is used
		expectedVars = pattern.getExpectedVariables();
	}

	@Override
	public boolean equals( final Object o ) {
		if ( o == this )
			return true;

		if (    o instanceof SPARQLOverRESTRequest r
		     && r.getQueryPattern().equals(pattern)
		     && r.getParamVars().equals(paramVars) )
			return true;

		return false;
	}

	@Override
	public int hashCode(){
		return pattern.hashCode() & paramVars.hashCode();
	}

	@Override
	public SPARQLGraphPattern getQueryPattern() {
		return pattern;
	}

	@Override
	public List<Var> getParamVars() {
		return paramVars;
	}

	@Override
	public ExpectedVariables getExpectedVariables() {
		return expectedVars;
	}

	@Override
	public String toString(){
		return "SPARQLOverRESTRequest with pattern: " + pattern.toString();
	}

}
