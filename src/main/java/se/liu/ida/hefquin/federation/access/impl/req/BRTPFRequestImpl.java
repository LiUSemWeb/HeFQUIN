package se.liu.ida.hefquin.federation.access.impl.req;

import java.util.Set;

import se.liu.ida.hefquin.data.SolutionMapping;
import se.liu.ida.hefquin.federation.access.BRTPFRequest;
import se.liu.ida.hefquin.query.TriplePattern;

public class BRTPFRequestImpl extends BindingsRestrictedTriplePatternRequestImpl implements BRTPFRequest
{
	protected final int pageNumber;

	public BRTPFRequestImpl( final TriplePattern tp, final Set<SolutionMapping> solMaps, final int pageNumber ) {
		super(tp, solMaps);
		this.pageNumber = pageNumber;
	}

	@Override
	public int getPageNumber() {
		return pageNumber;
	}

}
