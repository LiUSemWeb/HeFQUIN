package se.liu.ida.hefquin.engine.federation.access.impl;

import se.liu.ida.hefquin.engine.federation.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.*;

/**
 * Abstract base class for implementations of the {@link FederationAccessManager}
 * interface that use request processors (see {@link RequestProcessor} etc).
 */
public abstract class FederationAccessManagerBase implements FederationAccessManager
{
	protected final SPARQLRequestProcessor    reqProcSPARQL;
	protected final TPFRequestProcessor       reqProcTPF;
	protected final BRTPFRequestProcessor     reqProcBRTPF;
	protected final Neo4jRequestProcessor	  reqProcNeo4j;

	protected FederationAccessManagerBase(
			final SPARQLRequestProcessor reqProcSPARQL,
			final TPFRequestProcessor reqProcTPF,
			final BRTPFRequestProcessor reqProcBRTPF,
			final Neo4jRequestProcessor reqProcNeo4j ) {

		assert reqProcSPARQL  != null;
		assert reqProcTPF     != null;
		assert reqProcBRTPF   != null;
		assert reqProcNeo4j	  != null;

		this.reqProcSPARQL    = reqProcSPARQL;
		this.reqProcTPF       = reqProcTPF;
		this.reqProcBRTPF     = reqProcBRTPF;
		this.reqProcNeo4j = reqProcNeo4j;
	}

}
