package se.liu.ida.hefquin.engine.datastructures.impl;

import java.util.*;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

import org.apache.jena.sparql.engine.binding.Binding;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;

public class SolutionMappingsHashTable extends SolutionMappingsIndexBase
{
	// Having List<Node> as key type for the hash table is probably
	// not the best choice in terms of efficiency. However, it will have
	// to do for the moment.
	// TODO: can this be made more efficient?
	protected final Map<List<Node>, List<SolutionMapping>> map = new HashMap<>();
	protected final Set<Var> joinVariables;

	public SolutionMappingsHashTable(final ExpectedVariables... inputVars){
		assert inputVars.length == 2;
		final Set<Var> certainVars = new HashSet<>( inputVars[0].getCertainVariables());
		certainVars.retainAll( inputVars[1].getCertainVariables() );

		this.joinVariables = certainVars;
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public boolean contains( final Object o ) {
		return map.containsKey(o);
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
		final Binding solMapBinding = e.asJenaBinding();
		final List<Node> valKeyL = new ArrayList<>();

		for (Var v : joinVariables) {
			valKeyL.add(solMapBinding.get(v));
		}

		final List<SolutionMapping> solMapList = new ArrayList<>();
		if (map.containsKey(valKeyL)) {
			solMapList.addAll(map.get(valKeyL));
		}
		solMapList.add(e);

		map.put(valKeyL, solMapList);
		return true;
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public Iterable<SolutionMapping> getJoinPartners( final SolutionMapping sm )
			throws UnsupportedOperationException
	{
		final Binding solMapBinding = sm.asJenaBinding();
		final List<Node> valKeyL = new ArrayList<>();
		List<SolutionMapping> matchingSolMaps = new ArrayList<>();

		for (Var v : joinVariables) {
			valKeyL.add(solMapBinding.get(v));
		}

		if (! valKeyL.isEmpty()){
			matchingSolMaps = map.get(valKeyL);
		} else {
			// If no join variable exists in the given solution mapping, then all solution mappings (in the hash table) are join partners
			final Iterator<List<SolutionMapping>> li = map.values().iterator();
			while(li.hasNext()){
				matchingSolMaps.addAll(li.next());
			}
		}

		return matchingSolMaps;
	}

	@Override
	public Iterable<SolutionMapping> findSolutionMappings( final Var var, final Node value )
			throws UnsupportedOperationException
	{
		final List<Node> valKeyL = new ArrayList<>();

		if ( joinVariables.size() == 1 && joinVariables.contains(var) ){
			valKeyL.add(value);
			final List<SolutionMapping> solMaps = map.get(valKeyL);
			return solMaps;
		}
		else{
			throw new IllegalArgumentException();
		}
	}

	@Override
	public Iterable<SolutionMapping> findSolutionMappings(
			final Var var1, final Node value1,
			final Var var2, final Node value2 )
			throws UnsupportedOperationException
	{
		final List<Node> valKeyL = new ArrayList<>();

		if ( joinVariables.size() == 2 && joinVariables.contains(var1) && joinVariables.contains(var2) ){
			valKeyL.add(value1);
			valKeyL.add(value2);
			final List<SolutionMapping> solMaps = map.get(valKeyL);
			return solMaps;
		}
		else{
			throw new IllegalArgumentException();
		}
	}

	@Override
	public Iterable<SolutionMapping> findSolutionMappings(
			final Var var1, final Node value1,
			final Var var2, final Node value2,
			final Var var3, final Node value3 )
			throws UnsupportedOperationException
	{
		final List<Node> valKeyL = new ArrayList<>();

		if( joinVariables.size() == 3 && joinVariables.contains(var1) && joinVariables.contains(var2) && joinVariables.contains(var3) ){
			valKeyL.add(value1);
			valKeyL.add(value2);
			valKeyL.add(value3);
			final List<SolutionMapping> solMaps = map.get(valKeyL);
			return solMaps;
		}
		else{
			throw new IllegalArgumentException();
		}
	}

}
