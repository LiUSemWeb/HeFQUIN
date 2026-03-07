package se.liu.ida.hefquin.federation.catalog;

import java.util.NoSuchElementException;
import java.util.Set;

import se.liu.ida.hefquin.federation.FederationMember;

public interface FederationCatalog
{
	/**
	 * Returns an {@link Iterable} over all federation members in this catalog.
	 */
	Set<FederationMember> getAllFederationMembers();

	/**
	 * Returns the federation member identified by the given URI.
	 *
	 * Throws a {@link NoSuchElementException} if there is no federation
	 * member for the given URI.
	 */
	FederationMember getFederationMemberByURI( String uri );
}
