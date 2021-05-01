package se.liu.ida.hefquin.federation.access.impl.req;

import java.util.Set;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.federation.access.BGPRequest;
import se.liu.ida.hefquin.query.BGP;
import se.liu.ida.hefquin.query.jenaimpl.JenaBasedQueryPatternUtils;

public class BGPRequestImpl implements BGPRequest
{
	protected final BGP bgp;

	public BGPRequestImpl( final BGP bgp ) {
		assert bgp != null;
		this.bgp = bgp;
	}

	@Override
	public BGP getQueryPattern() {
		return bgp;
	}

	@Override
	public Set<Var> getExpectedVariables() {
		return JenaBasedQueryPatternUtils.getVariablesInPattern(bgp);
	}

}
