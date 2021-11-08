package se.liu.ida.hefquin.engine.federation.access.impl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.Neo4jServer;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.*;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.BRTPFRequestProcessor;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.Neo4jRequestProcessor;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.SPARQLRequestProcessor;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.TPFRequestProcessor;

public class AsyncFederationAccessManagerImpl extends FederationAccessManagerBase2
{
	public static int DEFAULT_THREAD_POOL_SIZE = 10;

	protected final ExecutorService threadPool;

	public AsyncFederationAccessManagerImpl(
			final SPARQLRequestProcessor reqProcSPARQL,
			final TPFRequestProcessor reqProcTPF,
			final BRTPFRequestProcessor reqProcBRTPF,
			final Neo4jRequestProcessor reqProcNeo4j) {
		this(DEFAULT_THREAD_POOL_SIZE, reqProcSPARQL, reqProcTPF, reqProcBRTPF, reqProcNeo4j);
	}

	public AsyncFederationAccessManagerImpl(
			final int threadPoolSize,
			final SPARQLRequestProcessor reqProcSPARQL,
			final TPFRequestProcessor reqProcTPF,
			final BRTPFRequestProcessor reqProcBRTPF,
			final Neo4jRequestProcessor reqProcNeo4j) {
		super(reqProcSPARQL, reqProcTPF, reqProcBRTPF, reqProcNeo4j);

		threadPool = Executors.newFixedThreadPool(threadPoolSize);
	}

	@Override
	public CompletableFuture<SolMapsResponse> issueRequest( final SPARQLRequest req, final SPARQLEndpoint fm )
	{
		return CompletableFuture.supplyAsync( createSupplier(req,fm), threadPool );
	}

	@Override
	public CompletableFuture<TPFResponse> issueRequest( final TPFRequest req, final TPFServer fm )
	{
		return CompletableFuture.supplyAsync( createSupplier(req,fm), threadPool );
	}

	@Override
	public CompletableFuture<TPFResponse> issueRequest( final TPFRequest req, final BRTPFServer fm )
	{
		return CompletableFuture.supplyAsync( createSupplier(req,fm), threadPool );
	}

	@Override
	public CompletableFuture<TPFResponse> issueRequest( final BRTPFRequest req, final BRTPFServer fm )
	{
		return CompletableFuture.supplyAsync( createSupplier(req,fm), threadPool );
	}

	@Override
	public CompletableFuture<RecordsResponse> issueRequest(final Neo4jRequest req, final Neo4jServer fm )
	{
		return CompletableFuture.supplyAsync( createSupplier(req,fm), threadPool );
	}


	protected Supplier<SolMapsResponse> createSupplier( final SPARQLRequest req,
	                                                    final SPARQLEndpoint fm )
	{
		return new Supplier<SolMapsResponse>() {
			@Override public SolMapsResponse get() {
				try {
					return reqProcSPARQL.performRequest(req, fm);
				} catch ( final FederationAccessException e ) {
					throw new RuntimeException("Performing a request caused an exception.", e);
				}
			}
		};
	}

	protected Supplier<TPFResponse> createSupplier( final TPFRequest req,
	                                                final TPFServer fm )
	{
		return new Supplier<TPFResponse>() {
			@Override public TPFResponse get() {
				try {
					return reqProcTPF.performRequest(req, fm);
				} catch ( final FederationAccessException e ) {
					throw new RuntimeException("Performing a request caused an exception.", e);
				}
			}
		};
	}

	protected Supplier<TPFResponse> createSupplier( final TPFRequest req,
	                                                final BRTPFServer fm )
	{
		return new Supplier<TPFResponse>() {
			@Override public TPFResponse get() {
				try {
					return reqProcTPF.performRequest(req, fm);
				} catch ( final FederationAccessException e ) {
					throw new RuntimeException("Performing a request caused an exception.", e);
				}
			}
		};
	}

	protected Supplier<TPFResponse> createSupplier( final BRTPFRequest req,
	                                                final BRTPFServer fm )
	{
		return new Supplier<TPFResponse>() {
			@Override public TPFResponse get() {
				try {
					return reqProcBRTPF.performRequest(req, fm);
				} catch ( final FederationAccessException e ) {
					throw new RuntimeException("Performing a request caused an exception.", e);
				}
			}
		};
	}

	protected Supplier<RecordsResponse> createSupplier( final Neo4jRequest req,
	                                                   final Neo4jServer fm )
	{
		return () -> {
			try {
				return reqProcNeo4j.performRequest(req, fm);
			} catch ( final FederationAccessException e ) {
				throw new RuntimeException("Performing a request caused an exception.", e);
			}
		};
	}

}
