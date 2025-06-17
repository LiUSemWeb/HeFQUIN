package se.liu.ida.hefquin.engine.federation.access.impl.req;

import java.util.Set;

import org.apache.jena.sparql.engine.binding.Binding;

import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.engine.federation.access.BRTPFRequest;

public class BRTPFRequestImpl extends BindingsRestrictedTriplePatternRequestImpl implements BRTPFRequest
{
	protected final String pageURL;

	public BRTPFRequestImpl( final TriplePattern tp, final Set<Binding> solMaps, final String pageURL ) {
		super(tp, solMaps);
		this.pageURL = pageURL;
	}

	public BRTPFRequestImpl( final TriplePattern tp, final Set<Binding> solMaps ) {
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
