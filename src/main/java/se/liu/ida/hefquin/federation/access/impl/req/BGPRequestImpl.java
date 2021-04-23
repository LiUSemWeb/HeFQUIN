package se.liu.ida.hefquin.federation.access.impl.req;

import se.liu.ida.hefquin.federation.access.BGPRequest;
import se.liu.ida.hefquin.query.BGP;

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

}
