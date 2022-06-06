package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.utils;

import org.apache.jena.graph.Triple;

import se.liu.ida.hefquin.engine.query.TriplePattern;

/**
 * Wrapper class for a TriplePattern that includes an integer id
 */
public class TriplePatternWithID implements TriplePattern
{
    protected final int id;
    protected final TriplePattern tp;

    public TriplePatternWithID(final int id, final TriplePattern tp) {
        assert tp != null;

        this.id = id;
        this.tp = tp;
    }

    public int getId() {
        return id;
    }

	@Override
	public Triple asJenaTriple() {
		return tp.asJenaTriple();
	}

	@Override
	public int numberOfVars() {
		return tp.numberOfVars();
	}
}
