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
 * Base class for all implementations of {@link UnaryExecutableOp} that work
 * more effectively if at least a minimum number of input solution mappings
 * are available to process. To this end, the functionality of this base class
 * is to collect the input solution mappings until there are at least as many
 * as specified by the {@code minimumBatchSize} argument of the constructor
 * and, then, pass the resulting batch of collected input solution mappings
 * to the {@code _processBatch} function implemented by the operator that is
 * implemented as an extension of this base class. If further input solution
 * mappings arrive after that, then these are collected into another batch,
 * and so on.
 * <p>
 * Notice that the specified {@code minimumBatchSize} is only a lower bound;
 * if multiple input solution mapping arrive at the same time, they are all
 * added into the current batch, which may result in batches that contain
 * (many) more than the minimum number of solution mappings.
 * <p>
 * Classes that extend this base class need to implement two functions:
 * <ul>
 * <li>{@link #_processBatch(List, IntermediateResultElementSink, ExecutionContext)} and</li>
 * <li>{@link #_concludeExecution(List, IntermediateResultElementSink, ExecutionContext)}.</li>
 * </ul>
 */
public abstract class UnaryExecutableOpBaseWithBatching extends UnaryExecutableOpBase
{
	private int numberOfBatchesProcessed = 0;

	protected final int minimumBatchSize;
	protected final List<SolutionMapping> collectedInputSolMaps;

	public UnaryExecutableOpBaseWithBatching( final int minimumBatchSize,
	                                          final boolean collectExceptions,
	                                          final QueryPlanningInfo qpInfo ) {
		super(collectExceptions, qpInfo);

		assert minimumBatchSize > 0;

		this.minimumBatchSize = minimumBatchSize;
		collectedInputSolMaps = new ArrayList<>(minimumBatchSize);
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
		if ( collectedInputSolMaps.size() == minimumBatchSize ) {
			_processBatch(collectedInputSolMaps, sink, execCxt);
			collectedInputSolMaps.clear();
			numberOfBatchesProcessed++;
		}
	}

	@Override
	protected final void _process( final List<SolutionMapping> inputSolMaps,
	                               final IntermediateResultElementSink sink,
	                               final ExecutionContext execCxt )
			 throws ExecOpExecutionException
	{
		// If the number of the given solution mappings exceeds the minimum
		// batch size and no other solution mappings have been collected so
		// far (at least not, since the previous batch was processed), then
		// we can pass on the given solution mappings directly as the next
		// batch to be processed.
		if (    inputSolMaps.size() >= minimumBatchSize
		     && collectedInputSolMaps.isEmpty()  ) {
			_processBatch(inputSolMaps, sink, execCxt);
			numberOfBatchesProcessed++;
			return;
		}

		// Otherwise, we append the given solution mappings to the collection.
		collectedInputSolMaps.addAll(inputSolMaps);

		// If enough solution mappings have been collected to form the next
		// batch, process this batch and, afterwards, remove the solution
		// mappings of that batch from the collection.
		if ( collectedInputSolMaps.size() >= minimumBatchSize ) {
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
	 * (if any) to the given sink. Notice that the batch given here may
	 * contain fewer solution mappings than the minimum batch size, and
	 * it may even be empty!
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
