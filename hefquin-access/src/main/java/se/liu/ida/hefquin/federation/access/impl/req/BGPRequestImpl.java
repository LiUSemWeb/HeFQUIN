package se.liu.ida.hefquin.federation.access.impl.req;

import se.liu.ida.hefquin.base.query.BGP;
import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.federation.access.BGPRequest;

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
	public int hashCode(){
		return bgp.hashCode();
	}

	@Override
	public BGP getQueryPattern() {
		return bgp;
	}

	@Override
	public ExpectedVariables getExpectedVariables() {
		return bgp.getExpectedVariables();
	}

	@Override
	public String toString(){
		return bgp.toString();
	}

}
