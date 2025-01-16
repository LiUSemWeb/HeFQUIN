package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.Iterator;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.Expr;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.utils.ExtendIteratorForSolMaps_MultipleVars;
import se.liu.ida.hefquin.base.data.utils.ExtendIteratorForSolMaps_OneVar;

/**
 * To be used for BIND clauses.
 */
public class ExecOpBind extends UnaryExecutableOpBaseWithIterator
{
	protected final VarExprList bindExpressions;

	public ExecOpBind( final VarExprList bindExpressions, final boolean collectExceptions ) {
		super(collectExceptions);

		assert bindExpressions != null;
		this.bindExpressions = bindExpressions;
	}

	public ExecOpBind( final Var var, final Expr expr, final boolean collectExceptions ) {
		this( new VarExprList(var,expr), collectExceptions );
	}

	@Override
	protected Iterator<SolutionMapping> createInputToOutputIterator( final Iterable<SolutionMapping> input ) {
		if ( bindExpressions.size() == 1 ) {
			final Var var = bindExpressions.getVars().get(0);
			final Expr expr = bindExpressions.getExpr(var);
			return new ExtendIteratorForSolMaps_OneVar(input, var, expr);
		}
		else {
			return new ExtendIteratorForSolMaps_MultipleVars(input, bindExpressions);
		}
	}

}
