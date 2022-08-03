package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.datastructures.SolutionMappingsIndex;
import se.liu.ida.hefquin.engine.datastructures.impl.SolutionMappingsHashTable;
import se.liu.ida.hefquin.engine.datastructures.impl.SolutionMappingsHashTableBasedOnOneVar;
import se.liu.ida.hefquin.engine.datastructures.impl.SolutionMappingsHashTableBasedOnTwoVars;
import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.*;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.GenericIntermediateResultBlockImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.physical.UnaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.utils.ExpectedVariablesUtils;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalOpUtils;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalToPhysicalOpConverter;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public class ExecOpParallelMultiwayLeftJoin extends UnaryExecutableOpBase
{
	protected final ExpectedVariables inputVarsFromNonOptionalPart;
	protected final List<LogicalOpRequest<?,?>> optionalParts;
	 
	protected final List<SolutionMappingsIndex> indexes; // will contain as many entries as optionalParts
	protected final List<Var> joinVars; // using a list gives us a deterministic iteration order
	protected final Set<List<Node>> bindingsForJoinVariable = new HashSet<>();

	public ExecOpParallelMultiwayLeftJoin( final ExpectedVariables inputVarsFromNonOptionalPart,
	                                       final LogicalOpRequest<?,?> ... optionalParts ) {
		this( inputVarsFromNonOptionalPart, Arrays.asList(optionalParts) );
	}

	public ExecOpParallelMultiwayLeftJoin( final ExpectedVariables inputVarsFromNonOptionalPart,
	                                       final List<LogicalOpRequest<?,?>> optionalParts ) {
		assert ! optionalParts.isEmpty();

		this.optionalParts = optionalParts;
		this.inputVarsFromNonOptionalPart = inputVarsFromNonOptionalPart;

		// determine the join variables
		// (assumption: the join variable(s) are the same for all optional parts)
		final ExpectedVariables varsFromOptionalPart = optionalParts.get(0).getRequest().getExpectedVariables();
		final Set<Var> joinVarsSet = ExpectedVariablesUtils.intersectionOfCertainVariables(inputVarsFromNonOptionalPart, varsFromOptionalPart);
		this.joinVars = new ArrayList<Var>( joinVarsSet );

		// create index for each optional part; the index implementation
		// to be used depends on the number of join variables
//		TODO: What if no join variable exists?
		indexes = new ArrayList<>( optionalParts.size() );
		if ( joinVars.size() == 1 ) {
			final Var joinVar = joinVars.get(0);
			for( int i = 0; i < optionalParts.size(); i ++ ) {
				indexes.add( new SolutionMappingsHashTableBasedOnOneVar(joinVar) );
			}
		}
		else if ( joinVars.size() == 2 ) {
			final Var joinVar1 = joinVars.get(0);
			final Var joinVar2 = joinVars.get(1);
			for( int i = 0; i < optionalParts.size(); i ++ ) {
				indexes.add( new SolutionMappingsHashTableBasedOnTwoVars(joinVar1, joinVar2) );
			}
		}
		else {
			for( int i = 0; i < optionalParts.size(); i ++ ) {
				indexes.add( new SolutionMappingsHashTable(joinVars) );
			}
		}
	}

	@Override
	public int preferredInputBlockSize() {
		return 30;
	}

	@Override
	protected void _process( final IntermediateResultBlock input,
	                         final IntermediateResultElementSink sink,
	                         final ExecutionContext execCxt ) throws ExecOpExecutionException {
		final IntermediateResultBlock inputForParallelProcess = determineInputForParallelProcess(input);

		if ( inputForParallelProcess.size() > 0 ) {
			parallelPhase(inputForParallelProcess, execCxt);
		}

		mergePhase( input.getSolutionMappings(), sink );
	}

	/**
	 * Preprocess the given {@link IntermediateResultBlock} to identify the
	 * input solution mappings that do not need to be considered during the
	 * parallel phase of the algorithm (because they have bindings for the
	 * join variables such that there already was an earlier input solution
	 * mapping with the same bindings). The {@link IntermediateResultBlock}
	 * returned by this function contains only the solution mappings from
	 * the given block that need to be considered.
	 */
	protected IntermediateResultBlock determineInputForParallelProcess( final IntermediateResultBlock input ) {
		final GenericIntermediateResultBlockImpl inputForParallelProcess = new GenericIntermediateResultBlockImpl();
		for ( final SolutionMapping sm : input.getSolutionMappings() ) {
			final List<Node> bindings = new ArrayList<>( joinVars.size() );
			for ( final Var v : joinVars ) {
				bindings.add( sm.asJenaBinding().get(v) );
			}

			if( ! bindingsForJoinVariable.contains(bindings) ) {
				inputForParallelProcess.add(sm);
			}

			bindingsForJoinVariable.add( bindings );
		}

		return inputForParallelProcess;
	}

	protected void parallelPhase( final IntermediateResultBlock inputForParallelProcess,
	                              final ExecutionContext execCxt ) throws ExecOpExecutionException {
		// begin the parallel phase by starting the workers for the optional parts
		final CompletableFuture<?>[] futures = new CompletableFuture<?>[ optionalParts.size() ];
		for ( int i = 0; i < optionalParts.size(); i++ ) {
			final Worker w = new Worker( optionalParts.get(i),
			                             indexes.get(i),
										 inputForParallelProcess,
			                             inputVarsFromNonOptionalPart,
			                             execCxt );
			futures[i] = CompletableFuture.runAsync( w, execCxt.getExecutorServiceForPlanTasks() );
		}

		// wait until the parallel phase is completed
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

	protected void mergePhase( final Iterable<SolutionMapping> inputSolMaps,
	                           final IntermediateResultElementSink sink ) {
		for( final SolutionMapping inputSolMap : inputSolMaps ) {
			final Set<SolutionMapping> outputSolMaps = merge(inputSolMap);
			for ( final SolutionMapping outputSolMap : outputSolMaps ) {
				sink.send(outputSolMap);
			}
		}
	}

	protected Set<SolutionMapping> merge( final SolutionMapping inputSol ) {
		Set<SolutionMapping> output = new HashSet<>();
		output.add(inputSol);

		for ( final SolutionMappingsIndex index: indexes ) {
			final Iterable<SolutionMapping> partners = index.getJoinPartners(inputSol);
			if( partners != null ){
				final Set<SolutionMapping> nextOutput = new HashSet<>();
				for ( final SolutionMapping inSol: output ) {
					for ( final SolutionMapping sol : partners ) {
						nextOutput.add( SolutionMappingUtils.merge(inSol,sol) );
					}
				}
				output = nextOutput;
			}
			// An else branch is not needed. If there are no join partners
			// in the current index, just continue using the 'output'
			// from the previous merge stage.
		}

		return output;
	}

	@Override
	protected void _concludeExecution( final IntermediateResultElementSink sink,
	                                   final ExecutionContext execCxt ) throws ExecOpExecutionException {
		// nothing to be done here
	}


	protected static class Worker implements Runnable {
		protected final UnaryExecutableOp execOp;
		protected final IntermediateResultElementSink mySink;
		protected final IntermediateResultBlock input;
		protected final ExecutionContext execCxt;

		public Worker( final LogicalOpRequest<?,?> req,
		               final SolutionMappingsIndex index,
		               final IntermediateResultBlock input,
		               final ExpectedVariables inputVarsFromNonOptionalPart,
		               final ExecutionContext execCxt ) {
			this.input = input;
			this.execCxt = execCxt;

			final UnaryLogicalOp addLop = LogicalOpUtils.createLogicalAddOpFromLogicalReqOp(req);
			final UnaryPhysicalOp addPop = LogicalToPhysicalOpConverter.convert(addLop);
			this.execOp = addPop.createExecOp(inputVarsFromNonOptionalPart);

			this.mySink = new IntermediateResultElementSink() {
				@Override
				public void send( final SolutionMapping sm ) {
					index.add(sm);
				}
			};
		}

		@Override
		public void run() {
			try {
				execOp.process(input, mySink, execCxt);
			} catch ( final ExecOpExecutionException e ) {
// TODO: this exception must be handled properly such that we have a chance to
// actually catch something in the main processing thread that has started this
// worker thread
				e.printStackTrace();
			}
		}
	}

}
