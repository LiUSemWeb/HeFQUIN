package se.liu.ida.hefquin.engine.federation.access.impl.response;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.SolMapsResponse;

public class SolMapsResponseImpl
                      extends DataRetrievalResponseBase 
                      implements SolMapsResponse
{
	protected final List<SolutionMapping> solMaps;

	/**
	 * Initializes the retrievalEndTime to the time when this object is created.
	 */
	public SolMapsResponseImpl( final List<SolutionMapping> solMaps,
	                            final FederationMember fm,
	                            final DataRetrievalRequest request,
	                            final Date requestStartTime ) {
		super(fm, request, requestStartTime);

		assert solMaps != null;
		this.solMaps = solMaps;
	}

	public SolMapsResponseImpl( final List<SolutionMapping> solMaps,
	                            final FederationMember fm,
	                            final DataRetrievalRequest request,
	                            final Date requestStartTime,
	                            final Date retrievalEndTime ) {
		super(fm, request, requestStartTime, retrievalEndTime);

		assert solMaps != null;
		this.solMaps = solMaps;
	}

	@Override
	public Iterator<SolutionMapping> getIterator() {
		return solMaps.iterator();
	}

	@Override
	public int getSize() {
		return solMaps.size();
	}

}
