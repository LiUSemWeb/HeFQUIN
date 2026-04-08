package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.utils.Stats;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds the hash table based on the input from the second subplan.
 */
public class ExecOpHashJoin2 extends BaseForExecOpHashJoin
{
	protected final boolean useOuterJoinSemantics;

	protected boolean child1InputComplete = false;
	protected boolean child2InputComplete = false;

	protected Stats statsOfIndex = null;

	public ExecOpHashJoin2( final ExpectedVariables inputVars1,
	                        final ExpectedVariables inputVars2,
	                        final boolean useOuterJoinSemantics,
	                        final boolean collectExceptions,
	                        final QueryPlanningInfo qpInfo,
	                        final boolean mayReduce ) {
		super(inputVars1, inputVars2, collectExceptions, qpInfo, mayReduce);

		this.useOuterJoinSemantics = useOuterJoinSemantics;
	}

	@Override
	public boolean requiresCompleteChild1InputFirst() {
		return false;
	}

	@Override
	public boolean requiresCompleteChild2InputFirst() {
		return true;
	}

	@Override
	protected void _processInputFromChild2( final SolutionMapping inputSolMap,
	                                        final IntermediateResultElementSink sink,
	                                        final ExecutionContext execCxt ) {
		index.add(inputSolMap);
	}

	@Override
	protected void _wrapUpForChild2( final IntermediateResultElementSink sink,
	                                 final ExecutionContext execCxt ) {
		this.child2InputComplete = true;
	}

	@Override
	protected void _processInputFromChild1( final SolutionMapping inputSolMap,
	                                        final IntermediateResultElementSink sink,
	                                        final ExecutionContext execCxt ) {
		if ( child2InputComplete == false ) {
			throw new IllegalStateException();
		}

		final List<SolutionMapping> output = new ArrayList<>();
		produceOutput(inputSolMap, output);

		sink.send(output);
	}

	@Override
	protected void _processInputFromChild1( final List<SolutionMapping> inputSolMaps,
	                                        final IntermediateResultElementSink sink,
	                                        final ExecutionContext execCxt ) {
		if ( child2InputComplete == false ) {
			throw new IllegalStateException();
		}

		final List<SolutionMapping> output = new ArrayList<>();
		for ( final SolutionMapping inputSolMap : inputSolMaps ) {
			produceOutput(inputSolMap, output);
		}

		sink.send(output);
	}

	@Override
	protected void _wrapUpForChild1( final IntermediateResultElementSink sink,
	                                 final ExecutionContext execCxt ) {
		child1InputComplete = true;

		// clear the index to enable the GC to release memory early,
		// but make sure we keep the final stats of the index
		statsOfIndex = index.getStats();
		index.clear();
	}

	protected void produceOutput( final SolutionMapping inputSolMap,
	                              final List<SolutionMapping> output ) {
		final Iterable<SolutionMapping> joinPartners = index.getJoinPartners(inputSolMap);
		boolean hasJoinPartner = false;
		for ( final SolutionMapping joinPartner : joinPartners ) {
			hasJoinPartner = true;
			output.add( SolutionMappingUtils.merge(joinPartner,inputSolMap) );
		}

		if ( useOuterJoinSemantics && ! hasJoinPartner ) {
			output.add(inputSolMap);
		}
	}

}
