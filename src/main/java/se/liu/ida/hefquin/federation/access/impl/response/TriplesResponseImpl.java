package se.liu.ida.hefquin.federation.access.impl.response;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import se.liu.ida.hefquin.data.Triple;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.TriplesResponse;

public class TriplesResponseImpl
extends DataRetrievalResponseBase 
implements TriplesResponse
{
	protected List<Triple> triples;

	public TriplesResponseImpl( final List<Triple> triples,
	                            final FederationMember fm ) {
		super(fm);

		assert triples != null;
		this.triples = triples;
	}

	public TriplesResponseImpl( final List<Triple> triples,
	                            final FederationMember fm,
	                            final Date retrievalTime ) {
		super(fm, retrievalTime);

		assert triples != null;
		this.triples = triples;
	}

	public Iterator<Triple> getIterator() {
		return triples.iterator();
	}

}
