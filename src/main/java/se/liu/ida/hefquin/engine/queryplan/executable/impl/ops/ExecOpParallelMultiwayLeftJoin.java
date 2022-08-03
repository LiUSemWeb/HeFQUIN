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
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.physical.UnaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.utils.ExpectedVariablesUtils;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalOpUtils;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalToPhysicalOpConverter;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public class ExecOpParallelMultiwayLeftJoin extends UnaryExecutableOpBase
{
	protected final List<LogicalOpRequest<?,?>> optionalParts;
	protected final Set<Var> joinVars;
	protected final ExpectedVariables inputVarsFromNonOptionalPart;
	protected final List<SolutionMappingsIndex> indexes = new ArrayList<>();
	protected final Set<List<Node>> bindingsForJoinVariable = new HashSet<>();

	public ExecOpParallelMultiwayLeftJoin( final List<LogicalOpRequest<?,?>> optionalParts,
	                                       final ExpectedVariables inputVarsFromNonOptionalPart ) {
		assert ! optionalParts.isEmpty();
		this.optionalParts = optionalParts;
		this.inputVarsFromNonOptionalPart = inputVarsFromNonOptionalPart;

//		Get join variables
		ExpectedVariables varsFromOptionalPart = optionalParts.get(0).getRequest().getExpectedVariables();
		this.joinVars = ExpectedVariablesUtils.intersectionOfCertainVariables( inputVarsFromNonOptionalPart, varsFromOptionalPart );

//		Create indexes structure based on number of join variables, for each optional part
		for( int i = 0; i < optionalParts.size(); i ++ ) {
//			TODO: What if no join variable exists?
			if ( joinVars.size() == 1 ) {
				final Var joinVar = joinVars.iterator().next();
				indexes.add( new SolutionMappingsHashTableBasedOnOneVar(joinVar) );
			} else if ( joinVars.size() == 2 ) {
				final Iterator<Var> liVar = joinVars.iterator();
				final Var joinVar1 = liVar.next();
				final Var joinVar2 = liVar.next();

				indexes.add( new SolutionMappingsHashTableBasedOnTwoVars(joinVar1, joinVar2) );
			} else {
				indexes.add( new SolutionMappingsHashTable(joinVars) );
			}
//
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
//		Populate values for join variables into the set 'bindingsForJoinVariable'
		for ( final SolutionMapping sm : input.getSolutionMappings() ) {
			List<Node> bindings = new ArrayList<>();

			final Iterator<Var> liVar = joinVars.iterator();
			while( liVar.hasNext() ){
				bindings.add( sm.asJenaBinding().get( liVar.next()) );
			}
			bindingsForJoinVariable.add( bindings );
		}

		final CompletableFuture<?>[] futures = new CompletableFuture<?>[ optionalParts.size() ];
		for ( int i = 0; i < optionalParts.size(); i++ ) {
			final LogicalOpRequest<?,?> req = optionalParts.get(i);
			final SolutionMappingsIndex index = indexes.get(i);

			Worker w = new Worker( req, index, input, inputVarsFromNonOptionalPart, execCxt);
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

		for( SolutionMapping inputSol : input.getSolutionMappings() ) {
			Set<SolutionMapping> solMaps = merge( inputSol, indexes );
			for ( SolutionMapping sol : solMaps){
				sink.send(sol);
			}
		}

	}

	@Override
	protected void _concludeExecution( final IntermediateResultElementSink sink,
	                                   final ExecutionContext execCxt ) throws ExecOpExecutionException {
		// nothing to be done here
	}

	protected Set<SolutionMapping> merge( final SolutionMapping inputSol, final List<SolutionMappingsIndex> indexes ){
		Set<SolutionMapping> output = new HashSet<>();
		output.add(inputSol);

		for( SolutionMappingsIndex intermediateResults: indexes ){
			final Set<SolutionMapping> temp = new HashSet<>();

			final Iterable<SolutionMapping> partners = intermediateResults.getJoinPartners( inputSol );
			if( partners != null ){
				for ( SolutionMapping inSol: output ) {
					for (SolutionMapping sol : partners) {
						temp.add( SolutionMappingUtils.merge(inSol, sol) );
					}
				}
				output.clear();
				output.addAll(temp);
			}
//			else
//				Do nothing, return current output
		}
		return output;
	}


	protected static class Worker implements Runnable {
		protected final LogicalOpRequest<?,?> req;
		protected final SolutionMappingsIndex index;
		protected final IntermediateResultBlock input;
		protected final ExpectedVariables inputVarsFromNonOptionalPart;
		protected final ExecutionContext execCxt;

		public Worker( final LogicalOpRequest<?,?> req,
					   final SolutionMappingsIndex index,
					   final IntermediateResultBlock input,
					   final ExpectedVariables inputVarsFromNonOptionalPart,
					   final ExecutionContext execCxt) {
			this.req = req;
			this.index = index;
			this.inputVarsFromNonOptionalPart = inputVarsFromNonOptionalPart;
			this.input = input;
			this.execCxt = execCxt;
		}

		@Override
		public void run() {
			final UnaryLogicalOp addOp = LogicalOpUtils.createLogicalAddOpFromLogicalReqOp(req);
			final UnaryPhysicalOp addPop = LogicalToPhysicalOpConverter.convert(addOp);

			final UnaryExecutableOp exe = addPop.createExecOp( inputVarsFromNonOptionalPart );
			final MyIntermediateResultElementSink mySink = new MyIntermediateResultElementSink(index);

			try {
				exe.process( input, mySink, execCxt );
			} catch (ExecOpExecutionException e) {
				e.printStackTrace();
			}

			mySink.flush();
		}
		
	}

	protected static class MyIntermediateResultElementSink implements IntermediateResultElementSink
	{
		protected final SolutionMappingsIndex index;
//		protected final IntermediateResultBlock input;

		public MyIntermediateResultElementSink( final SolutionMappingsIndex index ) {
			this.index = index;
//			this.input = input;
		}

		@Override
		public void send( final SolutionMapping smFromRequest ) {
//			No need to merge with input solution mappings at this step?
			index.add( smFromRequest );
		}

		public void flush() { }
	}

}
