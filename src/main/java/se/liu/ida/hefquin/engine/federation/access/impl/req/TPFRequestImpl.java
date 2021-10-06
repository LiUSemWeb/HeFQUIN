package se.liu.ida.hefquin.engine.federation.access.impl.req;

import org.apache.jena.graph.Triple;

import se.liu.ida.hefquin.engine.federation.access.TPFRequest;
import se.liu.ida.hefquin.engine.query.TriplePattern;

public class TPFRequestImpl extends TriplePatternRequestImpl implements TPFRequest
{
	protected final int pageNumber;

	@SuppressWarnings("unused")
	public TPFRequestImpl( final TriplePattern tp, final int pageNumber ) {
		super(tp);
		this.pageNumber = pageNumber;

		if ( false ) {
			// check that the given triple pattern does not contain any blank nodes
			final Triple jenaTP = tp.asJenaTriple();
			assert ! jenaTP.getSubject().isBlank();
			assert ! jenaTP.getPredicate().isBlank();
			assert ! jenaTP.getObject().isBlank();
		}
	}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof TPFRequest
				&& ((TPFRequest) o).getPageNumber() == pageNumber
				&& super.equals(o);
	}

	@Override
	public int getPageNumber() {
		return pageNumber;
	}

}
