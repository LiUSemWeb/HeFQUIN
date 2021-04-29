package se.liu.ida.hefquin.data.jenaimpl;

import java.util.Iterator;

import org.apache.jena.graph.Node;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.binding.BindingMap;
import org.apache.jena.sparql.engine.binding.BindingUtils;

import se.liu.ida.hefquin.data.SolutionMapping;

public class JenaBasedSolutionMappingUtils
{
	/**
	 * Creates an empty solution mapping.
	 */
	public static JenaBasedSolutionMapping createJenaBasedSolutionMapping()
	{
		final Binding b = BindingFactory.binding();
		return new JenaBasedSolutionMapping(b);
	}

	/**
	 * Creates a solution mapping based on the given {@link QuerySolution}.
	 */
	public static JenaBasedSolutionMapping createJenaBasedSolutionMapping( final QuerySolution s )
	{
		return new JenaBasedSolutionMapping( BindingUtils.asBinding(s) );
	}

	/**
	 * Creates a solution mapping in which the given variable
	 * is mapped to the given node.
	 */
	public static JenaBasedSolutionMapping createJenaBasedSolutionMapping(
			final Var var, final Node node )
	{
		final Binding b = BindingFactory.binding(var, node);
		return new JenaBasedSolutionMapping(b);
	}

	/**
	 * Creates a solution mapping in which the given variables
	 * are mapped to the given nodes, respectively.
	 */
	public static JenaBasedSolutionMapping createJenaBasedSolutionMapping(
			final Var var1, final Node node1,
			final Var var2, final Node node2 )
	{
		final BindingMap b = BindingFactory.create();
		b.add(var1, node1);
		b.add(var2, node2);
		return new JenaBasedSolutionMapping(b);
	}

	/**
	 * Creates a solution mapping in which the given variables
	 * are mapped to the given nodes, respectively.
	 */
	public static JenaBasedSolutionMapping createJenaBasedSolutionMapping(
			final Var var1, final Node node1,
			final Var var2, final Node node2,
			final Var var3, final Node node3 )
	{
		final BindingMap b = BindingFactory.create();
		b.add(var1, node1);
		b.add(var2, node2);
		b.add(var3, node3);
		return new JenaBasedSolutionMapping(b);
	}

	/**
	 * Returns true if the given solution mappings are compatible.
	 */
	public static boolean compatible( final JenaBasedSolutionMapping m1, final JenaBasedSolutionMapping m2 ) {
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
	 * Merges the given solution mappings into one, assuming that
	 * the given solution mappings are compatible and that both
	 * are of type {@link JenaBasedSolutionMapping}.
	 */
	public static JenaBasedSolutionMapping merge( final SolutionMapping m1, final SolutionMapping m2 ) {
		return merge( (JenaBasedSolutionMapping) m1, (JenaBasedSolutionMapping) m2 );
	}

	/**
	 * Merges the given solution mappings into one, assuming
	 * that the given solution mappings are compatible.
	 */
	public static JenaBasedSolutionMapping merge( final JenaBasedSolutionMapping m1, final JenaBasedSolutionMapping m2 ) {
		final Binding b1 = m1.asJenaBinding();
		final Binding b2 = m2.asJenaBinding();
		return new JenaBasedSolutionMapping( BindingUtils.merge(b1,b2) );
	}

}
