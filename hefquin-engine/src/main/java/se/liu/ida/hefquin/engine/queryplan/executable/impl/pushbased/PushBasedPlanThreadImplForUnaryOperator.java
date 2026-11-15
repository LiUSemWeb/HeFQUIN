package se.liu.ida.hefquin.engine.queryplan.executable.impl.pushbased;

import java.util.ArrayList;
import java.util.List;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutableOperator;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContextExt;

public class PushBasedPlanThreadImplForUnaryOperator extends PushBasedPlanThreadImplBase
{
	protected final UnaryExecutableOp op;
	protected final PushBasedPlanThread input;

	public PushBasedPlanThreadImplForUnaryOperator( final UnaryExecutableOp op,
	                                                final PushBasedPlanThread input,
	                                                final QueryProcContextExt ctx ) {
		super(ctx, op.mayReduce());

		assert op != null;
		assert input != null;

		this.op = op;
		this.input = input;
	}

	@Override
	protected ExecutableOperator getExecOp() {
		return op;
	}

	@Override
	protected void produceOutput( final IntermediateResultElementSink sink )
			throws ExecOpExecutionException, ConsumingPushBasedPlanThreadException
	{
		final List<SolutionMapping> transferBuffer = new ArrayList<>();
		boolean inputConsumed = false;
		while ( ! inputConsumed ) {
			if ( op.terminationFlagIsSet() ) return;

			input.transferAvailableOutput(transferBuffer);
			if ( ! transferBuffer.isEmpty() ) {
				for ( final SolutionMapping sm : transferBuffer ) {
					op.process(sm, sink, ctx);

					if ( op.terminationFlagIsSet() ) return;
// TODO: sending all sol.mappings separately to the operator is
// not a good idea for some operators (e.g., union, filter), because
// it causes such operators to also pass on these solution mappings
// separately to their parent operator, which causes a lot of
// synchronization between the operators (which is exactly what I
// wanted to avoid by introducing the version of op.process(..) that
// takes a collection of input sol.mappings. Idea: add a constant function
// to the operators that returns 'true' for operators for which it is okay
// to send the whole transferBuffer in one op.process(..) call without
// checking the terminationFlag for every sol.mapping in transferBuffer.
// TODO: Another idea: add a termination flag also into the operators themselves;
// some may pick it up internally (note that this flag needs to be thread safe)
				}
			}
			else {
				op.concludeExecution(sink, ctx);
				inputConsumed = true;
			}
		}
	}

}
