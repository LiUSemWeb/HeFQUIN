package se.liu.ida.hefquin.engine.datastructures.impl;

import java.util.*;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

import org.apache.jena.sparql.engine.binding.Binding;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.impl.SolutionMappingUtils;

public class SolutionMappingsHashTable extends SolutionMappingsIndexBase
{
	protected final Map<List<Node>, List<SolutionMapping>> map = new HashMap<>();
	protected final Set<Var> joinVariables;

	public SolutionMappingsHashTable(final Set<Var> joinVariables){
		this.joinVariables = joinVariables;
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
		// TODO
		return map.containsKey(o);
	}

	@Override
	public Iterator<SolutionMapping> iterator() {
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

		if (map.containsKey(valKeys)) {
			map.get(valKeys).add(e);
		}
		else {
			final List<SolutionMapping> solMapList = new ArrayList<>();
			solMapList.add(e);
			map.put(valKeys, solMapList);
		}
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
		final List<Node> valKeys = getVarKeys(sm);
		final List<SolutionMapping> joinPartner = new ArrayList<>();
		List<SolutionMapping> matchingSolMaps = new ArrayList<>();

		if (!valKeys.isEmpty() && valKeys.size() == joinVariables.size()){
			matchingSolMaps = map.get(valKeys);
		}
		else {
			final Iterator<List<SolutionMapping>> li = map.values().iterator();
			while(li.hasNext()){
				matchingSolMaps.addAll(li.next());
			}
		}

		if (matchingSolMaps != null) {
			for (SolutionMapping matchSolM : matchingSolMaps) {
				if (SolutionMappingUtils.compatible(sm, matchSolM)) {
					joinPartner.add(matchSolM);
				}
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
		if ( joinVariables.size() == 2 && joinVariables.contains(var1) && joinVariables.contains(var2) ){
			final List<Node> valKeyL = new ArrayList<>();
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
		if( joinVariables.size() == 3 && joinVariables.contains(var1) && joinVariables.contains(var2) && joinVariables.contains(var3) ){
			final List<Node> valKeyL = new ArrayList<>();
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

	public List<Node> getVarKeys(final SolutionMapping e){
		final Binding solMapBinding = e.asJenaBinding();
		final List<Node> valKeys = new ArrayList<>();

		for (Var v : joinVariables) {
			if ( !solMapBinding.contains(v) ){
				throw new IllegalArgumentException();
			}
			else {
				valKeys.add(solMapBinding.get(v));
			}
		}
		return valKeys;
	}
}
