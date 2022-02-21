package se.liu.ida.hefquin.engine.data;

import java.util.Set;

import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.TriplePattern;


public interface VocabularyMapping {

	/**
	 * Applies this vocabulary mapping to the given triple pattern and
	 * returns the resulting triple patterns. If this mapping is not relevant
	 * for the given triple pattern (i.e., applying the mapping to the given
	 * triple pattern does not have any effect), then the result of this
	 * function is a singleton set that simply contains the given triple
	 * pattern.
	 */
	Set<SPARQLGraphPattern> translateTriplePattern( TriplePattern tp );
}
