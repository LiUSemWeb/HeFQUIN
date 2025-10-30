package se.liu.ida.hefquin.federation.access.impl;

import java.util.ArrayList;
import java.util.List;

import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.BRTPFRequest;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.DataRetrievalResponse;
import se.liu.ida.hefquin.federation.access.FederationAccessException;
import se.liu.ida.hefquin.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.federation.access.TPFRequest;
import se.liu.ida.hefquin.federation.access.impl.reqproc.BRTPFRequestProcessor;
import se.liu.ida.hefquin.federation.access.impl.reqproc.Neo4jRequestProcessor;
import se.liu.ida.hefquin.federation.access.impl.reqproc.SPARQLRequestProcessor;
import se.liu.ida.hefquin.federation.access.impl.reqproc.TPFRequestProcessor;
import se.liu.ida.hefquin.federation.members.BRTPFServer;
import se.liu.ida.hefquin.federation.members.SPARQLEndpoint;
import se.liu.ida.hefquin.federation.members.TPFServer;

/**
 * Abstract base class for implementations of the {@link FederationAccessManager}
 * interface that use request processors (see {@link RequestProcessor} etc).
 */
public abstract class FederationAccessManagerBase2 extends FederationAccessManagerBase1
{
	private final List<RequestProcessor<?,?,?>> reqProcessors = new ArrayList<>();

	protected final SPARQLRequestProcessor    reqProcSPARQL;
	protected final TPFRequestProcessor       reqProcTPF;
	protected final BRTPFRequestProcessor     reqProcBRTPF;

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

		reqProcessors.add(reqProcNeo4j);
	}

	protected < ReqType extends DataRetrievalRequest,
	            RespType extends DataRetrievalResponse<?>,
	            MemberType extends FederationMember >
	RequestProcessor<ReqType, RespType, MemberType> getReqProc( final ReqType req,
	                                                            final MemberType fm )
			throws FederationAccessException
	{
		final Class<? extends DataRetrievalRequest> reqClass = req.getClass();
		final Class<? extends FederationMember> fmClass = fm.getClass();

		final RequestProcessor<?,?,?> common = getCommonReqProc(req, fm);
		if ( common != null ) {
			@SuppressWarnings("unchecked")
			final RequestProcessor<ReqType, RespType, MemberType> reqProc =
					(RequestProcessor<ReqType, RespType, MemberType>) common;
			return reqProc;
		}

		for ( final RequestProcessor<?,?,?> candidate : reqProcessors ) {
			if (    candidate.isSupportedMemberType(fmClass)
			     && candidate.isSupportedRequestType(reqClass) )
			{
				@SuppressWarnings("unchecked")
				final RequestProcessor<ReqType, RespType, MemberType> reqProc = (RequestProcessor<ReqType, RespType, MemberType>) candidate;
				return reqProc;
			}
		}

		throw new FederationAccessException("No request processor registered for requests of type " + reqClass.getName() + " (at federation members of type " + fmClass.getName() + ").", req, fm);
	}

	private RequestProcessor<?,?,?> getCommonReqProc( final DataRetrievalRequest req,
	                                                  final FederationMember fm ) {
		if ( req instanceof SPARQLRequest && fm instanceof SPARQLEndpoint )
			return reqProcSPARQL;

		if ( req instanceof TPFRequest && fm instanceof TPFServer )
			return reqProcTPF;

		if ( req instanceof TPFRequest && fm instanceof BRTPFServer )
			return reqProcTPF;

		if ( req instanceof BRTPFRequest && fm instanceof BRTPFServer )
			return reqProcBRTPF;

		return null;
	}
}
