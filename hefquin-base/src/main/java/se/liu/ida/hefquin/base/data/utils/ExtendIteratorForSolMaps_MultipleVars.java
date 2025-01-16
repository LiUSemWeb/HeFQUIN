package se.liu.ida.hefquin.base.data.utils;

import java.util.Iterator;
import java.util.Map;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.ExprUtils;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.impl.SolutionMappingImpl;

/**
 * This is an iterator of solution mappings that consumes another iterator and
 * extends each of the solution mappings of that other iterator with bindings
 * for additional variables where the values for these variables are obtained
 * by evaluating the given expressions based on the input solution mapping.
 * If evaluating an expression based on an input solution mapping produces an
 * error, then this solution mapping is not extended for the corresponding
 * variable.
 * Hence, this iterator is a basis for implementing the BIND clause of SPARQL.
 */
public class ExtendIteratorForSolMaps_MultipleVars implements Iterator<SolutionMapping>
{
	protected final Iterator<SolutionMapping> input;
	protected final VarExprList bindExpressions;

	public ExtendIteratorForSolMaps_MultipleVars( final Iterator<SolutionMapping> input,
	                                              final VarExprList bindExpressions ) {
		assert input != null;
		assert bindExpressions != null;

		this.input = input;
		this.bindExpressions = bindExpressions.isEmpty() ? null : bindExpressions;
	}

	public ExtendIteratorForSolMaps_MultipleVars( final Iterator<SolutionMapping> input,
	                                              final Var var,
	                                              final Expr expr ) {
		this( input, new VarExprList(var,expr) );
	}

	public ExtendIteratorForSolMaps_MultipleVars( final Iterable<SolutionMapping> input,
	                                              final VarExprList bindExpressions ) {
		this( input.iterator(), bindExpressions );
	}

	public ExtendIteratorForSolMaps_MultipleVars( final Iterable<SolutionMapping> input,
	                                              final Var var,
	                                              final Expr expr ) {
		this( input.iterator(), new VarExprList(var,expr) );
	}

	protected static ExprList createExprList( final Expr... bindExpressions ) {
		assert bindExpressions.length > 0;

		final ExprList l = new ExprList();
		for ( int i = 0; i < bindExpressions.length; ++i ) {
			assert bindExpressions[i] != null;
			l.add( bindExpressions[i] );
		}
		return l;
	}

	@Override
	public boolean hasNext() {
		return input.hasNext();
	}

	@Override
	public SolutionMapping next() {
		final SolutionMapping solmap = input.next();
		if ( bindExpressions == null )
			return solmap;

		final Binding sm = solmap.asJenaBinding();
		final BindingBuilder smBuilder = BindingFactory.builder(sm);
		boolean extended = false;

		for ( final Map.Entry<Var, Expr> e : bindExpressions.getExprs().entrySet() ) {
			final Var var = e.getKey();
			final Expr expr = e.getValue();

			if ( sm.contains(var) ) throw new IllegalArgumentException( "Variable '" + var.getVarName() + "' already bound in the given solution mapping." );

			// Evaluate the expression based on the current input solution
			// mapping and, if the evaluation is successful, add its result
			// to the output solution mapping.
			try {
				final NodeValue evaluationResult = ExprUtils.eval(expr, sm);
				smBuilder.add( var, evaluationResult.asNode() );
				extended = true;
			}
			catch ( final Exception ex ) {
				// If evaluating the expression based on the current
				// input solution mapping failed, then do nothing.
			}
		}

		if ( extended )
			return new SolutionMappingImpl( smBuilder.build() );
		else
			return solmap;
	}

}
