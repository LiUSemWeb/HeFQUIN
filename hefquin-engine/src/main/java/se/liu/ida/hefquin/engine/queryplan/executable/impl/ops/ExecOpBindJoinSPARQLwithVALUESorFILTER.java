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
 * Version of the VALUES-based bind join algorithm that can fall back to FILTER
 */
public class ExecOpBindJoinSPARQLwithVALUESorFILTER extends BaseForExecOpBindJoinSPARQL {

	BaseForExecOpBindJoinSPARQL currentInstance;
	
	
	public ExecOpBindJoinSPARQLwithVALUESorFILTER(	final TriplePattern query, 
													final SPARQLEndpoint fm, 
													final boolean useOuterJoinSemantics,
													final boolean collectExceptions) {
		super(query, fm, useOuterJoinSemantics, collectExceptions);
	}


	@Override
	protected void _process(IntermediateResultBlock input, IntermediateResultElementSink sink, ExecutionContext execCxt)
			throws ExecOpExecutionException {
		
		//If this is the first request
		if (currentInstance == null) {
			try {
				// Try using VALUES-based bind join
				currentInstance= new ExecOpBindJoinSPARQLwithVALUES (this.query, this.fm, false);
				currentInstance.process(input, sink, execCxt);
			} catch (Exception e ) {
				// Use FILTER-based bind join instead
				try {
					currentInstance = new ExecOpBindJoinSPARQLwithFILTER(this.query, this.fm , currentInstance.useOuterJoinSemantics, this.collectExceptions);
					currentInstance.process(input, sink, execCxt);
				} catch (Exception filterException ) {
					throw filterException;
				}
				
			}
		
		}
		else {
			try {
				currentInstance.process(input, sink, execCxt);
			} catch (Exception e ) {
				throw e;
			}
			
		}
	}

	
	@Override
	public List<Exception> getExceptionsCaughtDuringExecution() {
		return currentInstance.getExceptionsCaughtDuringExecution();
	}


	@Override
	protected NullaryExecutableOp createExecutableRequestOperator(Iterable<SolutionMapping> solMaps) {
		return currentInstance.createExecutableRequestOperator(solMaps);
	}
}
