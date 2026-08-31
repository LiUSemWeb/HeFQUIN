package se.liu.ida.hefquin.federation.members.impl;

import org.apache.jena.graph.Node;

import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.authentication.AuthenticationInformation;

/**
 * This is an abstract base class for classes that implement concrete
 * specializations (sub-interfaces) of the {@link FederationMember}
 * interface. This base class handles the creation of a unique ID per
 * federation member.
 */
public abstract class BaseForFederationMember implements FederationMember
{
	private static int counter = 0;

	protected final int id;
	protected final String serviceURI;
	protected final AuthenticationInformation authInfo;

	protected BaseForFederationMember( final Node serviceURI, final AuthenticationInformation authInfo ) {
		this( serviceURI.getURI(), authInfo );
	}

	private BaseForFederationMember( final String serviceURI, final AuthenticationInformation authInfo ) {
		assert serviceURI != null;

		this.serviceURI = serviceURI;
		this.authInfo = authInfo;
		this.id = ++counter;
	}

	@Override public int getID() { return id; }

	@Override public String getServiceURI() { return serviceURI; }

	@Override public AuthenticationInformation getAuthenticationInformation() { return authInfo; }

	@Override
	public boolean equals( final Object o ) {
		if ( o == this )
			return true;

		return    o instanceof BaseForFederationMember fm
		       && fm.getID() == id
		       && fm.getServiceURI().equals(serviceURI);
	}
}
