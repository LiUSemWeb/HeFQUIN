package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.List;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;
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
public class ExecOpBindJoinSPARQLwithVALUESorFILTER extends BaseForExecOpBindJoinSPARQL {

	// will be initialized when processing the first input block of solution mappings
	protected BaseForExecOpBindJoinSPARQL currentInstance = null;
	
	
	public ExecOpBindJoinSPARQLwithVALUESorFILTER( final TriplePattern query, 
	                                               final SPARQLEndpoint fm,
	                                               final boolean useOuterJoinSemantics,
	                                               final boolean collectExceptions ) {
		super(query, fm, useOuterJoinSemantics, collectExceptions);
	}


	@Override
	protected void _process( final IntermediateResultBlock input, final IntermediateResultElementSink sink, final ExecutionContext execCxt )
			throws ExecOpExecutionException {
		
		//If this is the first request
		if (currentInstance == null) {
			currentInstance = new ExecOpBindJoinSPARQLwithVALUES(query, fm, collectExceptions);
			try {
				// Try using VALUES-based bind join
				currentInstance.process(input, sink, execCxt);
				if (!currentInstance.getExceptionsCaughtDuringExecution().isEmpty()) {
					// Use FILTER-based bind join instead
					currentInstance = new ExecOpBindJoinSPARQLwithFILTER(query, fm, useOuterJoinSemantics, collectExceptions);
					currentInstance.process(input, sink, execCxt);
				}	
			} catch ( final ExecOpExecutionException e ) {
				// Use FILTER-based bind join instead
				currentInstance = new ExecOpBindJoinSPARQLwithFILTER(query, fm, useOuterJoinSemantics, collectExceptions);
				currentInstance.process(input, sink, execCxt);
			}
		}
		else {
			currentInstance.process(input, sink, execCxt);
		}
	}

	
	@Override
	public List<Exception> getExceptionsCaughtDuringExecution() {
		return currentInstance.getExceptionsCaughtDuringExecution();
	}


	@Override
	protected NullaryExecutableOp createExecutableRequestOperator( final Iterable<SolutionMapping> solMaps) {
		return currentInstance.createExecutableRequestOperator(solMaps);
	}
}
