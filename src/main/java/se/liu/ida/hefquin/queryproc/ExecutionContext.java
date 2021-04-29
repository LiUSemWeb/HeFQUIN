package se.liu.ida.hefquin.queryproc;

import se.liu.ida.hefquin.federation.FederationAccessManager;

public class ExecutionContext
{
	protected final FederationAccessManager fedAccessMgr;
	protected final int index;

	public ExecutionContext( final FederationAccessManager fedAccessMgr ) {
		assert fedAccessMgr != null;
		this.fedAccessMgr = fedAccessMgr;
		this.index = 0;
	}
	public ExecutionContext(final int index) {
		this.fedAccessMgr = null;
		this.index = index;
	}

	public FederationAccessManager getFederationAccessMgr() {
		return fedAccessMgr;
	}

	public int getIndex() {
		return index;
	}
}
