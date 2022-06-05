package se.liu.ida.hefquin.engine.datastructures.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.utils.FilteringIteratorForSolMaps_OneVarBinding;
import se.liu.ida.hefquin.engine.data.utils.FilteringIteratorForSolMaps_TwoVarsBindings;
import se.liu.ida.hefquin.engine.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.data.utils.SolutionMappingsIterableOverCollectionOfLists;

import java.util.*;

/**
 * This is a hash table based implementation of {@link SolutionMappingsIndex}
 * that can be used for indexes that are built on one query variable and that
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
 * Another assumption of this implementation is that the only variable
 * relevant for index look-ups is the variable on which the index is built.
 * In other words, the assumption is that the only variable that the solution
 * mappings added to the index have in common with the solution mappings
 * given to the method {@link #getJoinPartners(SolutionMapping)} is the
 * variable on which the index is built. For cases in which this assumption
 * does not hold, a {@link SolutionMappingsIndexWithPostMatching} can be
 * used to wrap a {@link SolutionMappingsHashTableBasedOnOneVar}.
 */
public class SolutionMappingsHashTableBasedOnOneVar extends SolutionMappingsIndexBase
{
    protected final Map<Node, List<SolutionMapping>> map = new HashMap<>();
    protected final Var joinVar;

    public SolutionMappingsHashTableBasedOnOneVar(final Var joinVar) {
        this.joinVar = joinVar;
    }

    @Override
    public int size() {
        int size = 0;
        for ( final List<SolutionMapping> li : map.values() ) {
        	size += li.size();
        }
        return size;
    }

    @Override
    public boolean isEmpty() {
    	for ( final List<SolutionMapping> li : map.values() ) {
    		if ( ! li.isEmpty() ) {
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
        final Node valKeys = getVarKey(e);

        if (valKeys != null ){
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
    	for ( final List<SolutionMapping> li : map.values() ) {
    		li.clear();
    	}
    	map.clear();
    }

    /**
     * This method assumes that the only variable that the given solution
     * mapping has in common with the solution mappings in the index is the
     * variable on which the index is built. For cases in which this assumption
     * does not hold, the returned solution mappings are not actually join
     * partners but only candidates for join partners; hence, they still need
     * to be checked for compatibility with the given solution mapping. In
     * cases in which he assumption is not guaranteed to hold, this index can
     * be wrapped by a {@link SolutionMappingsIndexWithPostMatching} which
     * takes care of the compatibility check. 
     */
    @Override
    public Iterable<SolutionMapping> getJoinPartners( final SolutionMapping sm )
    {
        final Node valKeys = getVarKey(sm);
        if ( valKeys == null ){
            return getAllSolutionMappings();
        }
        else {
            return findSolutionMappings(joinVar, valKeys);
        }
    }

    @Override
    public Iterable<SolutionMapping> findSolutionMappings( final Var var, final Node value )
    {
        if ( joinVar.equals(var) ) {
            final List<SolutionMapping> solMapList = map.get(value);

            // return an empty list if index look-up was unsuccessful
            if ( solMapList == null) {
                return Arrays.asList();
            }

            return solMapList;
        }
        else{
            return findSolutionMappingsLastResort(var, value);
        }
    }

    @Override
    public Iterable<SolutionMapping> findSolutionMappings(
    		final Var var1, final Node value1,
    		final Var var2, final Node value2 )
    {
        if ( joinVar.equals(var1) ) {
            final Iterable<SolutionMapping> it = findSolutionMappings(var1, value1);
            return FilteringIteratorForSolMaps_OneVarBinding.createAsIterable(it, var2, value2);
        }
        else if ( joinVar.equals(var2) ) {
            final Iterable<SolutionMapping> it = findSolutionMappings(var2, value2);
            return FilteringIteratorForSolMaps_OneVarBinding.createAsIterable(it, var1, value1);
        }
        else {
            return findSolutionMappingsLastResort(var1, value1, var2, value2);
        }
    }

    @Override
    public Iterable<SolutionMapping> findSolutionMappings(
    		final Var var1, final Node value1,
    		final Var var2, final Node value2,
    		final Var var3, final Node value3 )
    {
        if ( joinVar.equals(var1) ) {
            final Iterable<SolutionMapping> it = findSolutionMappings(var1, value1);
            return FilteringIteratorForSolMaps_TwoVarsBindings.createAsIterable(it, var2, value2, var3, value3);
        }
        else if ( joinVar.equals(var2) ) {
            final Iterable<SolutionMapping> it = findSolutionMappings(var2, value2);
            return FilteringIteratorForSolMaps_TwoVarsBindings.createAsIterable(it, var1, value1, var3, value3);
        }
        else if ( joinVar.equals(var3) ) {
            final Iterable<SolutionMapping> it = findSolutionMappings(var3, value3);
            return FilteringIteratorForSolMaps_TwoVarsBindings.createAsIterable(it, var1, value1, var2, value2);
        }
        else {
            return findSolutionMappingsLastResort(var1, value1, var2, value2, var3, value3);
        }
    }

    protected Node getVarKey( final SolutionMapping e ) {
        final Node n = e.asJenaBinding().get(joinVar);
        return n; // may be null
    }

}
