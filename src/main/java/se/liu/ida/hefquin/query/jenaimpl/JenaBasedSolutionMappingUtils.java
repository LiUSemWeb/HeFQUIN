package se.liu.ida.hefquin.query.jenaimpl;

import java.util.Iterator;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingUtils;

public class JenaBasedSolutionMappingUtils
{
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

	public static JenaBasedSolutionMapping merge( final JenaBasedSolutionMapping m1, final JenaBasedSolutionMapping m2 ) {
		final Binding b1 = m1.asJenaBinding();
		final Binding b2 = m2.asJenaBinding();
		return new JenaBasedSolutionMapping( BindingUtils.merge(b1,b2) );
	}

}
