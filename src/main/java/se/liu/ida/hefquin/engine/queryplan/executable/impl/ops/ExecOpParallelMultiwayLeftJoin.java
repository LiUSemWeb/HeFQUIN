package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalOpUtils;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public class ExecOpParallelMultiwayLeftJoin extends UnaryExecutableOpBase
{
/*
	protected final List<UnaryPhysicalOp> optionalParts;


	public ExecOpParallelMultiwayLeftJoin( final List<UnaryPhysicalOp> optionalParts,
	                                       final ExpectedVariables inputVarsFromNonOptionalPart ) {
		assert ! optionalParts.isEmpty();
		//BasePhysicalOpSingleInputJoin
		this.optionalParts = optionalParts;
	}
*/
	protected final List<LogicalOpRequest<?,?>> optionalParts;
	protected final List<?> indexes = null;
	protected final Set<Node> sldjgkrjg = null;

	public ExecOpParallelMultiwayLeftJoin( final List<LogicalOpRequest<?,?>> optionalParts,
	                                       final ExpectedVariables inputVarsFromNonOptionalPart ) {
		assert ! optionalParts.isEmpty();
		this.optionalParts = optionalParts;
	}

	@Override
	public int preferredInputBlockSize() {
		return 30;
	}

	@Override
	protected void _process( final IntermediateResultBlock input,
	                         final IntermediateResultElementSink sink,
	                         final ExecutionContext execCxt ) throws ExecOpExecutionException {
		final List<SolutionMapping> inputForParallelProcess = new ArrayList<>();
		for ( final SolutionMapping sm : input.getSolutionMappings() ) {
			
		}

		// TODO ...
		final CompletableFuture<?>[] futures = new CompletableFuture<?>[ optionalParts.size() ];
		for ( int i = 0; i < optionalParts.size(); i++ ) {
			final LogicalOpRequest<?,?> req = optionalParts.get(i);
			final ? index = indexes.get(i);
			Worker w = new Worker(req, index);
			futures[i] = CompletableFuture.runAsync( w, execCxt.getExecutorServiceForPlanTasks() );
		}

		// wait for all the futures to be completed
		if ( futures.length > 0 ) {
			try {
				CompletableFuture.allOf(futures).get();
			}
			catch ( final InterruptedException e ) {
				throw new ExecOpExecutionException("interruption of the futures that run the executable operators", e, this);
			}
			catch ( final ExecutionException e ) {
				throw new ExecOpExecutionException("The execution of the futures that run the executable operators caused an exception.", e, this);
			}
		}

	}

	@Override
	protected void _concludeExecution( final IntermediateResultElementSink sink,
	                                   final ExecutionContext execCxt ) throws ExecOpExecutionException {
		// nothing to be done here
	}


	protected static class Worker implements Runnable {
		protected final LogicalOpRequest<?,?> req;
		protected final 

		public Worker( final LogicalOpRequest<?,?> req ) { this.req = req; }

		@Override
		public void run() {
			UnaryLogicalOp addOp = LogicalOpUtils.createLogicalAddOpFromLogicalReqOp(req);
			// TODO ...
		}
		
	}

}
