package se.liu.ida.hefquin.engine;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.main.OpExecutor;
import org.apache.jena.sparql.engine.main.OpExecutorFactory;
import org.apache.jena.sparql.engine.main.QC;
import org.apache.jena.sparql.resultset.ResultsFormat;
import org.apache.jena.sparql.util.QueryExecUtils;

import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessStats;
import se.liu.ida.hefquin.engine.queryproc.QueryProcStats;
import se.liu.ida.hefquin.engine.queryproc.QueryProcessor;
import se.liu.ida.hefquin.engine.utils.Pair;
import se.liu.ida.hefquin.jenaintegration.sparql.HeFQUINConstants;
import se.liu.ida.hefquin.jenaintegration.sparql.engine.main.OpExecutorHeFQUIN;

public class HeFQUINEngineImpl implements HeFQUINEngine
{
	protected final FederationAccessManager fedAccessMgr;
	protected final QueryProcessor qProc;

	public HeFQUINEngineImpl( final FederationAccessManager fedAccessMgr,
	                          final QueryProcessor qProc ) {
		assert fedAccessMgr != null;
		assert qProc != null;

		this.fedAccessMgr = fedAccessMgr;
		this.qProc = qProc;
	}

	@Override
	public void integrateIntoJena() {
		final OpExecutorFactory factory = new OpExecutorFactory() {
			@Override
			public OpExecutor create( final ExecutionContext execCxt ) {
				return new OpExecutorHeFQUIN(qProc, execCxt);
			}
		};

		QC.setFactory( ARQ.getContext(), factory );
	}

	@Override
	public FederationAccessStats getFederationAccessStats() {
		return fedAccessMgr.getStats();
	}

	@Override
	public Pair<QueryProcStats, List<Exception>> executeQuery( final Query query,
	                                                           final ResultsFormat outputFormat,
	                                                           final PrintStream output ) {
		ValuesServiceQueryResolver.expandValuesPlusServicePattern(query);

		final DatasetGraph dsg = DatasetGraphFactory.createGeneral();
		final QueryExecution qe = QueryExecutionFactory.create(query, dsg);

		Exception ex = null;
		try {
			if ( query.isSelectType() )
				executeSelectQuery(qe, outputFormat, output);
			else
				QueryExecUtils.executeQuery(query.getPrologue(), qe, outputFormat, output);
		}
		catch ( final Exception e ) {
			ex = e;
		}

		final QueryProcStats stats = (QueryProcStats) qe.getContext().get(HeFQUINConstants.sysQueryProcStats);

		@SuppressWarnings("unchecked")
		List<Exception> exceptions = (List<Exception>) qe.getContext().get(HeFQUINConstants.sysQueryProcExceptions);
		if ( ex != null ) {
			if ( exceptions == null ) {
				exceptions = new ArrayList<>();
			}
			exceptions.add(ex);
		}

		return new Pair<>(stats, exceptions);
	}

	protected void executeSelectQuery( final QueryExecution qe,
	                                   final ResultsFormat outputFormat,
	                                   final PrintStream output ) throws Exception {
		final ResultSet rs;
		try {
			rs = qe.execSelect();
		}
		catch ( final Exception e ) {
			throw new Exception("Exception occurred when executing a SELECT query using the Jena machinery.", e);
		}

		try {
			QueryExecUtils.outputResultSet(rs, qe.getQuery().getPrologue(), outputFormat, output);
		}
		catch ( final Exception e ) {
			throw new Exception("Exception occurred when outputting the result of a SELECT query using the Jena machinery.", e);
		}
	}

	protected void executeNonSelectQuery( final QueryExecution qe,
	                                      final ResultsFormat outputFormat,
	                                      final PrintStream output ) throws Exception {
		try {
			QueryExecUtils.executeQuery( qe.getQuery().getPrologue(), qe, outputFormat, output );
		}
		catch ( final Exception e ) {
			throw new Exception("Exception occurred when executing an ASK/DESCRIBE/CONSTRUCT query using the Jena machinery.", e);
		}
	}

}
