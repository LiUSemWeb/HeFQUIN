package se.liu.ida.hefquin.engine.federation;

import se.liu.ida.hefquin.engine.data.VocabularyMapping;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalInterface;

public interface FederationMember
{
	DataRetrievalInterface getInterface();
	
	/**
	 * For federation members for which sub-queries need to be rewritten
	 * based on a particular vocabulary mapping, this function return that
	 * mapping. For the federation members for which the sub-queries can
	 * be used as is, this function returns <code>null</code>.
	 */
	VocabularyMapping getVocabularyMapping();

}
