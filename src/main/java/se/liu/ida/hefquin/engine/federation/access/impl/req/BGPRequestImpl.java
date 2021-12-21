package se.liu.ida.hefquin.engine.federation.access.impl.req;

import se.liu.ida.hefquin.engine.federation.access.BGPRequest;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.impl.QueryPatternUtils;
import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;

public class BGPRequestImpl implements BGPRequest
{
	protected final BGP bgp;

	public BGPRequestImpl( final BGP bgp ) {
		assert bgp != null;
		this.bgp = bgp;
	}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof BGPRequest && ((BGPRequest) o).getQueryPattern().equals(bgp);
	}

	@Override
	public BGP getQueryPattern() {
		return bgp;
	}

	@Override
	public ExpectedVariables getExpectedVariables() {
		return QueryPatternUtils.getExpectedVariablesInPattern(bgp);
	}

	@Override
	public String toString(){
		return bgp.toString();
	}

}
