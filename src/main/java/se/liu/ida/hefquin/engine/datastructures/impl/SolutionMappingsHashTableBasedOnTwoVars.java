package se.liu.ida.hefquin.engine.datastructures.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.utils.FilteringIteratorForSolMaps_OneVarBinding;
import se.liu.ida.hefquin.engine.data.utils.FilteringIteratorForSolMaps_TwoVarsBindings;
import se.liu.ida.hefquin.engine.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.data.utils.SolutionMappingsIterableOverCollectionOfLists;
import se.liu.ida.hefquin.engine.data.utils.SolutionMappingsIteratorOverCollectionOfLists;

import java.util.*;

/**
 * This is a hash table based implementation of {@link SolutionMappingsIndex}
 * that can be used for indexes that are built on two query variables and that
 * are meant to be used for cases in which adding and probing into the index
 * may not happen concurrently.
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
 * relevant for index look-ups are the variable on which the index is built.
 * In other words, the assumption is that the only variables that the solution
 * mappings added to the index have in common with the solution mappings given
 * to the method {@link #getJoinPartners(SolutionMapping)} are the two variables
 * on which the index is built. For cases in which this assumption does not
 * hold, a {@link SolutionMappingsIndexWithPostMatching} can be used to wrap
 * a {@link SolutionMappingsHashTableBasedOnTwoVars}.
 */
public class SolutionMappingsHashTableBasedOnTwoVars extends SolutionMappingsIndexBase
{
    protected final Map<Node, Map<Node, List<SolutionMapping>>> map = new HashMap<>();
    protected final Var joinVar1;
    protected final Var joinVar2;

    public SolutionMappingsHashTableBasedOnTwoVars( final Var joinVar1, final Var joinVar2 ) {
        assert joinVar1 != null;
        assert joinVar2 != null;
        assert ! joinVar1.equals(joinVar2);

        this.joinVar1 = joinVar1;
        this.joinVar2 = joinVar2;
    }

    @Override
    public int size() {
        int size = 0;
        for ( final Map<Node, List<SolutionMapping>> mapIn : map.values() ) {
            for ( final List<SolutionMapping> li : mapIn.values() ){
                size = size + li.size();
            }
        }
        return size;
    }

