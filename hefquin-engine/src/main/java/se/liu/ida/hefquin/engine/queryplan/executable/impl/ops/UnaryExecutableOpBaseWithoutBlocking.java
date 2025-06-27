package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.Iterator;
import java.util.List;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

/**
 * Base class for all implementations of {@link UnaryExecutableOp} that do not
 * need to process the input solution mappings in batches (the filter operator
 * is a typical example). For such operators, this base class implements the
 * {@link #_process(List, IntermediateResultElementSink, ExecutionContext)}
 * method in a way that the given list is still consumed in batches, where
 * each batch is processed by the
 * {@link #_process(Iterator, int, IntermediateResultElementSink, ExecutionContext)}
 * method. Classes that extend this base class should override this method.
 *
 * The purpose of consuming the input list of solution mappings in batches is
 * to increase inter-operator parallelism in the executable plans, in particular
 * in cases in which the input list is huge. If such a huge input list was first
 * processed completely to collect all the output solution mappings for the
 * parent operator in the plan, then the parent operator may idle. Of course,
 * it is also possible to directly send all output solution mappings individually
 * to the parent operator, but that should be avoided to reduce the communication
 * between the threads that run these operators. 
 */
public abstract class UnaryExecutableOpBaseWithoutBlocking extends UnaryExecutableOpBase
{
	public static final int MAX_BATCH_SIZE = 100;

	public UnaryExecutableOpBaseWithoutBlocking( final boolean collectExceptions ) {
		super(collectExceptions);
	}

	@Override
	protected final void _process( final List<SolutionMapping> inputSolMaps,
	                               final IntermediateResultElementSink sink,
	                               final ExecutionContext execCxt )
		 throws ExecOpExecutionException
	{
		final int inputSize = inputSolMaps.size();
		if ( inputSize == 1 ) {
			// If we have only a single input solution mapping, we pass
			// that to the single-input version of the _process method.
			// This is beneficial in cases in which the current subclass
			// of this class would otherwise create a list for collecting
			// the output (but does not do so in its single-input version
			// of the _process method).
			final SolutionMapping inputSolMap = inputSolMaps.get(0);
			_process(inputSolMap, sink, execCxt);
		}
		else if ( inputSize > 1 ) {
			final Iterator<SolutionMapping> it = inputSolMaps.iterator();
			while ( it.hasNext() ) {
				_process(it, MAX_BATCH_SIZE, sink, execCxt);
			}
		}
		// no else case here - nothing to do if inputSolMaps is empty
	}

	/**
	 * Processes input solution mappings of the given iterator by calling
	 * {@link #_process(SolutionMapping, IntermediateResultElementSink, ExecutionContext)}
	 * for each of them, but consumes only as many input solution mappings as
	 * specified by the maxBatchSize argument (or less if the given iterator
	 * is exhausted earlier).
	 *
	 * Subclasses may override this behavior to send a greater number of output
	 * solution mappings to the given sink at a time (which is useful to reduce
	 * the communication between threads in the push-based execution model).
	 * Yet, overriding implementations should not call the given iterator more
	 * often as specified by the maxBatchSize argument. If an exception occurs
	 * within the overriding implementation, then this exception needs to be
	 * thrown.
	 */
	protected void _process( final Iterator<SolutionMapping> inputSolMaps,
	                         final int maxBatchSize,
	                         final IntermediateResultElementSink sink,
	                         final ExecutionContext execCxt )
			throws ExecOpExecutionException {
		int cnt = 0;
		while ( cnt < maxBatchSize && inputSolMaps.hasNext() ) {
			final SolutionMapping inputSolMap = inputSolMaps.next();
			cnt++;
			_process(inputSolMap, sink, execCxt);
		}
	}

}
