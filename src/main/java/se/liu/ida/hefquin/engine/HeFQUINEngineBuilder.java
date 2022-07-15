package se.liu.ida.hefquin.engine;

import java.io.PrintStream;
import java.util.concurrent.ExecutorService;

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

import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.access.utils.FederationAccessUtils;
import se.liu.ida.hefquin.engine.federation.catalog.FederationCatalog;
import se.liu.ida.hefquin.engine.queryproc.QueryProcStats;
import se.liu.ida.hefquin.jenaintegration.sparql.HeFQUINConstants;
import se.liu.ida.hefquin.jenaintegration.sparql.engine.main.OpExecutorHeFQUIN;

public class HeFQUINEngineBuilder
{
	protected HeFQUINEngineConfig config               = null;
	protected FederationCatalog fedCatalog             = null;
	protected FederationAccessManager fedAccessMgr     = null;
	protected ExecutorService execServiceForPlanTasks  = null;

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

	/**
	 * mandatory
	 */
	public HeFQUINEngineBuilder setExecutorServiceForPlanTasks( final ExecutorService es ) {
		if ( es == null )
			throw new IllegalArgumentException();

		this.execServiceForPlanTasks = es;
		return this;
	}

	public HeFQUINEngine build() {
		if ( config == null )
			throw new IllegalStateException("no HeFQUINEngineConfig specified");

		if ( fedCatalog == null )
			throw new IllegalStateException("no FederationCatalog specified");

		if ( fedAccessMgr == null )
			setDefaultFederationAccessManager();

		if ( execServiceForPlanTasks == null )
			throw new IllegalStateException("no ExecutorService for plan tasks specified");

		final Context ctxt = ARQ.getContext();
		QC.setFactory( ctxt, OpExecutorHeFQUIN.factory );

		config.initializeContext(ctxt);

		ctxt.set( HeFQUINConstants.sysFederationCatalog, fedCatalog );
		ctxt.set( HeFQUINConstants.sysFederationAccessManager, fedAccessMgr );
		ctxt.set( HeFQUINConstants.sysIsExperimentRun, true );
		ctxt.set( HeFQUINConstants.sysExecServiceForPlanTasks, execServiceForPlanTasks );
		return new MyEngine();
	}


	protected void setDefaultFederationAccessManager() {
		this.fedAccessMgr = FederationAccessUtils.getDefaultFederationAccessManager();
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
