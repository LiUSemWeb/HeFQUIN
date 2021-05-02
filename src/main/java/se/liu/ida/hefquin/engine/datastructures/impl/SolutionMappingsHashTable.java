package se.liu.ida.hefquin.engine.datastructures.impl;

import java.util.Iterator;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.engine.data.SolutionMapping;

public class SolutionMappingsHashTable extends SolutionMappingsIndexBase
{
	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean contains( final Object o ) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Iterator<SolutionMapping> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean add( final SolutionMapping e ) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Iterator<SolutionMapping> getJoinPartners( final SolutionMapping sm )
			throws UnsupportedOperationException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<SolutionMapping> findSolutionMappings( final Var var, final Node value )
			throws UnsupportedOperationException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<SolutionMapping> findSolutionMappings(
			final Var var1, final Node value1,
			final Var var2, final Node value2 )
					throws UnsupportedOperationException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<SolutionMapping> findSolutionMappings(
			final Var var1, final Node value1,
			final Var var2, final Node value2,
			final Var var3, final Node value3 )
					throws UnsupportedOperationException
	{
		// TODO Auto-generated method stub
		return null;
	}

}
