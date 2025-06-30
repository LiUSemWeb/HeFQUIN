package se.liu.ida.hefquin.engine.federation.access;

import se.liu.ida.hefquin.base.query.BGP;

public interface BGPRequest extends SPARQLRequest
{
	@Override
	BGP getQueryPattern();
}
