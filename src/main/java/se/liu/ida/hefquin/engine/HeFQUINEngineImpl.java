package se.liu.ida.hefquin.engine;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.resultset.ResultsFormat;
import org.apache.jena.sparql.util.QueryExecUtils;

import se.liu.ida.hefquin.engine.queryproc.QueryProcStats;
import se.liu.ida.hefquin.engine.utils.Pair;
import se.liu.ida.hefquin.jenaintegration.sparql.HeFQUINConstants;

public class HeFQUINEngineImpl implements HeFQUINEngine
{
	@Override
	public Pair<QueryProcStats, List<Exception>> executeQuery( final Query query,
	                                                           final ResultsFormat outputFormat,
	                                                           final PrintStream output ) {
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
