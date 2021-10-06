package se.liu.ida.hefquin.engine.federation.access.impl.req;

import java.util.Set;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.federation.access.BRTPFRequest;
import se.liu.ida.hefquin.engine.query.TriplePattern;

public class BRTPFRequestImpl extends BindingsRestrictedTriplePatternRequestImpl implements BRTPFRequest
{
	protected final int pageNumber;

	public BRTPFRequestImpl( final TriplePattern tp, final Set<SolutionMapping> solMaps, final int pageNumber ) {
		super(tp, solMaps);
		this.pageNumber = pageNumber;
	}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof BRTPFRequest
				&& ((BRTPFRequest) o).getPageNumber() == pageNumber
				&& super.equals(o); 
	}

	@Override
	public int getPageNumber() {
		return pageNumber;
	}

}
