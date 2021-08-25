package se.liu.ida.hefquin.engine.federation.access.impl;

import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.BRTPFRequestProcessor;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.Neo4jRequestProcessor;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.SPARQLRequestProcessor;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.TPFRequestProcessor;

public abstract class FederationAccessManagerBase2 extends FederationAccessManagerBase1
{
	protected final SPARQLRequestProcessor    reqProcSPARQL;
	protected final TPFRequestProcessor       reqProcTPF;
	protected final BRTPFRequestProcessor     reqProcBRTPF;
	protected final Neo4jRequestProcessor     reqProcNeo4j;

	protected FederationAccessManagerBase2(
			final SPARQLRequestProcessor reqProcSPARQL,
			final TPFRequestProcessor reqProcTPF,
			final BRTPFRequestProcessor reqProcBRTPF,
			final Neo4jRequestProcessor reqProcNeo4j )
	{
		assert reqProcSPARQL  != null;
		assert reqProcTPF     != null;
		assert reqProcBRTPF   != null;
		assert reqProcNeo4j	  != null;

		this.reqProcSPARQL    = reqProcSPARQL;
		this.reqProcTPF       = reqProcTPF;
		this.reqProcBRTPF     = reqProcBRTPF;
		this.reqProcNeo4j     = reqProcNeo4j;
	}

}
