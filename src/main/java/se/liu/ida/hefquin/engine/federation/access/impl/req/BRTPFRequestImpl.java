package se.liu.ida.hefquin.engine.federation.access.impl.req;

import java.util.Set;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.federation.access.BRTPFRequest;
import se.liu.ida.hefquin.engine.query.TriplePattern;

public class BRTPFRequestImpl extends BindingsRestrictedTriplePatternRequestImpl implements BRTPFRequest
{
	protected final String pageURL;

	public BRTPFRequestImpl( final TriplePattern tp, final Set<SolutionMapping> solMaps, final String pageURL ) {
		super(tp, solMaps);
		this.pageURL = pageURL;
	}

	public BRTPFRequestImpl( final TriplePattern tp, final Set<SolutionMapping> solMaps ) {
		this(tp, solMaps, null);
	}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof BRTPFRequest
				&& ((BRTPFRequest) o).getPageURL() == pageURL
				&& super.equals(o); 
	}

	@Override
	public String getPageURL() {
		return pageURL;
	}

}
