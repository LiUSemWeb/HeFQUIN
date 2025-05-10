package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.Collections;
import java.util.List;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecutableOperatorStatsImpl;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

/**
 * Implementation of (a batching version of) the bind join algorithm that starts by
 * using a VALUES clause exactly as done by {@link ExecOpBindJoinSPARQLwithVALUES}.
 * If this fails for the first request, the implementation repeats the request by
 * using FILTERs as done by {@link ExecOpBindJoinSPARQLwithFILTER} and, then,
 * continues using the FILTER-based approach for the rest of the requests. If the
 * first VALUES-based request succeeds, however, then the implementation continues
 * using the VALUES-based approach for the rest of the requests. 
 */
public class ExecOpBindJoinSPARQLwithVALUESorFILTER extends UnaryExecutableOpBaseWithBatching
{
	public final static int DEFAULT_BATCH_SIZE = BaseForExecOpBindJoinWithRequestOps.DEFAULT_BATCH_SIZE;

	protected final SPARQLGraphPattern query;
	protected final SPARQLEndpoint fm;
	protected final boolean useOuterJoinSemantics;

	// will be initialized when processing the first input block of solution mappings
	protected BaseForExecOpBindJoinSPARQL currentInstance = null;

	public ExecOpBindJoinSPARQLwithVALUESorFILTER( final SPARQLGraphPattern query,
	                                               final SPARQLEndpoint fm,
	                                               final boolean useOuterJoinSemantics,
	                                               final int batchSize,
	                                               final boolean collectExceptions ) {
		super(batchSize, collectExceptions);

		assert query != null;
		assert fm != null;

		this.query = query;
		this.fm = fm;
		this.useOuterJoinSemantics = useOuterJoinSemantics;
	}

	public ExecOpBindJoinSPARQLwithVALUESorFILTER( final SPARQLGraphPattern query,
	                                               final SPARQLEndpoint fm,
	                                               final boolean useOuterJoinSemantics,
	                                               final boolean collectExceptions ) {
		this(query, fm, useOuterJoinSemantics, DEFAULT_BATCH_SIZE, collectExceptions);
	}

	@Override
	protected void _process( final List<SolutionMapping> batch,
	                         final IntermediateResultElementSink sink,
	                         final ExecutionContext execCxt ) throws ExecOpExecutionException {
		// If this is the first request.
		if ( currentInstance == null ) {
			currentInstance = new ExecOpBindJoinSPARQLwithVALUES(query, fm, useOuterJoinSemantics, collectExceptions);
			boolean valuesBasedRequestFailed = false;
			try {
				// Try using VALUES-based bind join
				currentInstance._process(batch, sink, execCxt);
				if ( ! currentInstance.getExceptionsCaughtDuringExecution().isEmpty() ) {
					valuesBasedRequestFailed = true;
				}	
			} catch ( final ExecOpExecutionException e ) {
				valuesBasedRequestFailed = true;
			}
			if ( valuesBasedRequestFailed == true ) {
				// Use FILTER-based bind join instead
				currentInstance = new ExecOpBindJoinSPARQLwithFILTER(query, fm, useOuterJoinSemantics, collectExceptions);
				currentInstance._process(batch, sink, execCxt);
			}
		}
		else {
			currentInstance._process(batch, sink, execCxt);
		}
	}

	@Override
	protected void _concludeExecution( final List<SolutionMapping> batch,
	                                   final IntermediateResultElementSink sink,
	                                   final ExecutionContext execCxt )
			throws ExecOpExecutionException
	{
		if ( batch != null && ! batch.isEmpty() ) {
			_process(batch, sink, execCxt);
		}

		if ( currentInstance != null ) {
			currentInstance.concludeExecution(sink, execCxt);
		}
	}

	@Override
	public List<Exception> getExceptionsCaughtDuringExecution() {
		if ( currentInstance != null ) {
			return currentInstance.getExceptionsCaughtDuringExecution();
		}
		else {
			return Collections.emptyList();
		}
	}

	@Override
	public void resetStats() {
		super.resetStats();

		if ( currentInstance != null ) {
			currentInstance.resetStats();
		}
	}

	@Override
	protected ExecutableOperatorStatsImpl createStats() {
		final ExecutableOperatorStatsImpl s = super.createStats();

		if ( currentInstance == null )
			s.put( "currentInstance", null );
		else
			s.put( "currentInstance", currentInstance.getStats() );

		return s;
	}
}
