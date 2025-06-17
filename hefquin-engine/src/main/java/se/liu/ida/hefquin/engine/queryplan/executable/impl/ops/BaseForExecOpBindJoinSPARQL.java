package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.List;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

/**
 * A base class for all variations of the bind join algorithm that use
 * some form of SPARQL requests.
 */
public abstract class BaseForExecOpBindJoinSPARQL extends BaseForExecOpBindJoinWithRequestOps<SPARQLGraphPattern, SPARQLEndpoint>
{
	public BaseForExecOpBindJoinSPARQL( final SPARQLGraphPattern p,
	                                    final SPARQLEndpoint fm,
	                                    final ExpectedVariables inputVars,
	                                    final boolean useOuterJoinSemantics,
	                                    final int batchSize,
	                                    final boolean collectExceptions ) {
		super(p, p.getAllMentionedVariables(), fm, inputVars, useOuterJoinSemantics, batchSize, collectExceptions);
	}

	// Change visibility to public in order to be able to call this function
	// directly from ExecOpBindJoinSPARQLwithVALUESorFILTER
	@Override
	public void _processBatch( final List<SolutionMapping> batchOfSolMaps,
	                           final IntermediateResultElementSink sink,
	                           final ExecutionContext execCxt )
			throws ExecOpExecutionException
	{
		super._processBatch(batchOfSolMaps, sink, execCxt);
	}

	@Override
	protected NullaryExecutableOp createExecutableReqOpForAll() {
		return new ExecOpRequestSPARQL( new SPARQLRequestImpl(query), fm, false );
	}

}
