package se.liu.ida.hefquin.engine.datastructures.impl;

import java.util.*;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

import org.apache.jena.sparql.engine.binding.Binding;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.utils.FilteringIteratorForSolMaps_OneVarBinding;
import se.liu.ida.hefquin.engine.data.utils.FilteringIteratorForSolMaps_TwoVarsBindings;
import se.liu.ida.hefquin.engine.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.data.utils.SolutionMappingsIterableOverCollectionOfLists;

/**
 * This is a hash table based implementation of {@link SolutionMappingsIndex}
 * that can be used for indexes that are meant to be used for cases in which
 * adding and probing into the index may not happen concurrently.
 *
 * As mentioned above, this implementation assumes that adding and probing
 * into the index may not happen concurrently. Based on this assumption,
 * for every method that returns an {@link Iterable} of solution mappings,
 * the {@link Iterable} that it returns is directly based on an internal
 * data structure (rather than being a new {@link Iterable} into which the  
 * solution mappings have been copied). For cases in which the assumption
 * does not hold (i.e., cases in which adding and probing into the index
 * may actually happen concurrently), an object of this class may simply
 * be wrapped in a {@link SolutionMappingsIndexForMixedUsage} which then
 * creates a new {@link Iterable} for every {@link Iterable} returned by
 * this implementation. 
 *
 * Another assumption of this implementation is that the only variables
 * relevant for index look-ups are the variables on which the index is built.
 * In other words, the assumption is that the only variables that the solution
 * mappings added to the index have in common with the solution mappings
 * given to the method {@link #getJoinPartners(SolutionMapping)} are the
 * variables on which the index is built. For cases in which this assumption
 * does not hold, a {@link SolutionMappingsIndexWithPostMatching} can be
 * used to wrap a {@link SolutionMappingsHashTable}.
 *
 * This implementation is generic in the sense that it can be used for an index
 * on an arbitrary number of variables. As a consequence, it is not the most
 * efficient implementation for cases in which the possible number of variables
 * is fix. More efficient alternatives for the cases in which the number of
 * variables is one or two are {@link SolutionMappingsHashTableBasedOnOneVar}
 * and {@link SolutionMappingsHashTableBasedOnTwoVars}, respectively. 
 */
public class SolutionMappingsHashTable extends SolutionMappingsIndexBase
{
	// Having List<Node> as key type for the hash table is probably
	// not the best choice in terms of efficiency. However, it will have
	// to do for the moment.
	// TODO: can this be made more efficient?
	protected final Map<List<Node>, List<SolutionMapping>> map = new HashMap<>();
	protected final List<Var> joinVariables;

	public SolutionMappingsHashTable( final List<Var> joinVariables ){
		assert ! joinVariables.isEmpty();
		this.joinVariables = joinVariables;
	}

	public SolutionMappingsHashTable( final Var ... vars ) {
		this( Arrays.asList(vars) );
	}

	public SolutionMappingsHashTable( final Set<Var> joinVariables ) {
		this( new ArrayList<>(joinVariables) );
	}

	@Override
	public int size() {
		int size = 0;
		final Iterator<List<SolutionMapping>> li = map.values().iterator();
		while( li.hasNext() ){
			size = size + li.next().size();
		}
		return size;
	}

