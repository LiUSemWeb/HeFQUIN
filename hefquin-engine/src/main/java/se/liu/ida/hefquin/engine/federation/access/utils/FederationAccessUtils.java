package se.liu.ida.hefquin.engine.federation.access.utils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import se.liu.ida.hefquin.base.utils.CompletableFutureUtils;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.*;
import se.liu.ida.hefquin.federation.access.impl.req.BRTPFRequestImpl;
import se.liu.ida.hefquin.federation.access.impl.req.TPFRequestImpl;
import se.liu.ida.hefquin.federation.members.BRTPFServer;
import se.liu.ida.hefquin.federation.members.Neo4jServer;
import se.liu.ida.hefquin.federation.members.SPARQLEndpoint;
import se.liu.ida.hefquin.federation.members.TPFServer;

public class FederationAccessUtils
{
	public static DataRetrievalResponse<?>[] performRequest( final FederationAccessManager fedAccessMgr,
	                                                         final LogicalOpRequest<?,?>... reqOps )
			  throws FederationAccessException
	{
		@SuppressWarnings("unchecked")
		final CompletableFuture<? extends DataRetrievalResponse<?>>[] futures = new CompletableFuture[reqOps.length];

		for ( int i = 0; i < reqOps.length; ++i ) {
			final DataRetrievalRequest req = reqOps[i].getRequest();
			final FederationMember fm = reqOps[i].getFederationMember();
			if ( fm instanceof SPARQLEndpoint ep && req instanceof SPARQLRequest sreq ) {
				futures[i] = fedAccessMgr.issueRequest(sreq, ep);
			}
			else if ( fm instanceof TPFServer tpf && req instanceof TriplePatternRequest tpreq ) {
				final TPFRequest reqTPF = ensureTPFRequest(tpreq);
				futures[i] = fedAccessMgr.issueRequest(reqTPF, tpf);
			}
			else if ( fm instanceof BRTPFServer brtpf && req instanceof TriplePatternRequest tpreq ) {
				final TPFRequest reqTPF = ensureTPFRequest(tpreq);
				futures[i] = fedAccessMgr.issueRequest(reqTPF, brtpf);
			}
			else if ( fm instanceof BRTPFServer brtpf && req instanceof BindingsRestrictedTriplePatternRequest brreq ) {
				final BRTPFRequest reqBRTPF = ensureBRTPFRequest(brreq);
				futures[i] = fedAccessMgr.issueRequest(reqBRTPF, brtpf);
			}
			else if ( fm instanceof Neo4jServer neo && req instanceof Neo4jRequest nreq ) {
				futures[i] = fedAccessMgr.issueRequest(nreq, neo);
			}
			else {
				throw new IllegalArgumentException("Unsupported combination of federation member (type: " + fm.getClass().getName() + ") and request type (" + req.getClass().getName() + ")");
			}
		}

		try {
			return CompletableFutureUtils.getAll(futures, DataRetrievalResponse.class);
		}
		catch ( final CompletableFutureUtils.GetAllException ex ) {
			if ( ex.getCause() != null && ex.getCause() instanceof InterruptedException ) {
				throw new FederationAccessException("Unexpected interruption when getting the response to a data retrieval request.", ex.getCause(), reqOps[ex.i].getRequest(), reqOps[ex.i].getFederationMember() );
			}
			else {
				throw new FederationAccessException("Getting the response to a data retrieval request caused an exception: " + ex.getMessage(), ex.getCause(), reqOps[ex.i].getRequest(), reqOps[ex.i].getFederationMember() );
			}
		}
	}

	public static CardinalityResponse[] performCardinalityRequests( final FederationAccessManager fedAccessMgr,
	                                                                final LogicalOpRequest<?,?>... reqOps )
					throws FederationAccessException
	{
		return performCardinalityRequests( fedAccessMgr, Arrays.asList(reqOps) );
	}

