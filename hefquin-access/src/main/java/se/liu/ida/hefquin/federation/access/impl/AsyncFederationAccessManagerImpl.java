package se.liu.ida.hefquin.federation.access.impl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.DataRetrievalResponse;
import se.liu.ida.hefquin.federation.access.FederationAccessException;
import se.liu.ida.hefquin.federation.access.impl.reqproc.BRTPFRequestProcessor;
import se.liu.ida.hefquin.federation.access.impl.reqproc.BRTPFRequestProcessorImpl;
import se.liu.ida.hefquin.federation.access.impl.reqproc.Neo4jRequestProcessor;
import se.liu.ida.hefquin.federation.access.impl.reqproc.Neo4jRequestProcessorImpl;
import se.liu.ida.hefquin.federation.access.impl.reqproc.SPARQLRequestProcessor;
import se.liu.ida.hefquin.federation.access.impl.reqproc.SPARQLRequestProcessorImpl;
import se.liu.ida.hefquin.federation.access.impl.reqproc.TPFRequestProcessor;
import se.liu.ida.hefquin.federation.access.impl.reqproc.TPFRequestProcessorImpl;

public class AsyncFederationAccessManagerImpl extends FederationAccessManagerBase2
{
	protected final ExecutorService threadPool;

	// stats
	protected AtomicLong issuedSPARQLRequests  = new AtomicLong(0L);
	protected AtomicLong issuedTPFRequests     = new AtomicLong(0L);
	protected AtomicLong issuedBRTPFRequests   = new AtomicLong(0L);
	protected AtomicLong issuedOtherRequests   = new AtomicLong(0L);
	protected AtomicLong completedSPARQLRequests  = new AtomicLong(0L);
	protected AtomicLong completedTPFRequests     = new AtomicLong(0L);
	protected AtomicLong completedBRTPFRequests   = new AtomicLong(0L);
	protected AtomicLong completedOtherRequests   = new AtomicLong(0L);

	public AsyncFederationAccessManagerImpl(
			final ExecutorService execService,
			final SPARQLRequestProcessor reqProcSPARQL,
			final TPFRequestProcessor reqProcTPF,
			final BRTPFRequestProcessor reqProcBRTPF,
			final Neo4jRequestProcessor reqProcNeo4j) {
		super(reqProcSPARQL, reqProcTPF, reqProcBRTPF, reqProcNeo4j);

		assert execService != null;
		threadPool = execService;
	}

	/**
	 * Creates an {@link AsyncFederationAccessManagerImpl} with a default configuration.
	 */
	public AsyncFederationAccessManagerImpl( final ExecutorService execService ) {
		this( execService,
		      new SPARQLRequestProcessorImpl(),
		      new TPFRequestProcessorImpl(),
		      new BRTPFRequestProcessorImpl(),
		      new Neo4jRequestProcessorImpl() );
	}

	@Override
	public < ReqType extends DataRetrievalRequest,
	         RespType extends DataRetrievalResponse<?>,
	         MemberType extends FederationMember >
	CompletableFuture<RespType> issueRequest( final ReqType req,
	                                          final MemberType fm )
			throws FederationAccessException {
		final RequestProcessor<ReqType, RespType, MemberType> reqProc = getReqProc(req, fm);

		final Supplier<RespType> supplier = new Supplier<RespType>() {
			@Override public RespType get() {
				final RespType resp;
				try {
					resp = reqProc.performRequest(req, fm);
				} catch ( final FederationAccessException e ) {
					throw new RuntimeException("Performing a request caused an exception.", e);
				}

				if ( resp == null ) {
					throw new RuntimeException("reqProc2 returned null");
				}

				// update the statistics
				if ( reqProc instanceof SPARQLRequestProcessor )
					completedSPARQLRequests.incrementAndGet();
				else if ( reqProc instanceof TPFRequestProcessor )
					completedTPFRequests.incrementAndGet();
				else if ( reqProc instanceof BRTPFRequestProcessor )
					completedBRTPFRequests.incrementAndGet();
				else
					completedOtherRequests.incrementAndGet();

				return resp;
			}
		};

		// update the statistics
		if ( reqProc instanceof SPARQLRequestProcessor )
			issuedSPARQLRequests.incrementAndGet();
		else if ( reqProc instanceof TPFRequestProcessor )
			issuedTPFRequests.incrementAndGet();
		else if ( reqProc instanceof BRTPFRequestProcessor )
			issuedBRTPFRequests.incrementAndGet();
		else
			issuedOtherRequests.incrementAndGet();

		return CompletableFuture.supplyAsync(supplier, threadPool);
	}

	@Override
	protected FederationAccessStatsImpl _getStats() {
		return new FederationAccessStatsImpl( issuedSPARQLRequests.get(),
		                                      issuedTPFRequests.get(),
		                                      issuedBRTPFRequests.get(),
		                                      issuedOtherRequests.get(),
		                                      completedSPARQLRequests.get(),
		                                      completedTPFRequests.get(),
		                                      completedBRTPFRequests.get(),
		                                      completedOtherRequests.get() );
	}

	@Override
	protected void _resetStats() {
		issuedSPARQLRequests.set(0L);
		issuedTPFRequests.set(0L);
		issuedBRTPFRequests.set(0L);
		issuedOtherRequests.set(0L);

		completedSPARQLRequests.set(0L);
		completedTPFRequests.set(0L);
		completedBRTPFRequests.set(0L);
		completedOtherRequests.set(0L);
	}

	/**
	 * Shuts down all thread pools associated with this federation access manager.
	 */
	@Override
	public void shutdown() {
		threadPool.shutdown();
		try {
			if ( ! threadPool.awaitTermination(500L, TimeUnit.MILLISECONDS) ) {
				threadPool.shutdownNow();
			}
		} catch ( InterruptedException ex ) {
			Thread.currentThread().interrupt();
			threadPool.shutdownNow();
		}
	}
}
