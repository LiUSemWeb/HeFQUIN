package se.liu.ida.hefquin.federation;

import se.liu.ida.hefquin.federation.wrappers.Wrapper;

/**
 * Captures all kinds of federation members from which data cannot be
 * retrieved in some RDF-related form and, thus, for which some form
 * of local (HeFQUIN-side) wrapper is required.
 */
public interface WrappedFederationMember extends FederationMember
{
	/**
	 * Returns the wrapper for this federation member.
	 */
	Wrapper getWrapper();
}
