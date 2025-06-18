package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;

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

	@Override
	protected NullaryExecutableOp createExecutableReqOpForAll() {
		return new ExecOpRequestSPARQL( new SPARQLRequestImpl(query), fm, false );
	}

}
