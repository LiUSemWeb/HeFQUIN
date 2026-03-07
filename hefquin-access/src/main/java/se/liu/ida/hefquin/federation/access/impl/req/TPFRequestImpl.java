package se.liu.ida.hefquin.federation.access.impl.req;

import org.apache.jena.graph.Triple;

import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.federation.access.TPFRequest;

public class TPFRequestImpl extends TriplePatternRequestImpl implements TPFRequest
{
	protected final String pageURL;

	@SuppressWarnings("unused")
	public TPFRequestImpl( final TriplePattern tp, final String pageURL ) {
		super(tp);
		this.pageURL = pageURL;

		if ( false ) {
			// check that the given triple pattern does not contain any blank nodes
			final Triple jenaTP = tp.asJenaTriple();
			assert ! jenaTP.getSubject().isBlank();
			assert ! jenaTP.getPredicate().isBlank();
			assert ! jenaTP.getObject().isBlank();
		}
	}

	public TPFRequestImpl( final TriplePattern tp ) {
		this(tp, null);
	}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof TPFRequest
				&& ((TPFRequest) o).getPageURL() == pageURL
				&& super.equals(o);
	}

	@Override
	public String getPageURL() {
		return pageURL;
	}

}
