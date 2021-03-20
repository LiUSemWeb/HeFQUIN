package se.liu.ida.hefquin.federation.access.impl.response;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import se.liu.ida.hefquin.data.SolutionMapping;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.SolMapsResponse;

public class SolMapsResponseImpl
                      extends DataRetrievalResponseBase 
                      implements SolMapsResponse
{
	protected List<SolutionMapping> solMaps;

	public SolMapsResponseImpl( final List<SolutionMapping> solMaps,
	                            final FederationMember fm ) {
		super(fm);

		assert solMaps != null;
		this.solMaps = solMaps;
	}

	public SolMapsResponseImpl( final List<SolutionMapping> solMaps,
	                            final FederationMember fm,
	                            final Date retrievalTime ) {
		super(fm, retrievalTime);

		assert solMaps != null;
		this.solMaps = solMaps;
	}

	public Iterator<SolutionMapping> getIterator() {
		return solMaps.iterator();
	}

}
