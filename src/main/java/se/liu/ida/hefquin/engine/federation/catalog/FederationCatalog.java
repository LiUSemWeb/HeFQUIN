package se.liu.ida.hefquin.engine.federation.catalog;

import java.util.NoSuchElementException;

import se.liu.ida.hefquin.engine.federation.FederationMember;

public interface FederationCatalog
{
	/**
	 * Returns the federation member identified by the given URI.
	 *
	 * Throws a {@link NoSuchElementException} if there is no federation
	 * member for the given URI.
	 */
	FederationMember getFederationMemberByURI( String uri );
}
