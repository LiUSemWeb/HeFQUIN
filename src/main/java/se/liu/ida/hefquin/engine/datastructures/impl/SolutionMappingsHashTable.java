package se.liu.ida.hefquin.engine.datastructures.impl;

import java.util.*;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

import org.apache.jena.sparql.engine.binding.Binding;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.impl.SolutionMappingUtils;

public class SolutionMappingsHashTable extends SolutionMappingsIndexBase
{
	// Having List<Node> as key type for the hash table is probably
	// not the best choice in terms of efficiency. However, it will have
	// to do for the moment.
	// TODO: can this be made more efficient?
	protected final Map<List<Node>, List<SolutionMapping>> map = new HashMap<>();
	protected final Collection<Var> joinVariables;

	public SolutionMappingsHashTable( final Set<Var> joinVariables ){
		this.joinVariables = joinVariables;
	}
	public SolutionMappingsHashTable( final Var ... vars ) {
		this.joinVariables = Arrays.asList(vars);
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
		if( !(o instanceof SolutionMapping) ){
			return false;
		}

		final SolutionMapping sm = (SolutionMapping) o;
		for ( final List<SolutionMapping> li : map.values() ) {
			if ( li.contains(sm) ) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Iterator<SolutionMapping> iterator() {
		// The following implementation of this method is inefficient because,
		// each time it is called, it creates and populates a list of solution
		// mappings by iterating over the content of this index. However,
		// for a thread-safe implementation, we may have to live with this.
		// TODO: is there a more efficient way to implement this method?
		final List<SolutionMapping> solMap = new ArrayList<>();
		final Iterator<List<SolutionMapping>> li = map.values().iterator();
		while(li.hasNext()){
			solMap.addAll(li.next());
		}
		return solMap.iterator();
	}

	@Override
	public boolean add( final SolutionMapping e ) {
		final List<Node> valKeys = getVarKeys(e);

		List<SolutionMapping> solMapList = map.get(valKeys);
		if ( solMapList == null) {
			solMapList = new ArrayList<>();
			map.put(valKeys, solMapList);
		}

		solMapList.add(e);
		return true;
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
			throws UnsupportedOperationException
	{
		final List<Node> valKeys = getVarKeys(sm);
		final Iterator<SolutionMapping> matchingSolMaps;
		if (!valKeys.isEmpty() && valKeys.size() == joinVariables.size()){
			final List<SolutionMapping> l = map.get(valKeys);
			matchingSolMaps = (l != null) ? l.iterator() : null;
		}
		else if(valKeys.isEmpty()){
			matchingSolMaps = iterator();
		}
		else {
			throw new  UnsupportedOperationException();
		}

		final List<SolutionMapping> joinPartner = new ArrayList<>();
		while (matchingSolMaps != null && matchingSolMaps.hasNext()) {
			final SolutionMapping matchSolM = matchingSolMaps.next();
			if (SolutionMappingUtils.compatible(sm, matchSolM)) {
				joinPartner.add(matchSolM);
			}
		}
		return joinPartner;
	}

	@Override
	public Iterable<SolutionMapping> findSolutionMappings( final Var var, final Node value )
			throws UnsupportedOperationException
	{
		if ( joinVariables.size() == 1 && joinVariables.contains(var) ){
			final List<Node> valKeyL = new ArrayList<>();
			valKeyL.add(value);
			return map.get(valKeyL);
		}
		else{
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public Iterable<SolutionMapping> findSolutionMappings(
			final Var var1, final Node value1,
			final Var var2, final Node value2 )
			throws UnsupportedOperationException
	{
		if ( joinVariables.size() == 2 && joinVariables.contains(var1) && joinVariables.contains(var2) ){
			final List<Node> valKeyL = new ArrayList<>();
			for ( final Var v : joinVariables ) {
				if( v.equals(var1) ){
					valKeyL.add(value1);
				}
				else if( v.equals(var2) ){
					valKeyL.add(value2);
				}
				else{
					throw new IllegalStateException();
				}
			}
			return  map.get(valKeyL);
		}
		else{
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public Iterable<SolutionMapping> findSolutionMappings(
			final Var var1, final Node value1,
			final Var var2, final Node value2,
			final Var var3, final Node value3 )
			throws UnsupportedOperationException
	{
		if( joinVariables.size() == 3 && joinVariables.contains(var1) && joinVariables.contains(var2) && joinVariables.contains(var3) ){
			final List<Node> valKeyL = new ArrayList<>();
			for ( final Var v : joinVariables ) {
				if( v.equals(var1) ){
					valKeyL.add(value1);
				}
				else if( v.equals(var2) ){
					valKeyL.add(value2);
				}
				else if( v.equals(var3) ){
					valKeyL.add(value3);
				}
				else{
					throw new IllegalArgumentException();
				}
			}
			return map.get(valKeyL);
		}
		else{
			throw new UnsupportedOperationException();
		}
	}

	protected List<Node> getVarKeys(final SolutionMapping e){
		final Binding solMapBinding = e.asJenaBinding();
		final List<Node> valKeys = new ArrayList<>();

		for ( final Var v : joinVariables ) {
			final Node n = solMapBinding.get(v);
			if ( n == null ){
				throw new IllegalArgumentException();
			}
			valKeys.add(n);
		}
		return valKeys;
	}
}
