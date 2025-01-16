package se.liu.ida.hefquin.base.data.utils;

import java.util.Iterator;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.ExprUtils;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.impl.SolutionMappingImpl;

/**
 * This is an iterator of solution mappings that consumes another iterator and
 * extends each of the solution mappings of that other iterator with a binding
 * for an additional variable where the value for this variable is obtained by
 * evaluating the given expression based on the input solution mapping.
 * Any input solution mapping for which evaluating the expression produces an
 * error is passed on as is (i.e., such a solution mapping is not extended).
 * Hence, this iterator is a basis for implementing the BIND clause of SPARQL.
 */
public class ExtendIteratorForSolMaps_OneVar implements Iterator<SolutionMapping>
{
	protected final Iterator<SolutionMapping> input;
	protected final Var var;
	protected final Expr expr;

	public ExtendIteratorForSolMaps_OneVar( final Iterator<SolutionMapping> input,
	                                        final Var var,
	                                        final Expr expr ) {
		assert input != null;
		assert var != null;
		assert expr != null;

		this.input = input;
		this.var = var;
		this.expr = expr;
	}

	public ExtendIteratorForSolMaps_OneVar( final Iterable<SolutionMapping> input,
	                                        final Var var,
	                                        final Expr expr ) {
		this( input.iterator(), var, expr );
	}

	@Override
	public boolean hasNext() {
		return input.hasNext();
	}

	@Override
	public SolutionMapping next() {
		final SolutionMapping solmap = input.next();
		final Binding sm = solmap.asJenaBinding();

		if ( sm.contains(var) ) throw new IllegalArgumentException( "Variable '" + var.getVarName() + "' already bound in the given solution mapping." );

		final NodeValue evaluationResult;
		try {
			evaluationResult = ExprUtils.eval(expr, sm);
		}
		catch ( final Exception ex ) {
			// If evaluating the expression based on the current input solution
			// mapping failed, pass on the solution mapping without extending it.
			return solmap;
		}

		final Binding smOut = BindingFactory.binding( sm, var, evaluationResult.asNode() );
		return new SolutionMappingImpl( smOut );
	}

}
