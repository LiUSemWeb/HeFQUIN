package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.ExprUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.impl.SolutionMappingImpl;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecutableOperatorStatsImpl;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContextExt;

/**
 * To be used for BIND clauses.
 */
public class ExecOpBind extends UnaryExecutableOpBaseWithoutBlocking
{
	private static final Logger log = LoggerFactory.getLogger( ExecOpBind.class );
	private long numberOfOutputMappingsProduced = 0L;

	protected final Worker worker;

	public ExecOpBind( final VarExprList bindExpressions,
	                   final boolean mayReduce,
	                   final boolean collectExceptions,
	                   final QueryPlanningInfo qpInfo ) {
		super(mayReduce, collectExceptions, qpInfo);

		log.debug( "Initialized ExecOpBind with {} bind expression(s).", bindExpressions.size() );

		if ( bindExpressions.size() == 1 ) {
			final Var var = bindExpressions.getVars().get(0);
			final Expr expr = bindExpressions.getExpr(var);
			worker = new OneVarWorker(var, expr);
		}
		else {
			worker = new MultipleVarsWorker(bindExpressions);
		}
	}

	public ExecOpBind( final Var var,
	                   final Expr expr,
	                   final boolean mayReduce,
	                   final boolean collectExceptions,
	                   final QueryPlanningInfo qpInfo ) {
		super(mayReduce, collectExceptions, qpInfo);

		assert var != null;
		assert expr != null;

		log.debug( "Initialized ExecOpBind for variable {} with expression {}.", var, expr );

		worker = new OneVarWorker(var, expr);
	}

	@Override
	protected void _process( final SolutionMapping inputSolMap,
	                         final IntermediateResultElementSink sink,
	                         final QueryProcContextExt ctx )
			 throws ExecOpExecutionException {
		sink.send( worker.extend(inputSolMap) );
		numberOfOutputMappingsProduced++;
	}

	@Override
	protected void _process( final Iterator<SolutionMapping> inputSolMaps,
	                         final int maxBatchSize,
	                         final IntermediateResultElementSink sink,
	                         final QueryProcContextExt ctx )
		 throws ExecOpExecutionException
	{
		final List<SolutionMapping> output = new ArrayList<>();

		// Produce the output solution mappings
		// and populate the list with them.
		int cnt = 0;
		while ( cnt < maxBatchSize && inputSolMaps.hasNext() ) {
			cnt++;
			final SolutionMapping inputSolMap = inputSolMaps.next();
			final SolutionMapping outputSolMap = worker.extend(inputSolMap);
			output.add(outputSolMap);
		}

		numberOfOutputMappingsProduced += output.size();
		sink.send(output);
	}

	@Override
	protected void _concludeExecution( final IntermediateResultElementSink sink,
	                                   final QueryProcContextExt ctx ) {
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
			log.debug( "Evaluating bind expression {} for variable {}.", expr, var );
			final Binding sm = solmap.asJenaBinding();

			if ( sm.contains(var) ) {
				log.debug( "Cannot bind variable {} because it is already bound.", var );
				throwExecOpExecutionException( "Variable '" + var.getVarName() + "' already bound in the given solution mapping." );
			}

			final NodeValue evaluationResult;
			try {
				evaluationResult = ExprUtils.eval(expr, sm);
			}
			catch ( final Exception ex ) {
				// If evaluating the expression based on the current input solution
				// mapping failed, pass on the solution mapping without extending it.
				log.debug( "Evaluation failed for variable {}. Returning original solution mapping.", var );
				return solmap;
			}
			log.debug( "Successfully evaluated expression for variable {}.", var );

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
			log.debug( "Evaluating multiple bind expressions." );

			// This Binding will be replaced by extended versions of
			// it within the following for-loop. Extending it stepwise
			// is necessary because the variable bound by one of the
			// expressions in 'exprs' may be used in a subsequent
			// expressions in 'exprs'.
			Binding sm = solmap.asJenaBinding();

			boolean extended = false;

			for ( final Map.Entry<Var, Expr> e : exprs.getExprs().entrySet() ) {
				final Var var = e.getKey();
				final Expr expr = e.getValue();

				log.debug( "Evaluating bind expression {} for variable {}.", expr, var );

				if ( sm.contains(var) ) {
					log.debug( "Cannot bind variable {} because it is already bound.", var );
					throwExecOpExecutionException( "Variable '" + var.getVarName() + "' already bound in the given solution mapping." );
				}

				// Evaluate the expression based on the current input solution
				// mapping and, if the evaluation is successful, add its result
				// to the output solution mapping.
				try {
					final NodeValue evaluationResult = ExprUtils.eval(expr, sm);
					sm = BindingFactory.binding(sm, var, evaluationResult.asNode() );
					extended = true;

					log.debug( "Successfully extended solution mapping with variable {}.", var );
				}
				catch ( final Exception ex ) {
					// If evaluating the expression based on the current
					// input solution mapping failed, then do nothing.
					log.debug( "Evaluation failed for variable {}. Skipping extension.", var );
				}
			}

			if ( extended )
				return new SolutionMappingImpl(sm);
			else {
				log.debug( "No bind expressions could be evaluated successfully." );
				return solmap;
			}
		}
	}

	protected void throwExecOpExecutionException( final String msg )
			throws ExecOpExecutionException {
		throw new ExecOpExecutionException(msg, this);
	}

}
