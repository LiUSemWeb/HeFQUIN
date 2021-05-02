package se.liu.ida.hefquin.engine.federation.catalog.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.catalog.FederationCatalog;

public class FederationCatalogImpl implements FederationCatalog
{
	protected final Map<String, FederationMember> membersByURI;

	public FederationCatalogImpl() {
		this( new HashMap<String, FederationMember>() );
	}

	public FederationCatalogImpl( final Map<String, FederationMember> membersByURI ) {
		assert membersByURI != null;
		this.membersByURI = membersByURI;
	}

	@Override
	public FederationMember getFederationMemberByURI( final String uri ) {
		final FederationMember fm = membersByURI.get(uri);
		if ( fm == null ) {
			throw new NoSuchElementException( "no federation member with URI <" + uri + ">" );
		}
		return fm;
	}

}
