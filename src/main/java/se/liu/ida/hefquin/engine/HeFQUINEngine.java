package se.liu.ida.hefquin.engine;

import java.io.PrintStream;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.resultset.ResultsFormat;

public interface HeFQUINEngine
{
	void executeQuery( Query query, ResultsFormat outputFormat, PrintStream output );

	default void executeQuery( Query query, ResultsFormat outputFormat ) {
		executeQuery(query, outputFormat, System.out);
	}

	default void executeQuery( Query query, PrintStream output ) {
		executeQuery(query, ResultsFormat.FMT_TEXT, output);
	}

	default void executeQuery( Query query ) {
		executeQuery(query, ResultsFormat.FMT_TEXT);
	}
}
