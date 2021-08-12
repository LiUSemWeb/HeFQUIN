package se.liu.ida.hefquin.engine.federation.access.utils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.Neo4jServer;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.BRTPFRequest;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalResponse;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.access.Neo4jRequest;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.engine.federation.access.StringResponse;
import se.liu.ida.hefquin.engine.federation.access.TPFRequest;
import se.liu.ida.hefquin.engine.federation.access.TPFResponse;

public class FederationAccessUtils
{
	public static DataRetrievalResponse[] performRequest( final FederationAccessManager fedAccessMgr,
	                                                      final RequestMemberPair... pairs )
			  throws FederationAccessException
	{
		final CompletableFuture<?>[] futures = new CompletableFuture[pairs.length];
		for ( int i = 0; i < pairs.length; ++i ) {
			final DataRetrievalRequest req = pairs[i].getRequest();
			final FederationMember fm = pairs[i].getMember();
			if ( req instanceof SPARQLRequest && fm instanceof SPARQLEndpoint ) {
				futures[i] = fedAccessMgr.issueRequest( (SPARQLRequest) req, (SPARQLEndpoint) fm );
			}
			else if ( req instanceof TPFRequest && fm instanceof TPFServer ) {
				futures[i] = fedAccessMgr.issueRequest( (TPFRequest) req, (TPFServer) fm );
			}
			else if ( req instanceof TPFRequest && fm instanceof BRTPFServer ) {
				futures[i] = fedAccessMgr.issueRequest( (TPFRequest) req, (BRTPFServer) fm );
			}
			else if ( req instanceof BRTPFRequest && fm instanceof BRTPFServer ) {
				futures[i] = fedAccessMgr.issueRequest( (BRTPFRequest) req, (BRTPFServer) fm );
			}
			else if ( req instanceof Neo4jRequest && fm instanceof Neo4jServer ) {
				futures[i] = fedAccessMgr.issueRequest( (Neo4jRequest) req, (Neo4jServer) fm );
			}
			else {
				throw new IllegalArgumentException("Unsupported combination of federation member (type: " + fm.getClass().getName() + ") and request type (" + req.getClass().getName() + ")");
			}
		}

		final DataRetrievalResponse[] result = new DataRetrievalResponse[pairs.length];
		FederationAccessException ex = null;
		for ( int i = 0; i < pairs.length; ++i ) {
			if ( ex == null ) {
				try {
					result[i] = (DataRetrievalResponse) futures[i].get();
				}
				catch ( final InterruptedException e ) {
					ex = new FederationAccessException("Unexpected interruption when getting the response to a data retrieval request.", e, pairs[i].getRequest(), pairs[i].getMember() );
				}
				catch ( final ExecutionException e ) {
					ex = new FederationAccessException("Getting the response to a data retrieval request caused an exception.", e, pairs[i].getRequest(), pairs[i].getMember() );
				}
			}
			else {
				futures[i].cancel(true);
			}
		}

		if ( ex != null ) {
			throw ex;
		}

		return result;
	}

	public static SolMapsResponse performRequest( final FederationAccessManager fedAccessMgr,
	                                              final SPARQLRequest req,
	                                              final SPARQLEndpoint fm )
			  throws FederationAccessException
	{
		return getSolMapsResponse( fedAccessMgr.issueRequest(req,fm), req, fm );
	}

	public static TPFResponse performRequest( final FederationAccessManager fedAccessMgr,
	                                          final TPFRequest req,
	                                          final TPFServer fm )
			  throws FederationAccessException
	{
		return getTPFResponse( fedAccessMgr.issueRequest(req,fm), req, fm );
	}

	public static TPFResponse performRequest( final FederationAccessManager fedAccessMgr,
	                                          final TPFRequest req,
	                                          final BRTPFServer fm )
			  throws FederationAccessException
	{
		return getTPFResponse( fedAccessMgr.issueRequest(req,fm), req, fm );
	}

	public static TPFResponse performRequest( final FederationAccessManager fedAccessMgr,
	                                          final BRTPFRequest req,
	                                          final BRTPFServer fm )
			  throws FederationAccessException
	{
		return getTPFResponse( fedAccessMgr.issueRequest(req,fm), req, fm );
	}

	public static StringResponse performRequest( final FederationAccessManager fedAccessMgr,
	                                             final Neo4jRequest req,
	                                             final Neo4jServer fm )
			  throws FederationAccessException
	{
		return getStringResponse( fedAccessMgr.issueRequest(req,fm), req, fm );
	}

	protected static SolMapsResponse getSolMapsResponse( final CompletableFuture<SolMapsResponse> futureResp,
	                                                     final DataRetrievalRequest req,
	                                                     final FederationMember fm )
			  throws FederationAccessException
	{
		try {
			return futureResp.get();
		}
		catch ( final InterruptedException e ) {
			throw new FederationAccessException("Unexpected interruption when getting the response to a data retrieval request.", e, req, fm);
		}
		catch ( final ExecutionException e ) {
			throw new FederationAccessException("Getting the response to a data retrieval request caused an exception.", e, req, fm);
		}
	}

	protected static TPFResponse getTPFResponse( final CompletableFuture<TPFResponse> futureResp,
	                                             final DataRetrievalRequest req,
	                                             final FederationMember fm )
			  throws FederationAccessException
	{
		try {
			return futureResp.get();
		}
		catch ( final InterruptedException e ) {
			throw new FederationAccessException("Unexpected interruption when getting the response to a data retrieval request.", e, req, fm);
		}
		catch ( final ExecutionException e ) {
			throw new FederationAccessException("Getting the response to a data retrieval request caused an exception.", e, req, fm);
		}
	}

	protected static StringResponse getStringResponse( final CompletableFuture<StringResponse> futureResp,
	                                                   final DataRetrievalRequest req,
	                                                   final FederationMember fm )
			  throws FederationAccessException
	{
		try {
			return futureResp.get();
		}
		catch ( final InterruptedException e ) {
			throw new FederationAccessException("Unexpected interruption when getting the response to a data retrieval request.", e, req, fm);
		}
		catch ( final ExecutionException e ) {
			throw new FederationAccessException("Getting the response to a data retrieval request caused an exception.", e, req, fm);
		}
	}

}
