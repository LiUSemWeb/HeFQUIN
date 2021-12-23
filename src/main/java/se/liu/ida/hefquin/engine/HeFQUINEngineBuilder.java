package se.liu.ida.hefquin.engine;

import java.io.PrintStream;

import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.engine.main.QC;
import org.apache.jena.sparql.resultset.ResultsFormat;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.QueryExecUtils;

import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.BRTPFRequest;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.access.TPFRequest;
import se.liu.ida.hefquin.engine.federation.access.TPFResponse;
import se.liu.ida.hefquin.engine.federation.access.impl.AsyncFederationAccessManagerImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.FederationAccessManagerWithCache;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.BRTPFRequestProcessor;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.Neo4jRequestProcessor;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.Neo4jRequestProcessorImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.SPARQLRequestProcessor;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.SPARQLRequestProcessorImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.TPFRequestProcessor;
import se.liu.ida.hefquin.engine.federation.catalog.FederationCatalog;
import se.liu.ida.hefquin.engine.queryproc.QueryProcStats;
import se.liu.ida.hefquin.jenaintegration.sparql.HeFQUINConstants;
import se.liu.ida.hefquin.jenaintegration.sparql.engine.main.OpExecutorHeFQUIN;

public class HeFQUINEngineBuilder
{
	protected HeFQUINEngineConfig config             = null;
	protected FederationCatalog fedCatalog           = null;
	protected FederationAccessManager fedAccessMgr   = null;

	/**
	 * mandatory
	 */
	public HeFQUINEngineBuilder setConfiguration( final HeFQUINEngineConfig config ) {
		if ( config == null )
			throw new IllegalArgumentException();

		this.config = config;
		return this;
	}

	/**
	 * mandatory
	 */
	public HeFQUINEngineBuilder setFederationCatalog( final FederationCatalog fedCatalog ) {
		if ( fedCatalog == null )
			throw new IllegalArgumentException();

		this.fedCatalog = fedCatalog;
		return this;
	}

	/**
	 * optional
	 */
	public HeFQUINEngineBuilder setFederationAccessManager( final FederationAccessManager fedAccessMgr ) {
		this.fedAccessMgr = fedAccessMgr;
		return this;
	}

	public HeFQUINEngine build() {
		if ( config == null )
			throw new IllegalStateException("no HeFQUINEngineConfig specified");

		if ( fedCatalog == null )
			throw new IllegalStateException("no FederationCatalog specified");

		if ( fedAccessMgr == null )
			setDefaultFederationAccessManager();

		final Context ctxt = ARQ.getContext();
		QC.setFactory( ctxt, OpExecutorHeFQUIN.factory );

		config.initializeContext(ctxt);

		ctxt.set( HeFQUINConstants.sysFederationCatalog, fedCatalog );
		ctxt.set( HeFQUINConstants.sysFederationAccessManager, fedAccessMgr );

		return new MyEngine();
	}


	protected void setDefaultFederationAccessManager() {
		final SPARQLRequestProcessor reqProcSPARQL = new SPARQLRequestProcessorImpl();
		// TODO: replace the following once we have an implementation of TPFRequestProcessor
		final TPFRequestProcessor reqProcTPF = new TPFRequestProcessor() {
			@Override public TPFResponse performRequest(TPFRequest req, TPFServer fm) { throw new UnsupportedOperationException(); }
			@Override public TPFResponse performRequest(TPFRequest req, BRTPFServer fm) { throw new UnsupportedOperationException(); }
		};
		// TODO: replace the following once we have an implementation of BRTPFRequestProcessor
		final BRTPFRequestProcessor reqProcBRTPF = new BRTPFRequestProcessor() {
			@Override public TPFResponse performRequest(BRTPFRequest req, BRTPFServer fm) { throw new UnsupportedOperationException(); }
		};

		final Neo4jRequestProcessor reqProcNeo4j = new Neo4jRequestProcessorImpl();

		final FederationAccessManager fedAccessMgrWithoutCache = new AsyncFederationAccessManagerImpl(reqProcSPARQL, reqProcTPF, reqProcBRTPF, reqProcNeo4j);
		final FederationAccessManager fedAccessMgrWithCache = new FederationAccessManagerWithCache(fedAccessMgrWithoutCache, 100);
		this.fedAccessMgr = fedAccessMgrWithCache;
	}

	protected static class MyEngine implements HeFQUINEngine {
		@Override
		public QueryProcStats executeQuery( final Query query, final ResultsFormat outputFormat, final PrintStream output ) {
			final DatasetGraph dsg = DatasetGraphFactory.createGeneral();
			final QueryExecution qe = QueryExecutionFactory.create(query, dsg);
			QueryExecUtils.executeQuery(query, qe, outputFormat, output);

			return (QueryProcStats) qe.getContext().get( HeFQUINConstants.sysQueryProcStats );
		}
		
	}

}
