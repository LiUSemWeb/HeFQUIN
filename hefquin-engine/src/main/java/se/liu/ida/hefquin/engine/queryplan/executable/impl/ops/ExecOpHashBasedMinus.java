package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;

/**
 * To be used for MINUS clauses. This operator extends {@link ExecOpHashJoin2} to
 * calculate solution mappings in the left-hand side that are not compatible with
 * the solutions on the right-hand side.
 */
public class ExecOpHashBasedMinus extends ExecOpHashJoin2
{
	private static final Logger log = LoggerFactory.getLogger( ExecOpHashBasedMinus.class );

	public ExecOpHashBasedMinus( final boolean mayReduce,
	                             final ExpectedVariables inputVars1,
	                             final ExpectedVariables inputVars2,
	                             final boolean collectExceptions,
	                             final QueryPlanningInfo qpInfo ) {
		super(true, mayReduce, inputVars1, inputVars2, collectExceptions, qpInfo);

		log.info(
			"Initialized ExecOpHashBasedMinus with mayReduce={}, inputVarsLeft={}, inputVarsRight={}",
			mayReduce,
			inputVars1,
			inputVars2 );
	}

	@Override
	protected void produceOutput( final SolutionMapping inputSolMap,
	                              final List<SolutionMapping> output) {
		final Iterable<SolutionMapping> joinPartners = index.getJoinPartners(inputSolMap);
		final Iterator<SolutionMapping> it = joinPartners.iterator();

		if ( ! it.hasNext() ) {
			output.add(inputSolMap);
		}
	}

}
