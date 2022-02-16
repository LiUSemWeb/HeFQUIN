package se.liu.ida.hefquin.engine.federation;

import se.liu.ida.hefquin.engine.data.VocabularyMapping;
import se.liu.ida.hefquin.engine.federation.access.SPARQLEndpointInterface;

public interface SPARQLEndpoint extends FederationMember
{
	@Override
	SPARQLEndpointInterface getInterface();
	
	@Override
	VocabularyMapping getVocabularyMapping();
}
