package se.liu.ida.hefquin.federation.access.impl;

import se.liu.ida.hefquin.federation.FederationAccessManager;
import se.liu.ida.hefquin.federation.access.impl.reqproc.BRTPFRequestProcessor;
import se.liu.ida.hefquin.federation.access.impl.reqproc.RequestProcessor;
import se.liu.ida.hefquin.federation.access.impl.reqproc.SPARQLRequestProcessor;
import se.liu.ida.hefquin.federation.access.impl.reqproc.TPFRequestProcessor;

/**
 * Abstract base class for implementations of the {@link FederationAccessManager}
 * interface that use request processors (see {@link RequestProcessor} etc).
 */
public abstract class FederationAccessManagerBase implements FederationAccessManager
{
	protected final SPARQLRequestProcessor    reqProcSPARQL;
	protected final TPFRequestProcessor       reqProcTPF;
	protected final BRTPFRequestProcessor     reqProcBRTPF;

	protected FederationAccessManagerBase(
			final SPARQLRequestProcessor reqProcSPARQL,
			final TPFRequestProcessor reqProcTPF,
			final BRTPFRequestProcessor reqProcBRTPF ) {
		assert reqProcSPARQL  != null;
		assert reqProcTPF     != null;
		assert reqProcBRTPF   != null;

		this.reqProcSPARQL    = reqProcSPARQL;
		this.reqProcTPF       = reqProcTPF;
		this.reqProcBRTPF     = reqProcBRTPF;
	}

}
