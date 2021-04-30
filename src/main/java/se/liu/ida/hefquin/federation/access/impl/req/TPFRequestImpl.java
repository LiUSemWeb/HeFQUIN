package se.liu.ida.hefquin.federation.access.impl.req;

import se.liu.ida.hefquin.federation.access.TPFRequest;
import se.liu.ida.hefquin.query.TriplePattern;

public class TPFRequestImpl extends TriplePatternRequestImpl implements TPFRequest
{
	protected final int pageNumber;

	public TPFRequestImpl( final TriplePattern tp, final int pageNumber ) {
		super(tp);
		this.pageNumber = pageNumber;
	}

	@Override
	public int getPageNumber() {
		return pageNumber;
	}

}
