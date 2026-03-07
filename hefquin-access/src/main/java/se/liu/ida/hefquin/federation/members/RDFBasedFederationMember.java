package se.liu.ida.hefquin.federation.members;

import se.liu.ida.hefquin.base.data.VocabularyMapping;
import se.liu.ida.hefquin.federation.FederationMember;

/**
 * This interface captures any kind of federation member
 * that provides RDF-based data access.
 */
public interface RDFBasedFederationMember extends FederationMember
{
	/**
	 * For federation members for which sub-queries need to be rewritten
	 * based on a particular vocabulary mapping, this function return that
	 * mapping. For the federation members for which the sub-queries can
	 * be used as is, this function returns <code>null</code>.
	 */
	VocabularyMapping getVocabularyMapping();
}
