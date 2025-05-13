package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.ExprUtils;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.impl.SolutionMappingImpl;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecutableOperatorStatsImpl;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

/**
 * To be used for BIND clauses.
 */
public class ExecOpBind extends UnaryExecutableOpBase
{
	private long numberOfOutputMappingsProduced = 0L;

	protected final Worker worker;

	public ExecOpBind( final VarExprList bindExpressions, final boolean collectExceptions ) {
		super(collectExceptions);

		if ( bindExpressions.size() == 1 ) {
			final Var var = bindExpressions.getVars().get(0);
			final Expr expr = bindExpressions.getExpr(var);
			worker = new OneVarWorker(var, expr);
		}
		else {
			worker = new MultipleVarsWorker(bindExpressions);
		}
	}

	public ExecOpBind( final Var var, final Expr expr, final boolean collectExceptions ) {
		super(collectExceptions);

		assert var != null;
		assert expr != null;

		worker = new OneVarWorker(var, expr);
	}

	@Override
	protected void _process( final SolutionMapping inputSolMap,
	                         final IntermediateResultElementSink sink,
	                         final ExecutionContext execCxt )
			 throws ExecOpExecutionException {
		sink.send( worker.extend(inputSolMap) );
		numberOfOutputMappingsProduced++;
	}

	@Override
	protected void _process( final List<SolutionMapping> inputSolMaps,
	                         final IntermediateResultElementSink sink,
	                         final ExecutionContext execCxt )
		 throws ExecOpExecutionException
	{
		if ( inputSolMaps.size() == 1 ) {
			// If we have only a single input solution mapping,
			// there will be only one output solution mapping.
			// In contrast to the general case (below), we don't
			// need to create a list to collect the output.
			final SolutionMapping inputSolMap = inputSolMaps.get(0);
			final SolutionMapping outputSolMap = worker.extend(inputSolMap);
			sink.send(outputSolMap);
		}
		else if ( inputSolMaps.size() > 1 ) {
			// If we have multiple input solution mappings, create
			// a list to collect the output solution mappings.
			final List<SolutionMapping> output = new ArrayList<>();

			// Produce the output solution mappings and populate the
			// list with them.
			for ( final SolutionMapping inputSolMap : inputSolMaps ) {
				final SolutionMapping outputSolMap = worker.extend(inputSolMap);
				output.add(outputSolMap);
			}

			sink.send(output);
		}
		// no else case here - nothing to do if inputSolMaps is empty

		numberOfOutputMappingsProduced += inputSolMaps.size();
	}

	@Override
	protected void _concludeExecution( final IntermediateResultElementSink sink,
	                                   final ExecutionContext execCxt ) {
		// nothing to be done here
	}

	@Override
	public void resetStats() {
		super.resetStats();
		numberOfOutputMappingsProduced = 0L;
	}

	@Override
	protected ExecutableOperatorStatsImpl createStats() {
		final ExecutableOperatorStatsImpl s = super.createStats();
		s.put( "numberOfOutputMappingsProduced",  Long.valueOf(numberOfOutputMappingsProduced) );
		return s;
	}

	protected interface Worker {
		SolutionMapping extend( SolutionMapping solmap )
				throws ExecOpExecutionException;
	}

	protected class OneVarWorker implements Worker {
		protected final Var var;
		protected final Expr expr;

		public OneVarWorker( final Var var, final Expr expr ) {
			this.var = var;
			this.expr = expr;
		}

		@Override
		public SolutionMapping extend( final SolutionMapping solmap )
				 throws ExecOpExecutionException
		{
			final Binding sm = solmap.asJenaBinding();

			if ( sm.contains(var) ) throwExecOpExecutionException( "Variable '" + var.getVarName() + "' already bound in the given solution mapping." );

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
			return new SolutionMappingImpl(smOut);
		}
	}

	protected class MultipleVarsWorker implements Worker {
		final VarExprList exprs;

		public MultipleVarsWorker( final VarExprList bindExpressions ) {
			this.exprs = bindExpressions;
		}

		@Override
		public SolutionMapping extend( final SolutionMapping solmap )
				 throws ExecOpExecutionException
		{
			final Binding sm = solmap.asJenaBinding();
			final BindingBuilder smBuilder = BindingFactory.builder(sm);
			boolean extended = false;

			for ( final Map.Entry<Var, Expr> e : exprs.getExprs().entrySet() ) {
				final Var var = e.getKey();
				final Expr expr = e.getValue();

				if ( sm.contains(var) ) throwExecOpExecutionException( "Variable '" + var.getVarName() + "' already bound in the given solution mapping." );

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

	protected void throwExecOpExecutionException( final String msg )
			throws ExecOpExecutionException {
		throw new ExecOpExecutionException(msg, this);
	}

}
