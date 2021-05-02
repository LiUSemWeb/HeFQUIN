package se.liu.ida.hefquin.engine.datastructures;

import java.util.Collection;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.engine.data.SolutionMapping;

/**
 * Implementations of this interface can be used as an index for
 * {@link SolutionMapping} objects.
 * 
 * In general, such an index may contain duplicates. That is, multiple
 * identical solution mappings may be added and should be possible to
 * retrieve. However, there may be implementations of this interface
 * that are explicitly restricted to index sets of solution mappings
 * (rather than multisets).
 */
public interface SolutionMappingsIndex extends Collection<SolutionMapping>
{
	/**
	 * Returns an iterator over all solution mappings in this index
	 * that are compatible with the given solution mappings.
	 *
	 * Implementations may choose to support this method only for specific
	 * types of solution mappings (e.g., with specific variables), in which
	 * case an {@link UnsupportedOperationException} will be thrown if the
	 * method is called with an unsupported variable.
	 */
	Iterable<SolutionMapping> getJoinPartners( SolutionMapping sm )
			throws UnsupportedOperationException;

	/**
	 * Returns an iterator over all solution mappings in this index
	 * that map the given variable to the given value.
	 *
	 * Implementations may choose to support this method only for specific
	 * variables, in which case an {@link UnsupportedOperationException}
	 * will be thrown if the method is called with an unsupported variable.
	 *
	 * The result of this method should essentially be the same as the result
	 * of calling {@link #getJoinPartners(SolutionMapping)} with a solution
	 * mapping that is defined only for the given variable and that maps this
	 * variable to the given value. 
	 */
	Iterable<SolutionMapping> findSolutionMappings( Var var, Node value )
			throws UnsupportedOperationException;

	/**
	 * Returns an iterator over all solution mappings in this
	 * index that map the first variable to the first value and
	 * the second variable to the second value.
	 *
	 * Hence, this method is a two-variables version of the
	 * method {@link #findSolutionMappings(Var, Node)}. 
	 */
	Iterable<SolutionMapping> findSolutionMappings( Var var1, Node value1,
	                                                Var var2, Node value2 )
			throws UnsupportedOperationException;

	/**
	 * This method is a three-variables version of the
	 * method {@link #findSolutionMappings(Var, Node)}. 
	 */
	Iterable<SolutionMapping> findSolutionMappings( Var var1, Node value1,
	                                                Var var2, Node value2,
	                                                Var var3, Node value3 )
			throws UnsupportedOperationException;
}
