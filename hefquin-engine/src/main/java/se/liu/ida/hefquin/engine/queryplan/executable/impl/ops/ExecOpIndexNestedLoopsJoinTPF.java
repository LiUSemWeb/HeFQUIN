package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.VariableByBlankNodeSubstitutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.federation.access.impl.req.TriplePatternRequestImpl;
import se.liu.ida.hefquin.federation.members.BRTPFServer;
import se.liu.ida.hefquin.federation.members.TPFServer;

/**
 * May be used for brTPF servers as well (in fact, {@link BRTPFServer}
 * is a specialization of {@link TPFServer}).
 */
public class ExecOpIndexNestedLoopsJoinTPF
           extends BaseForExecOpIndexNestedLoopsJoinWithRequestOps<TriplePattern,TPFServer>
{
	public ExecOpIndexNestedLoopsJoinTPF( final TriplePattern query,
	                                      final TPFServer fm,
	                                      final boolean useOuterJoinSemantics,
	                                      final boolean collectExceptions,
	                                      final QueryPlanningInfo qpInfo ) {
		super(query, fm, useOuterJoinSemantics, collectExceptions, qpInfo);
	}

	@Override
	protected NullaryExecutableOp createExecutableRequestOperator( final SolutionMapping inputSolMap )
			throws VariableByBlankNodeSubstitutionException
	{
		final TriplePatternRequest req = createRequest(inputSolMap);
		return new ExecOpRequestTPF<>(req, fm, false, null);
	}

	protected TriplePatternRequest createRequest( final SolutionMapping inputSolMap )
			throws VariableByBlankNodeSubstitutionException
	{
		final TriplePattern tp = query.applySolMapToGraphPattern(inputSolMap);
		return new TriplePatternRequestImpl(tp);
	}
}
