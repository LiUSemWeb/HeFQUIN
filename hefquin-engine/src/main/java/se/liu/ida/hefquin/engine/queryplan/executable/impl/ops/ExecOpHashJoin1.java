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
 * Builds the hash table based on the input from the first subplan.
 */
public class ExecOpHashJoin1 extends BaseForExecOpHashJoin
{
	protected Stats statsOfIndex = null;

	protected boolean child1InputComplete = false;
	protected boolean child2InputComplete = false;

	public ExecOpHashJoin1( final boolean mayReduce,
	                        final ExpectedVariables inputVars1,
	                        final ExpectedVariables inputVars2,
	                        final boolean collectExceptions,
	                        final QueryPlanningInfo qpInfo ) {
		super(mayReduce, inputVars1, inputVars2, collectExceptions, qpInfo);
	}

	@Override
	public boolean requiresCompleteChild1InputFirst() {
		return true;
	}

	@Override
	public boolean requiresCompleteChild2InputFirst() {
		return false;
	}

	@Override
	protected void _processInputFromChild1( final SolutionMapping inputSolMap,
	                                        final IntermediateResultElementSink sink,
	                                        final ExecutionContext execCxt ) {
		index.add(inputSolMap);
	}

	@Override
	protected void _wrapUpForChild1( final IntermediateResultElementSink sink,
	                                 final ExecutionContext execCxt ) {
		this.child1InputComplete = true;
	}

	@Override
	protected void _processInputFromChild2( final SolutionMapping inputSolMap,
	                                        final IntermediateResultElementSink sink,
	                                        final ExecutionContext execCxt ) {
		if ( child1InputComplete == false ) {
			throw new IllegalStateException();
		}

		final List<SolutionMapping> output = new ArrayList<>();
		produceOutput(inputSolMap, output);

		sink.send(output);
	}

	@Override
	protected void _processInputFromChild2( final List<SolutionMapping> inputSolMaps,
	                                        final IntermediateResultElementSink sink,
	                                        final ExecutionContext execCxt ) {
		if ( child1InputComplete == false ) {
			throw new IllegalStateException();
		}

		final List<SolutionMapping> output = new ArrayList<>();
		for ( final SolutionMapping inputSolMap : inputSolMaps ) {
			produceOutput(inputSolMap, output);
		}

		sink.send(output);
	}

	@Override
	protected void _wrapUpForChild2( final IntermediateResultElementSink sink,
	                                 final ExecutionContext execCxt ) {
		child2InputComplete = true;

		// clear the index to enable the GC to release memory early,
		// but make sure we keep the final stats of the index
		statsOfIndex = index.getStats();
		index.clear();
	}

	protected void produceOutput( final SolutionMapping inputSolMap,
	                              final List<SolutionMapping> output ) {
		final Iterable<SolutionMapping> matchSolMapL = index.getJoinPartners(inputSolMap);
		for ( final SolutionMapping smL : matchSolMapL ){
			output.add( SolutionMappingUtils.merge(smL,inputSolMap) );
		}
	}

}
