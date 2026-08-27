package se.liu.ida.hefquin.federation;

import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.federation.authentication.AuthenticationInformation;

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
	 * Returns the (unique) service URI of this federation member. The
	 * service URI is the URI via which the federation member is referred
	 * to in the SERVICE clauses.
	 */
	String getServiceURI();

	/**
	 * Returns the authentication information of this federation member
	 * or {@code null} if there is no authentication information for this
	 * federation member (in which case it can be assumed that this
	 * federation member can be accessed without authentication).
	*/
	AuthenticationInformation getAuthenticationInformation();

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
}
