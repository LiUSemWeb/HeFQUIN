package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.base.datastructures.SolutionMappingsIndex;
import se.liu.ida.hefquin.base.datastructures.impl.*;
import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.utils.ExpectedVariablesUtils;
import se.liu.ida.hefquin.base.utils.Stats;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Builds the hash table based on the input from the second subplan.
 */
public class ExecOpHashJoin2 extends BinaryExecutableOpBase
{
	protected final SolutionMappingsIndex index;
	protected Stats statsOfIndex = null;

	protected boolean child1InputComplete = false;
	protected boolean child2InputComplete = false;

	public ExecOpHashJoin2( final ExpectedVariables inputVars1,
	                        final ExpectedVariables inputVars2,
	                        final boolean collectExceptions,
	                        final QueryPlanningInfo qpInfo ) {
		super(collectExceptions, qpInfo);

		// determine the certain join variables
		final Set<Var> certainJoinVars = ExpectedVariablesUtils.intersectionOfCertainVariables(inputVars1, inputVars2);

		// set up the core part of the index first;
		// it is built on the certain join variables
		final SolutionMappingsIndex _index;
		if ( certainJoinVars.size() == 1 ) {
			final Var joinVar = certainJoinVars.iterator().next();
			_index = new SolutionMappingsHashTableBasedOnOneVar(joinVar);
		}
		else if ( certainJoinVars.size() == 2 ) {
			final Iterator<Var> liVar = certainJoinVars.iterator();
			final Var joinVar1 = liVar.next();
			final Var joinVar2 = liVar.next();
			_index = new SolutionMappingsHashTableBasedOnTwoVars(joinVar1, joinVar2);
		}
		else {
			_index = new SolutionMappingsHashTable(certainJoinVars);
		}

		// Check whether there are other variables that may be relevant for
		// the join and, if so, set up the index to use post-matching.
		final Set<Var> potentialJoinVars = ExpectedVariablesUtils.intersectionOfAllVariables(inputVars1, inputVars2);
		if ( ! potentialJoinVars.equals(certainJoinVars) )
			index = new SolutionMappingsIndexWithPostMatching(_index);
		else
			index = _index;
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
		for ( final SolutionMapping joinPartner : joinPartners ) {
			output.add( SolutionMappingUtils.merge(joinPartner,inputSolMap) );
		}
	}

}
