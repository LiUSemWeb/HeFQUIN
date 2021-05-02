package se.liu.ida.hefquin.engine.data.impl;

import java.util.Iterator;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.binding.BindingMap;
import org.apache.jena.sparql.engine.binding.BindingUtils;

import se.liu.ida.hefquin.engine.data.SolutionMapping;

public class SolutionMappingUtils
{
	/**
	 * Creates an empty solution mapping.
	 */
	public static SolutionMapping createSolutionMapping()
	{
		final Binding b = BindingFactory.binding();
		return new SolutionMappingImpl(b);
	}

	/**
	 * Creates a solution mapping based on the given {@link QuerySolution}.
	 */
	public static SolutionMapping createSolutionMapping( final QuerySolution s )
	{
		return new SolutionMappingImpl( BindingUtils.asBinding(s) );
	}

	/**
	 * Creates a solution mapping in which the given variable
	 * is mapped to the given node.
	 */
	public static SolutionMapping createSolutionMapping(
			final Var var, final Node node )
	{
		final Binding b = BindingFactory.binding(var, node);
		return new SolutionMappingImpl(b);
	}

	/**
	 * Creates a solution mapping in which the given variables
	 * are mapped to the given nodes, respectively.
	 */
	public static SolutionMapping createSolutionMapping(
			final Var var1, final Node node1,
			final Var var2, final Node node2 )
	{
		final BindingMap b = BindingFactory.create();
		b.add(var1, node1);
		b.add(var2, node2);
		return new SolutionMappingImpl(b);
	}

	/**
	 * Creates a solution mapping in which the given variables
	 * are mapped to the given nodes, respectively.
	 */
	public static SolutionMapping createSolutionMapping(
			final Var var1, final Node node1,
			final Var var2, final Node node2,
			final Var var3, final Node node3 )
	{
		final BindingMap b = BindingFactory.create();
		b.add(var1, node1);
		b.add(var2, node2);
		b.add(var3, node3);
		return new SolutionMappingImpl(b);
	}

	/**
	 * Returns true if the given solution mappings are compatible.
	 */
	public static boolean compatible( final SolutionMapping m1, final SolutionMapping m2 ) {
		final Binding b1 = m1.asJenaBinding();
		final Binding b2 = m2.asJenaBinding();

		final Iterator<Var> it = b1.vars();
		while ( it.hasNext() ) {
			final Var v = it.next();
			if ( b2.contains(v) && ! b2.get(v).sameValueAs(b1.get(v)) )
				return false;
		}

		return true;		
	}

	/**
	 * Merges the given solution mappings into one, assuming
	 * that the given solution mappings are compatible.
	 */
	public static SolutionMapping merge( final SolutionMapping m1, final SolutionMapping m2 ) {
		final Binding b1 = m1.asJenaBinding();
		final Binding b2 = m2.asJenaBinding();
		return new SolutionMappingImpl( BindingUtils.merge(b1,b2) );
	}

	/**
	 * Restricts the given solution mapping to the given set of variables.
	 * Hence, the returned solution mapping will be compatible to the solution
	 * mapping given as input, but it will be defined only for the variables
	 * that are in the intersection of the given set of variables and the
	 * set of variables for which the given solution mapping is defined.
	 */
	public static SolutionMapping restrict( final SolutionMapping sm, final Set<Var> vars ) {
		final Binding input = sm.asJenaBinding();
		final Iterator<Var> it = input.vars();
		final BindingMap output = BindingFactory.create();

		while ( it.hasNext() ) {
			final Var var = it.next();
			if ( vars.contains(var) ) {
				output.add( var, input.get(var) );
			}
		}

		return new SolutionMappingImpl(output);
	}

}