	@Override
	public boolean isEmpty() {
		for ( final List<SolutionMapping> l : map.values() ) {
			if ( ! l.isEmpty() ) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean contains( final Object o ) {
        if( o instanceof SolutionMapping ){
            final SolutionMapping sm = (SolutionMapping) o;
            for ( final SolutionMapping sm2 : getJoinPartners(sm)) {
                if ( sm == sm2 || SolutionMappingUtils.equals(sm, sm2) )
                    return true;
            }
        }

		return false;
	}

	@Override
	public Iterable<SolutionMapping> getAllSolutionMappings() {
		return new SolutionMappingsIterableOverCollectionOfLists( map.values() );
	}

	@Override
	public boolean add( final SolutionMapping e ) {
		final List<Node> valKeys = getVarKeys(e);

		if ( valKeys != null ){
			List<SolutionMapping> solMapList = map.get(valKeys);
			if ( solMapList == null) {
				solMapList = new ArrayList<>();
				map.put(valKeys, solMapList);
			}
			solMapList.add(e);
			return true;
		}
		else {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public void clear() {
		for ( final List<SolutionMapping> l : map.values() ) {
			l.clear();
		}
		map.clear();
	}

	@Override
	public Iterable<SolutionMapping> getJoinPartners( final SolutionMapping sm )
	{
		final List<Node> valKeys = getVarKeys(sm);
		if ( valKeys == null ){
			return getAllSolutionMappings();
		}
		else {
			return returnLookupResultOrEmptyList(valKeys);
		}
	}

	@Override
	public Iterable<SolutionMapping> findSolutionMappings( final Var var, final Node value )
	{
		if ( ! joinVariables.contains(var) ) {
			return getAllSolutionMappings();
		}
		else if ( joinVariables.size() > 1 ) {
			return findSolutionMappingsLastResort(var, value);
		}
		else {
			final List<Node> valKeyL = Arrays.asList(value);
			return returnLookupResultOrEmptyList(valKeyL);
		}
	}

	@Override
	public Iterable<SolutionMapping> findSolutionMappings(
			final Var var1, final Node value1,
			final Var var2, final Node value2 )
	{
		if ( joinVariables.size() == 1 ) {
			if ( joinVariables.contains(var1) ) {
				final Iterable<SolutionMapping> it = findSolutionMappings(var1, value1);
				return FilteringIteratorForSolMaps_OneVarBinding.createAsIterable(it, var2, value2);
			}
			else if ( joinVariables.contains(var2) ) {
				final Iterable<SolutionMapping> it = findSolutionMappings(var2, value2);
				return FilteringIteratorForSolMaps_OneVarBinding.createAsIterable(it, var1, value1);
			}
			else {
				return findSolutionMappingsLastResort(var1, value1, var2, value2);
			}
		}

		if ( joinVariables.size() > 2 ) {
			return findSolutionMappingsLastResort(var1, value1, var2, value2);
		}

		// at this point we know that joinVariables.size() == 2

		if (    (joinVariables.contains(var1) && ! joinVariables.contains(var2))
		     || (joinVariables.contains(var2) && ! joinVariables.contains(var1)) )
		{
			return findSolutionMappingsLastResort(var1, value1, var2, value2);
		}

		final List<Node> valKeyL = new ArrayList<>();
		for ( final Var v : joinVariables ) {
			if( v.equals(var1) ) {
				valKeyL.add(value1);
			}
			else {
				valKeyL.add(value2);
			}
		}

		return returnLookupResultOrEmptyList(valKeyL);
	}

	@Override
	public Iterable<SolutionMapping> findSolutionMappings(
			final Var var1, final Node value1,
			final Var var2, final Node value2,
			final Var var3, final Node value3 )
	{
		final boolean c1 = joinVariables.contains(var1);
		final boolean c2 = joinVariables.contains(var2);
		final boolean c3 = joinVariables.contains(var3);

		if ( joinVariables.size() == 1 ) {
			if ( c1 ) {
				final Iterable<SolutionMapping> it = findSolutionMappings(var1, value1);
				return FilteringIteratorForSolMaps_TwoVarsBindings.createAsIterable(it, var2, value2, var3, value3);
			}
			else if ( c2 ) {
				final Iterable<SolutionMapping> it = findSolutionMappings(var2, value2);
				return FilteringIteratorForSolMaps_TwoVarsBindings.createAsIterable(it, var1, value1, var3, value3);
			}
			else if ( c3 ) {
				final Iterable<SolutionMapping> it = findSolutionMappings(var3, value3);
				return FilteringIteratorForSolMaps_TwoVarsBindings.createAsIterable(it, var1, value1, var2, value2);
			}
			else {
				return findSolutionMappingsLastResort(var1, value1, var2, value2, var3, value3);
			}
		}

		if ( joinVariables.size() == 2 ) {
			if ( c1 && c2 ) {
				final Iterable<SolutionMapping> it = findSolutionMappings(var1, value1, var2, value2);
				return FilteringIteratorForSolMaps_OneVarBinding.createAsIterable(it, var3, value3);
			}
			else if ( c1 && c3 ) {
				final Iterable<SolutionMapping> it = findSolutionMappings(var1, value1, var3, value3);
				return FilteringIteratorForSolMaps_OneVarBinding.createAsIterable(it, var2, value2);
			}
			else if ( c2 && c3 ) {
				final Iterable<SolutionMapping> it = findSolutionMappings(var2, value2, var3, value3);
				return FilteringIteratorForSolMaps_OneVarBinding.createAsIterable(it, var1, value1);
			}
			else {
				return findSolutionMappingsLastResort(var1, value1, var2, value2, var3, value3);
			}
		}

		if ( joinVariables.size() > 3 ) {
			return findSolutionMappingsLastResort(var1, value1, var2, value2, var3, value3);
		}

		// at this point we know that joinVariables.size() == 3

		if ( ! c1 || ! c2 || ! c3 ) {
			return findSolutionMappingsLastResort(var1, value1, var2, value2, var3, value3);
		}

		// at this point we know that joinVariables consists of exactly the three given variables

		final List<Node> valKeyL = new ArrayList<>();
		for ( final Var v : joinVariables ) {
			if( v.equals(var1) ) {
				valKeyL.add(value1);
			}
			else if( v.equals(var2) ) {
				valKeyL.add(value2);
			}
			else {
				valKeyL.add(value3);
			}
		}

		return returnLookupResultOrEmptyList(valKeyL);
	}

	protected List<Node> getVarKeys(final SolutionMapping e){
		final Binding solMapBinding = e.asJenaBinding();
		final List<Node> valKeys = new ArrayList<>();

		for ( final Var v : joinVariables ) {
			final Node n = solMapBinding.get(v);
			if ( n == null ){
				return null;
			}
			valKeys.add(n);
		}
		return valKeys;
	}

	protected Iterable<SolutionMapping> returnLookupResultOrEmptyList( final List<Node> indexKey ) {
		final List<SolutionMapping> solMapList = map.get(indexKey);
		
		// return an empty list if index look-up was unsuccessful
		if ( solMapList == null) {
			return Arrays.asList();
		}

		return solMapList;
	}

}
