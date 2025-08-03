package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.ArrayList;
import java.util.List;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecutableOperatorStatsImpl;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

/**
 * Base class for all implementations of {@link UnaryExecutableOp} that process
 * the input solution mappings in batches (bind joins are typical examples of
 * such operators).
 *
 * This base class handles collecting the input solution mappings into batches.
 * Classes that extend this base class need to implement two functions:
 * <ul>
 * <li>{@link #_processBatch(List, IntermediateResultElementSink, ExecutionContext)} and</li>
 * <li>{@link #_concludeExecution(List, IntermediateResultElementSink, ExecutionContext)}.</li>
 * </ul>
 */
public abstract class UnaryExecutableOpBaseWithBatching extends UnaryExecutableOpBase
{
	private int numberOfBatchesProcessed = 0;

	protected final int batchSize;
	protected final List<SolutionMapping> collectedInputSolMaps;

	public UnaryExecutableOpBaseWithBatching( final int batchSize,
	                                          final boolean collectExceptions,
	                                          final QueryPlanningInfo qpInfo ) {
		super(collectExceptions, qpInfo);

		assert batchSize > 0;

		this.batchSize = batchSize;
		collectedInputSolMaps = new ArrayList<>(batchSize);
	}

	@Override
	protected final void _process( final SolutionMapping inputSolMap,
	                               final IntermediateResultElementSink sink,
	                               final ExecutionContext execCxt )
			 throws ExecOpExecutionException
	{
		// Add the given solution mapping to the collection.
		collectedInputSolMaps.add(inputSolMap);

		// If enough solution mappings have been collected to form the next
		// batch, process this batch and, afterwards, remove the solution
		// mappings of that batch from the collection.
		if ( collectedInputSolMaps.size() == batchSize ) {
			_processBatch(collectedInputSolMaps, sink, execCxt);
			collectedInputSolMaps.clear();
			numberOfBatchesProcessed++;
		}
	}

	@Override
	protected final void _concludeExecution( final IntermediateResultElementSink sink,
	                                         final ExecutionContext execCxt )
			throws ExecOpExecutionException
	{
		_concludeExecution(collectedInputSolMaps, sink, execCxt);

		if ( ! collectedInputSolMaps.isEmpty() ) {
			collectedInputSolMaps.clear();
			numberOfBatchesProcessed++;
		}
	}

	/**
	 * Implementations of this function need to process the given batch of
	 * solution mappings as input and send the produced result elements (if
	 * any) to the given sink.
	 *
	 * If an exception occurs while processing the batch, then this
	 * exception needs to be thrown.
	 */
	protected abstract void _processBatch( List<SolutionMapping> currentBatch,
	                                       IntermediateResultElementSink sink,
	                                       ExecutionContext execCxt )
			throws ExecOpExecutionException;

	/**
	 * Implementations of this function need to process the given batch
	 * of solution mappings as <strong>last</strong> input, conclude the
	 * execution of this operator, and send the remaining result elements
	 * (if any) to the given sink. Notice that the given batch of solution
	 * mappings may be empty!
	 *
	 * If an exception occurs during this process, then this exception needs
	 * to be thrown.
	 */
	protected abstract void _concludeExecution( List<SolutionMapping> currentBatch,
	                                            IntermediateResultElementSink sink,
	                                            ExecutionContext execCxt )
			throws ExecOpExecutionException;

	@Override
	public void resetStats() {
		super.resetStats();
		numberOfBatchesProcessed = 0;
	}

	@Override
	protected ExecutableOperatorStatsImpl createStats() {
		final ExecutableOperatorStatsImpl s = super.createStats();
		s.put( "numberOfBatchesProcessed",  numberOfBatchesProcessed );
		return s;
	}

}
