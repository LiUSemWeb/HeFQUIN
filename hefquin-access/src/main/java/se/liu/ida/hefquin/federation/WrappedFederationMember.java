package se.liu.ida.hefquin.federation;

import se.liu.ida.hefquin.federation.wrappers.Wrapper;

public interface WrappedFederationMember extends FederationMember
{
	/**
	 * Returns the wrapper for this federation member.
	 */
	Wrapper getWrapper();
}
