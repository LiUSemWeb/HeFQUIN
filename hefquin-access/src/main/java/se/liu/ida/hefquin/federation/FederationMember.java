package se.liu.ida.hefquin.federation;

import se.liu.ida.hefquin.base.data.VocabularyMapping;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;

/**
 * This interface captures any kind of federation member.
 */
public interface FederationMember
{
	/**
	 * Returns an identifier of this federation member, which should
	 * be unique (independent of the type of federation member).
	 */
	int getID();

	/**
	 * Returns {@code false} if the only types of graph patterns that
	 * can be answered by a single request to this federation member
	 * are triple patterns.
	 * <p>
	 * Notice that a return value of {@code true} does not mean that the
	 * federation member supports arbitrary graph patterns, but only that
	 * it supports more than only triple patterns. For a more specific way
	 * of checking, use {@link #isSupportedPattern(SPARQLGraphPattern)}.
	 */
	boolean supportsMoreThanTriplePatterns();

	/**
	 * Returns {@code true} if this federation member supports answering
	 * the given graph patterns in a single request.
	 */
	boolean isSupportedPattern( SPARQLGraphPattern p );

	/**
	 * For federation members for which sub-queries need to be rewritten
	 * based on a particular vocabulary mapping, this function return that
	 * mapping. For the federation members for which the sub-queries can
	 * be used as is, this function returns <code>null</code>.
	 */
	VocabularyMapping getVocabularyMapping();
}
