package se.liu.ida.hefquin.federation.access;

import se.liu.ida.hefquin.query.BGP;

public interface BGPRequest extends SPARQLRequest
{
	@Override
	BGP getQueryPattern();
}
