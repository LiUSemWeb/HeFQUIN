package se.liu.ida.hefquin.base.datastructures.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.base.data.SolutionMapping;

/**
 * This is a special implementation of {@link SolutionMappingsIndex} that
 * can be used for cases in which ALL indexed solution mappings should be
 * returned as join partners. This is useful for joins between subpatterns
 * that do not have any variable in common (i.e., no join variables).
 */
public class SolutionMappingsIndexNoJoinVars extends SolutionMappingsIndexBase
{
	protected final List<SolutionMapping> indexedSolMaps = new ArrayList<>();

	@Override
	public boolean add( final SolutionMapping sm ) {
		return indexedSolMaps.add(sm);
	}

	@Override
	public Iterable<SolutionMapping> getJoinPartners( final SolutionMapping sm ) throws UnsupportedOperationException {
		return indexedSolMaps;
	}

	@Override
	public Iterable<SolutionMapping> findSolutionMappings( final Var var, final Node value ) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterable<SolutionMapping> findSolutionMappings( final Var var1, final Node value1,
	                                                       final Var var2, final Node value2 ) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterable<SolutionMapping> findSolutionMappings( final Var var1, final Node value1,
	                                                       final Var var2, final Node value2,
	                                                       final Var var3, final Node value3 ) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterable<SolutionMapping> getAllSolutionMappings() {
		return indexedSolMaps;
	}

	@Override
	public void clear() {
		indexedSolMaps.clear();
	}

	@Override
	public boolean contains( final Object obj ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isEmpty() {
		return indexedSolMaps.isEmpty();
	}

	@Override
	public int size() {
		return indexedSolMaps.size();
	}

}
