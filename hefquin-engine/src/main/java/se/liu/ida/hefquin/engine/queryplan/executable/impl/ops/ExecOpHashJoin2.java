package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.utils.Stats;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContextExt;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds the hash table based on the input from the second subplan.
 */
public class ExecOpHashJoin2 extends BaseForExecOpHashJoin
{
	private static final Logger log = LoggerFactory.getLogger( ExecOpHashJoin2.class );
	protected final boolean useOuterJoinSemantics;

	protected boolean child1InputComplete = false;
	protected boolean child2InputComplete = false;

	protected Stats statsOfIndex = null;

	public ExecOpHashJoin2( final boolean useOuterJoinSemantics,
	                        final boolean mayReduce,
	                        final ExpectedVariables inputVars1,
	                        final ExpectedVariables inputVars2,
	                        final boolean collectExceptions,
	                        final QueryPlanningInfo qpInfo ) {
		super(mayReduce, inputVars1, inputVars2, collectExceptions, qpInfo);

		this.useOuterJoinSemantics = useOuterJoinSemantics;

		log.debug( "Initialized ExecOpHashJoin2 with useOuterJoinSemantics: {}.", useOuterJoinSemantics );
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
	                                        final QueryProcContextExt ctx ) {
		index.add(inputSolMap);
	}

	@Override
	protected void _wrapUpForChild2( final IntermediateResultElementSink sink,
	                                 final QueryProcContextExt ctx ) {
		this.child2InputComplete = true;
	}

	@Override
	protected void _processInputFromChild1( final SolutionMapping inputSolMap,
	                                        final IntermediateResultElementSink sink,
	                                        final QueryProcContextExt ctx ) {
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
	                                        final QueryProcContextExt ctx ) {
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
	                                 final QueryProcContextExt ctx ) {
		child1InputComplete = true;

		// clear the index to enable the GC to release memory early,
		// but make sure we keep the final stats of the index
		statsOfIndex = index.getStats();
		index.clear();
	}

	protected void produceOutput( final SolutionMapping inputSolMap,
	                              final List<SolutionMapping> output ) {
		final int sizeBefore = output.size();

		final Iterable<SolutionMapping> joinPartners = index.getJoinPartners(inputSolMap);
		boolean hasJoinPartner = false;
		for ( final SolutionMapping joinPartner : joinPartners ) {
			hasJoinPartner = true;
			output.add( SolutionMappingUtils.merge(joinPartner,inputSolMap) );
		}

		if ( useOuterJoinSemantics && ! hasJoinPartner ) {
			log.debug( "No join partner found. Applying outer join semantics." );
			output.add(inputSolMap);
		}
		log.debug( "Produced {} joined solution mappings.", output.size() - sizeBefore );
	}

}