	public static CardinalityResponse[] performCardinalityRequests( final FederationAccessManager fedAccessMgr,
	                                                                final List<LogicalOpRequest<?,?>> reqOps )
					throws FederationAccessException
	{
		@SuppressWarnings("unchecked")
		final CompletableFuture<CardinalityResponse>[] futures = new CompletableFuture[reqOps.size()];

		for ( int i = 0; i < reqOps.size(); ++i ) {
			final LogicalOpRequest<?,?> reqOp = reqOps.get(i);
			final DataRetrievalRequest req = reqOp.getRequest();
			final FederationMember fm = reqOp.getFederationMember();
			if ( fm instanceof SPARQLEndpoint && req instanceof SPARQLRequest ) {
				futures[i] = fedAccessMgr.issueCardinalityRequest( (SPARQLRequest) req, (SPARQLEndpoint) fm );
			}
			else if ( fm instanceof TPFServer && req instanceof TriplePatternRequest ) {
				final TPFRequest reqTPF = ensureTPFRequest( (TriplePatternRequest) req );
				futures[i] = fedAccessMgr.issueCardinalityRequest( reqTPF, (TPFServer) fm );
			}
			else if ( fm instanceof BRTPFServer && req instanceof TriplePatternRequest ) {
				final TPFRequest reqTPF = ensureTPFRequest( (TriplePatternRequest) req );
				futures[i] = fedAccessMgr.issueCardinalityRequest( reqTPF, (BRTPFServer) fm );
			}
			else if ( fm instanceof BRTPFServer && req instanceof BindingsRestrictedTriplePatternRequest ) {
				final BRTPFRequest reqBRTPF = ensureBRTPFRequest( (BindingsRestrictedTriplePatternRequest) req );
				futures[i] = fedAccessMgr.issueCardinalityRequest( reqBRTPF, (BRTPFServer) fm );
			}
			else {
				throw new IllegalArgumentException("Unsupported combination of federation member (type: " + fm.getClass().getName() + ") and request type (" + req.getClass().getName() + ")");
			}

			if ( futures[i] == null ) {
				throw new FederationAccessException("Unexpected null returned by the federation access manager (i=" + i + ")", req, fm);
			}
		}

		try {
			return CompletableFutureUtils.getAll(futures, CardinalityResponse.class);
		}
		catch ( final CompletableFutureUtils.GetAllException ex ) {
			if ( ex.getCause() != null && ex.getCause() instanceof InterruptedException ) {
				throw new FederationAccessException("Unexpected interruption when getting the response to a cardinality retrieval request.", ex.getCause(), reqOps.get(ex.i).getRequest(), reqOps.get(ex.i).getFederationMember() );
			}
			else {
				throw new FederationAccessException("Getting the response to a cardinality retrieval request caused an exception: " + ex.getMessage(), ex.getCause(), reqOps.get(ex.i).getRequest(), reqOps.get(ex.i).getFederationMember() );
			}
		}
	}

	public static < ReqType extends DataRetrievalRequest,
	                RespType extends DataRetrievalResponse<?>,
	                MemberType extends FederationMember >
	RespType performRequest( final FederationAccessManager fedAccessMgr,
	                         final ReqType req,
	                         final MemberType fm ) throws FederationAccessException
	{
		final CompletableFuture<RespType> f = fedAccessMgr.issueRequest(req,fm);

		try {
			return f.get();
		}
		catch ( final InterruptedException e ) {
			throw new FederationAccessException("Unexpected interruption when getting the response to a data retrieval request.", e, req, fm);
		}
		catch ( final ExecutionException e ) {
			throw new FederationAccessException("Getting the response to a data retrieval request caused an exception.", e, req, fm);
		}
	}

    public static TPFRequest ensureTPFRequest( final TriplePatternRequest req ) {
        if ( req instanceof TPFRequest ) {
            return (TPFRequest) req;
        }
        else {
            return new TPFRequestImpl( req.getQueryPattern() );
        }
    }

    public static BRTPFRequest ensureBRTPFRequest( final BindingsRestrictedTriplePatternRequest req ) {
        if ( req instanceof BRTPFRequest ) {
            return (BRTPFRequest) req;
        }
        else {
            return new BRTPFRequestImpl( req.getTriplePattern(), req.getSolutionMappings() );
        }
    }

}
