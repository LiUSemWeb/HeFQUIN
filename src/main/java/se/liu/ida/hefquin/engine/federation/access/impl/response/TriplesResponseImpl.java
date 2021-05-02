package se.liu.ida.hefquin.engine.federation.access.impl.response;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import se.liu.ida.hefquin.engine.data.Triple;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.TriplesResponse;

public class TriplesResponseImpl
                            extends DataRetrievalResponseBase
                            implements TriplesResponse
{
	protected final List<Triple> triples;

	/**
	 * Initializes the retrievalEndTime to the time when this object is created.
	 */
	public TriplesResponseImpl( final List<Triple> triples,
	                            final FederationMember fm,
	                            final DataRetrievalRequest request,
	                            final Date requestStartTime ) {
		super(fm, request, requestStartTime);

		assert triples != null;
		this.triples = triples;
	}

	public TriplesResponseImpl( final List<Triple> triples,
	                            final FederationMember fm,
	                            final DataRetrievalRequest request,
	                            final Date requestStartTime,
	                            final Date retrievalEndTime ) {
		super(fm, request, requestStartTime, retrievalEndTime);

		assert triples != null;
		this.triples = triples;
	}

	@Override
	public Iterator<Triple> getIterator() {
		return triples.iterator();
	}

	@Override
	public int getSize() {
		return triples.size();
	}

}
