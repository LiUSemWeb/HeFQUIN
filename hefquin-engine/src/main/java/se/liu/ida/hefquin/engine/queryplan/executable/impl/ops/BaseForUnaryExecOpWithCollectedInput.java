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
 * Base class for all implementations of {@link UnaryExecutableOp} that
 * work more effectively if at least a minimum number of input solution
 * mappings are available to process. To this end, the functionality of
 * this base class is to collect the input solution mappings until there
 * are at least as many as specified by the {@code minimumCollectionSize}
 * argument of the constructor and, then, pass the resulting batch of
 * collected input solution mappings to the {@code _processCollectedInput}
 * function implemented by the operator that is implemented as an extension
 * of this base class. If further input solution mappings arrive after that,
 * then these are collected again, and so on.
 * <p>
 * Notice that the specified {@code minimumCollectionSize} is only a lower
 * bound; if multiple input solution mapping arrive at the same time, they
 * are all added into the current collection, which may result in collections
 * that contain (many) more than the minimum number of solution mappings.
 * <p>
 * Classes that extend this base class need to implement two functions:
 * <ul>
 * <li>{@link #_processCollectedInput(List, IntermediateResultElementSink, ExecutionContext)} and</li>
 * <li>{@link #_concludeExecution(List, IntermediateResultElementSink, ExecutionContext)}.</li>
 * </ul>
 */
public abstract class BaseForUnaryExecOpWithCollectedInput extends UnaryExecutableOpBase
{
	private int numberOfCollectionsProcessed = 0;

	protected final int minimumCollectionSize;
	protected final List<SolutionMapping> collectedInputSolMaps;

	public BaseForUnaryExecOpWithCollectedInput( final int minimumCollectionSize,
	                                             final boolean collectExceptions,
	                                             final QueryPlanningInfo qpInfo ) {
		super(collectExceptions, qpInfo);

		assert minimumCollectionSize > 0;

		this.minimumCollectionSize = minimumCollectionSize;
		collectedInputSolMaps = new ArrayList<>(minimumCollectionSize);
	}

	@Override
	protected final void _process( final SolutionMapping inputSolMap,
	                               final IntermediateResultElementSink sink,
	                               final ExecutionContext execCxt )
			 throws ExecOpExecutionException
	{
		// Add the given solution mapping to the current collection.
		collectedInputSolMaps.add(inputSolMap);

		// If enough solution mappings have been collected, process them now
		// and, afterwards, remove them from the collection.
		if ( collectedInputSolMaps.size() == minimumCollectionSize ) {
			_processCollectedInput(collectedInputSolMaps, sink, execCxt);
			collectedInputSolMaps.clear();
			numberOfCollectionsProcessed++;
		}
	}

	@Override
	protected final void _process( final List<SolutionMapping> inputSolMaps,
	                               final IntermediateResultElementSink sink,
	                               final ExecutionContext execCxt )
			 throws ExecOpExecutionException
	{
		// If the number of the given solution mappings exceeds the minimum
		// collection size and no other solution mappings have been collected
		// so far (at least not, since the previous collection was processed),
		// then we can pass on the given solution mappings directly as the
		// next collection to be processed.
		if (    inputSolMaps.size() >= minimumCollectionSize
		     && collectedInputSolMaps.isEmpty()  ) {
			_processCollectedInput(inputSolMaps, sink, execCxt);
			numberOfCollectionsProcessed++;
			return;
		}

		// Otherwise, we append the given solution mappings to the collection.
		collectedInputSolMaps.addAll(inputSolMaps);

		// If enough solution mappings have been collected, process them now
		// and, afterwards, remove them from the collection.
		if ( collectedInputSolMaps.size() >= minimumCollectionSize ) {
			_processCollectedInput(collectedInputSolMaps, sink, execCxt);
			collectedInputSolMaps.clear();
			numberOfCollectionsProcessed++;
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
			numberOfCollectionsProcessed++;
		}
	}

	/**
	 * Implementations of this function need to process the given solution
	 * mappings as input and send the produced result elements (if any) to
	 * the given sink.
	 *
	 * If an exception occurs while processing the solution mappings, then
	 * this exception needs to be thrown.
	 */
	protected abstract void _processCollectedInput( List<SolutionMapping> currentBatch,
	                                                IntermediateResultElementSink sink,
	                                                ExecutionContext execCxt )
			throws ExecOpExecutionException;

	/**
	 * Implementations of this function need to process the given solution
	 * mappings as <strong>last</strong> input, conclude the execution of
	 * this operator, and send the remaining result elements (if any) to
	 * the given sink. Notice that the list of solution mappings given here
	 * may contain fewer solution mappings than the minimum collection size,
	 * and it may even be empty!
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
		numberOfCollectionsProcessed = 0;
	}

	@Override
	protected ExecutableOperatorStatsImpl createStats() {
		final ExecutableOperatorStatsImpl s = super.createStats();
		s.put( "numberOfCollectionsProcessed",  numberOfCollectionsProcessed );
		return s;
	}

}
