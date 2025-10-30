package se.liu.ida.hefquin.federation.members.impl;

import se.liu.ida.hefquin.federation.FederationMember;

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

	protected BaseForFederationMember() { id = ++counter; }

	@Override public int getID() { return id; }

	@Override
	public boolean equals( final Object o ) {
		if ( o == this )
			return true;

		return    o instanceof BaseForFederationMember fm
		       && fm.getID() == id;
	}
}
