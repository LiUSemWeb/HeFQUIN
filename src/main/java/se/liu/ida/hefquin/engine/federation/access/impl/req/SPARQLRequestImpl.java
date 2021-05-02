package se.liu.ida.hefquin.engine.federation.access.impl.req;

import java.util.Set;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.impl.QueryPatternUtils;

public class SPARQLRequestImpl implements SPARQLRequest
{
	protected final SPARQLGraphPattern pattern;

	public SPARQLRequestImpl( final SPARQLGraphPattern pattern ) {
		assert pattern != null;
		this.pattern = pattern;
	}

	public SPARQLGraphPattern getQueryPattern() {
		return pattern;
	}

	@Override
	public Set<Var> getExpectedVariables() {
		// Attention: this implementation may not be entirely correct;
		// not all variables in the pattern may be certain variables.
		return QueryPatternUtils.getVariablesInPattern(pattern);
	}

}
