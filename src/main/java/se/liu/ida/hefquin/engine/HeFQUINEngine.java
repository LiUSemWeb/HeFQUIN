package se.liu.ida.hefquin.engine;

import java.io.PrintStream;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.resultset.ResultsFormat;

import se.liu.ida.hefquin.engine.queryproc.QueryProcStats;

public interface HeFQUINEngine
{
	QueryProcStats executeQuery( Query query, ResultsFormat outputFormat, PrintStream output );

	default QueryProcStats executeQuery( Query query, ResultsFormat outputFormat ) {
		return executeQuery(query, outputFormat, System.out);
	}

	default QueryProcStats executeQuery( Query query, PrintStream output ) {
		return executeQuery(query, ResultsFormat.FMT_TEXT, output);
	}

	default QueryProcStats executeQuery( Query query ) {
		return executeQuery(query, ResultsFormat.FMT_TEXT);
	}
}
