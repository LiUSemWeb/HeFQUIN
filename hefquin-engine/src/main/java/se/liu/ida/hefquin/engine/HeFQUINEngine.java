package se.liu.ida.hefquin.engine;

import java.io.PrintStream;
import java.util.List;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.resultset.ResultsFormat;

import se.liu.ida.hefquin.base.utils.Pair;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessStats;
import se.liu.ida.hefquin.engine.queryproc.QueryProcStats;

public interface HeFQUINEngine
{
	/**
	 * Call this one after the engine has been created.
	 */
	void integrateIntoJena();

	Pair<QueryProcStats, List<Exception>> executeQuery( Query query, ResultsFormat outputFormat, PrintStream output );

	default Pair<QueryProcStats, List<Exception>> executeQuery( Query query, ResultsFormat outputFormat ) {
		return executeQuery(query, outputFormat, System.out);
	}

	default Pair<QueryProcStats, List<Exception>> executeQuery( Query query, PrintStream output ) {
		return executeQuery(query, ResultsFormat.FMT_TEXT, output);
	}

	default Pair<QueryProcStats, List<Exception>> executeQuery( Query query ) {
		return executeQuery(query, ResultsFormat.FMT_TEXT);
	}

	FederationAccessStats getFederationAccessStats();
}
