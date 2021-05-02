package se.liu.ida.hefquin.engine.queryproc;

import se.liu.ida.hefquin.engine.federation.FederationAccessManager;

public class ExecutionContext
{
	protected final FederationAccessManager fedAccessMgr;

	public ExecutionContext( final FederationAccessManager fedAccessMgr ) {
		assert fedAccessMgr != null;
		this.fedAccessMgr = fedAccessMgr;
	}

	public FederationAccessManager getFederationAccessMgr() {
		return fedAccessMgr;
	}

}
