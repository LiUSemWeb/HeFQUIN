package se.liu.ida.hefquin.engine;

import java.io.PrintStream;
import java.util.List;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.resultset.ResultsFormat;

import se.liu.ida.hefquin.engine.queryproc.QueryProcStats;
import se.liu.ida.hefquin.engine.utils.Pair;

public interface HeFQUINEngine
{
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
}