    @Override
    public boolean isEmpty() {
        for ( final Map<Node, List<SolutionMapping>> mapIn : map.values() ) {
            for ( final List<SolutionMapping> li : mapIn.values()){
                if ( ! li.isEmpty() ) {
                    return false;
                }
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
        return new MyAllSolutionMappingsIterable();
    }

    @Override
    public boolean add( final SolutionMapping e ) {
        final Binding solMapBinding = e.asJenaBinding();
        final Node n1 = solMapBinding.get(joinVar1);
        final Node n2 = solMapBinding.get(joinVar2);

        if (n1 == null || n2 == null){
            throw new IllegalArgumentException();
        }

        final Map<Node, List<SolutionMapping>> mapIn = map.get(n1);
        List<SolutionMapping> solMapList;
        if( mapIn == null){
            final Map<Node, List<SolutionMapping>> mapL = new HashMap<>();
            solMapList = new ArrayList<>();
            mapL.put(n2, solMapList);
            map.put(n1, mapL);
        }
        else {
            solMapList = mapIn.get(n2);
            if ( solMapList == null ){
                solMapList = new ArrayList<>();
                mapIn.put(n2, solMapList);
            }
        }
        solMapList.add(e);
        return true;
    }

    @Override
    public void clear() {
        for ( final Map<Node, List<SolutionMapping>> mapIn : map.values() ) {
            for ( final List<SolutionMapping> li : mapIn.values() ) {
                li.clear();
            }
            mapIn.clear();
        }
        map.clear();
    }

    /**
     * This method assumes that the only variables that the given solution
     * mapping has in common with the solution mappings in the index are the
     * two variables on which the index is built. For cases in which this
     * assumption does not hold, the returned solution mappings are not
     * actually join partners but only candidates for join partners; hence,
     * they still need to be checked for compatibility with the given solution
     * mapping. In cases in which he assumption is not guaranteed to hold, this
     * index can be wrapped by a {@link SolutionMappingsIndexWithPostMatching}
     * which takes care of the compatibility check. 
     */
    @Override
    public Iterable<SolutionMapping> getJoinPartners( final SolutionMapping sm )
    {
        final Binding solMapBinding = sm.asJenaBinding();
        final Node n1 = solMapBinding.get(joinVar1);
        final Node n2 = solMapBinding.get(joinVar2);

        if ( n1 == null && n2 == null ) {
            return getAllSolutionMappings();
        }
        else if ( n1 == null ) {
            return findSolutionMappings(joinVar2, n2);
        }
        else if ( n2 == null ) {
            return findSolutionMappings(joinVar1, n1);
        }
        else {
            return findSolutionMappings(joinVar1, n1, joinVar2, n2);
        }
    }

    @Override
    public Iterable<SolutionMapping> findSolutionMappings( final Var var, final Node value )
    {
        if ( var.equals(joinVar1) ) {
            return new SolutionMappingsIterableOverCollectionOfLists( map.get(value).values() );
        }
        else if ( var.equals(joinVar2) ) {
        	final Iterable<SolutionMapping> it = getAllSolutionMappings();
            return FilteringIteratorForSolMaps_OneVarBinding.createAsIterable(it, joinVar2, value);
        }
        else {
            return findSolutionMappingsLastResort(var, value);
        }
    }

    @Override
    public Iterable<SolutionMapping> findSolutionMappings(
            final Var var1, final Node value1,
            final Var var2, final Node value2 )
	{
		if ( var1.equals(joinVar1) ) {
			if ( var2.equals(joinVar2) ) {
				return returnLookupResultOrEmptyList(value1, value2);
			}
			else {
				final Iterable<SolutionMapping> it = findSolutionMappings(var1, value1);
				return FilteringIteratorForSolMaps_OneVarBinding.createAsIterable(it, var2, value2);
			}
        }

		if ( var2.equals(joinVar1) ) {
			if ( var1.equals(joinVar2) ) {
				return returnLookupResultOrEmptyList(value2, value1);
			}
			else {
				final Iterable<SolutionMapping> it = findSolutionMappings(var2, value2);
				return FilteringIteratorForSolMaps_OneVarBinding.createAsIterable(it, var1, value1);
			}
        }

		return findSolutionMappingsLastResort(var1, value1, var2, value2);
	}

    @Override
    public Iterable<SolutionMapping> findSolutionMappings(
    		final Var var1, final Node value1,
    		final Var var2, final Node value2,
    		final Var var3, final Node value3 )
	{
		if ( var1.equals(joinVar1) ) {
			if ( var2.equals(joinVar2) ) {
				final Iterable<SolutionMapping> it = returnLookupResultOrEmptyList(value1, value2);
				return FilteringIteratorForSolMaps_OneVarBinding.createAsIterable(it, var3, value3);
			}
			else if ( var3.equals(joinVar2) ) {
				final Iterable<SolutionMapping> it = returnLookupResultOrEmptyList(value1, value3);
				return FilteringIteratorForSolMaps_OneVarBinding.createAsIterable(it, var2, value2);
			}
			else {
				final Iterable<SolutionMapping> it = findSolutionMappings(var1, value1);
				return FilteringIteratorForSolMaps_TwoVarsBindings.createAsIterable(it, var2, value2, var3, value3);
			}
        }

		if ( var2.equals(joinVar1) ) {
			if ( var1.equals(joinVar2) ) {
				final Iterable<SolutionMapping> it = returnLookupResultOrEmptyList(value2, value1);
				return FilteringIteratorForSolMaps_OneVarBinding.createAsIterable(it, var3, value3);
			}
			else if ( var3.equals(joinVar2) ) {
				final Iterable<SolutionMapping> it = returnLookupResultOrEmptyList(value2, value3);
				return FilteringIteratorForSolMaps_OneVarBinding.createAsIterable(it, var1, value1);
			}
			else {
				final Iterable<SolutionMapping> it = findSolutionMappings(var2, value2);
				return FilteringIteratorForSolMaps_TwoVarsBindings.createAsIterable(it, var1, value1, var3, value3);
			}
        }

		if ( var3.equals(joinVar1) ) {
			if ( var1.equals(joinVar2) ) {
				final Iterable<SolutionMapping> it = returnLookupResultOrEmptyList(value3, value1);
				return FilteringIteratorForSolMaps_OneVarBinding.createAsIterable(it, var2, value2);
			}
			else if ( var2.equals(joinVar2) ) {
				final Iterable<SolutionMapping> it = returnLookupResultOrEmptyList(value3, value2);
				return FilteringIteratorForSolMaps_OneVarBinding.createAsIterable(it, var3, value3);
			}
			else {
				final Iterable<SolutionMapping> it = findSolutionMappings(var3, value3);
				return FilteringIteratorForSolMaps_TwoVarsBindings.createAsIterable(it, var1, value1, var2, value2);
			}
        }

		return findSolutionMappingsLastResort(var1, value1, var2, value2, var3, value3);
	}


    protected List<SolutionMapping> returnLookupResultOrEmptyList(
            final Node keyValue1, final Node keyValue2 )
    {
        final Map<Node, List<SolutionMapping>> mapIn = map.get(keyValue1);

        // return an empty list if index look-up was unsuccessful
        if ( mapIn == null ) {
            return Arrays.asList();
        }

        final List<SolutionMapping> bucket = mapIn.get(keyValue2);

        // return an empty list if index look-up was unsuccessful
        if ( bucket == null ) {
            return Arrays.asList();
        }

        return bucket;
    }


    // ---- helper classes --------

	protected class MyAllSolutionMappingsIterable implements Iterable<SolutionMapping>
	{
		@Override
		public Iterator<SolutionMapping> iterator() {
			return new MyAllSolutionMappingsIterator();
		}
	}

	protected class MyAllSolutionMappingsIterator implements Iterator<SolutionMapping>
	{
		final protected Iterator<Map<Node, List<SolutionMapping>>> itInnerMaps = map.values().iterator();
		protected Iterator<SolutionMapping> itInnerMapElmts;

		public MyAllSolutionMappingsIterator() {
			if ( itInnerMaps.hasNext() )
				itInnerMapElmts = new SolutionMappingsIteratorOverCollectionOfLists(
						itInnerMaps.next().values() );
			else
				itInnerMapElmts = null;
		}

		@Override
		public boolean hasNext() {
			if ( itInnerMapElmts == null )
				return false;

			while ( ! itInnerMapElmts.hasNext() && itInnerMaps.hasNext() ) {
				itInnerMapElmts = new SolutionMappingsIteratorOverCollectionOfLists(
						itInnerMaps.next().values() );
			}

			return itInnerMapElmts.hasNext();
		}

		@Override
		public SolutionMapping next() {
			if ( ! hasNext() )
				throw new NoSuchElementException();

			return itInnerMapElmts.next();
		}
	}

}
