package se.liu.ida.hefquin.federation.access;

import java.util.Iterator;

import se.liu.ida.hefquin.data.SolutionMapping;

public interface SolMapsResponse extends DataRetrievalResponse
{
	Iterator<SolutionMapping> getIterator();
}
