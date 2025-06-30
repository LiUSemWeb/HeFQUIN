package se.liu.ida.hefquin.federation.catalog.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.catalog.FederationCatalog;

public class FederationCatalogImpl implements FederationCatalog
{
	protected final Map<String, FederationMember> membersByURI;
	protected Set<FederationMember> members = null;

	public FederationCatalogImpl() {
		this( new HashMap<String, FederationMember>() );
	}

	public FederationCatalogImpl( final Map<String, FederationMember> membersByURI ) {
		assert membersByURI != null;
		this.membersByURI = membersByURI;
	}

	@Override
	public Set<FederationMember> getAllFederationMembers() {
		if ( members == null ) {
			members = new HashSet<>( membersByURI.values() );
		}

		return members;
	}

	@Override
	public FederationMember getFederationMemberByURI( final String uri ) {
		final FederationMember fm = membersByURI.get(uri);
		if ( fm == null ) {
			throw new NoSuchElementException( "no federation member with URI <" + uri + ">" );
		}
		return fm;
	}

	public void addMember( final String uri, final FederationMember fm ) {
		membersByURI.put(uri, fm);

		if ( members != null ) {
			members.add(fm);
		}
	}

}
