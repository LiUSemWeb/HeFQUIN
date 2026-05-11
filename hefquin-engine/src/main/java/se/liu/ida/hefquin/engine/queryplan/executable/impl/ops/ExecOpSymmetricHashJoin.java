package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import org.apache.jena.sparql.core.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.base.datastructures.SolutionMappingsIndex;
import se.liu.ida.hefquin.base.datastructures.impl.*;
import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.utils.ExpectedVariablesUtils;
import se.liu.ida.hefquin.base.utils.Stats;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecutableOperatorStatsImpl;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

import java.util.*;

/**
 * Implementation of the symmetric hash join algorithm. This algorithm
 * continuously builds two hash tables (one for each of the two inputs)
 * and, at the same time, uses these hash tables to find join partners
 * for solution mappings that arrive at the respective other input. More
 * specifically, whenever a solution mappings appears at one of the inputs,
 * it is added into the hash table for that input (using the value(s) that
 * it has for the join variable(s) to decide where to place it in the hash
 * table) and then the other hash table is probed for join partners (which
 * must have arrived at the other input before the current solution mapping
 * arrived at its input.
 */
public class ExecOpSymmetricHashJoin extends BinaryExecutableOpBase
{
	private static final Logger log = LoggerFactory.getLogger( ExecOpSymmetricHashJoin.class );
	protected final SolutionMappingsIndex indexForChild1;
	protected final SolutionMappingsIndex indexForChild2;

	protected Stats statsOfIndexForChild1 = null;
	protected Stats statsOfIndexForChild2 = null;

	protected boolean child1InputComplete = false;
	protected boolean child2InputComplete = false;

	/**
	 * This list is used to collect output solution mappings within each
	 * of the various functions of this implementation. Capturing this
	 * type of intermediate buffer as a member object rather creating
	 * a new version of it over and over again within each function
	 * reduces the number of Java objects that the garbage collector
	 * has to deal with.
	 */
	final List<SolutionMapping> buffer = new ArrayList<>();

	// statistics
	private long numberOfOutputMappingsProduced = 0L;

	public ExecOpSymmetricHashJoin( final boolean mayReduce,
	                                final ExpectedVariables inputVars1,
	                                final ExpectedVariables inputVars2,
	                                final boolean collectExceptions,
	                                final QueryPlanningInfo qpInfo ) {
		super(mayReduce, collectExceptions, qpInfo);

		// determine the certain join variables
		final Set<Var> certainJoinVars = ExpectedVariablesUtils.intersectionOfCertainVariables(inputVars1, inputVars2);

		log.info( "Initializing symmetric hash join operator with join variables {}.", certainJoinVars );

		// set up the core part of the two indexes first; it is built on the certain join variables
		SolutionMappingsIndex solMHashTableL;
		SolutionMappingsIndex solMHashTableR;
		if ( certainJoinVars.size() == 1 ) {
			final Var joinVar = certainJoinVars.iterator().next();
			log.info( "Using one-variable hash table for join variable {}.", joinVar );
			solMHashTableL = new SolutionMappingsHashTableBasedOnOneVar(joinVar);
			solMHashTableR = new SolutionMappingsHashTableBasedOnOneVar(joinVar);
		}
		else if ( certainJoinVars.size() == 2 ) {
			final Iterator<Var> liVar = certainJoinVars.iterator();
			final Var joinVar1 = liVar.next();
			final Var joinVar2 = liVar.next();

			log.info( "Using two-variable hash table for join variables {}, {}.", joinVar1, joinVar2 );

			solMHashTableL = new SolutionMappingsHashTableBasedOnTwoVars(joinVar1, joinVar2);
			solMHashTableR = new SolutionMappingsHashTableBasedOnTwoVars(joinVar1, joinVar2);
		}
		else {
			log.info( "Using generic hash table for join variables {}.", certainJoinVars );
			solMHashTableL = new SolutionMappingsHashTable(certainJoinVars);
			solMHashTableR = new SolutionMappingsHashTable(certainJoinVars);
		}

		// Check whether there are other variables that may be relevant for
		// the join and, if so, set up the indexes to use post-matching.
		final Set<Var> potentialJoinVars = ExpectedVariablesUtils.intersectionOfAllVariables(inputVars1, inputVars2);
		if ( ! potentialJoinVars.equals(certainJoinVars) ) {
			log.info( "Post-matching enabled for potential join variables {}.", potentialJoinVars );
			solMHashTableL = new SolutionMappingsIndexWithPostMatching(solMHashTableL);
			solMHashTableR = new SolutionMappingsIndexWithPostMatching(solMHashTableR);
		}

		this.indexForChild1 = new SolutionMappingsIndexForMixedUsage(solMHashTableL);
		this.indexForChild2 = new SolutionMappingsIndexForMixedUsage(solMHashTableR);
	}

	@Override
	public boolean requiresCompleteChild1InputFirst() {
		return false;
	}

	@Override
	public boolean requiresCompleteChild2InputFirst() {
		return false;
	}

	@Override
	protected void _processInputFromChild1( final SolutionMapping inputSolMap,
	                                        final IntermediateResultElementSink sink,
	                                        final ExecutionContext execCxt ) {
		log.info( "Processing solution mapping from child 1." );

		buffer.clear();

		_processInputSolMap(inputSolMap, indexForChild1, indexForChild2, buffer);

		numberOfOutputMappingsProduced += buffer.size();
		log.info( "Produced {} joined solution mappings.", buffer.size() );
		sink.send(buffer);

		buffer.clear();
	}

	@Override
	protected void _processInputFromChild1( final List<SolutionMapping> inputSolMaps,
	                                        final IntermediateResultElementSink sink,
	                                        final ExecutionContext execCxt ) {
		log.info( "Processing batch of {} solution mappings from child 1.", inputSolMaps.size() );

		buffer.clear();

		for ( final SolutionMapping inputSolMap : inputSolMaps ) {
			_processInputSolMap(inputSolMap, indexForChild1, indexForChild2, buffer);
		}

		numberOfOutputMappingsProduced += buffer.size();
		log.info( "Produced {} joined solution mappings.", buffer.size() );
		sink.send(buffer);

		buffer.clear();
	}

	@Override
	protected void _wrapUpForChild1( final IntermediateResultElementSink sink,
									 final ExecutionContext execCxt ) {
		log.info( "Completed input processing for child 1." );
		child1InputComplete = true;

		if ( child2InputComplete ) {
			wrapUp();
		}
	}

	@Override
	protected void _processInputFromChild2( final SolutionMapping inputSolMap,
	                                        final IntermediateResultElementSink sink,
	                                        final ExecutionContext execCxt ) {
		log.info( "Processing solution mapping from child 2." );

		buffer.clear();

		_processInputSolMap(inputSolMap, indexForChild2, indexForChild1, buffer);

		numberOfOutputMappingsProduced += buffer.size();
		log.info( "Produced {} joined solution mappings.", buffer.size() );
		sink.send(buffer);

		buffer.clear();
	}

	@Override
	protected void _processInputFromChild2( final List<SolutionMapping> inputSolMaps,
	                                        final IntermediateResultElementSink sink,
	                                        final ExecutionContext execCxt ) {
		log.info( "Processing batch of {} solution mappings from child 2.", inputSolMaps.size() );

		buffer.clear();

		for ( final SolutionMapping inputSolMap : inputSolMaps ) {
			_processInputSolMap(inputSolMap, indexForChild2, indexForChild1, buffer);
		}

		numberOfOutputMappingsProduced += buffer.size();
		log.info( "Produced {} joined solution mappings.", buffer.size() );
		sink.send(buffer);

		buffer.clear();
	}

	@Override
	protected void _wrapUpForChild2( final IntermediateResultElementSink sink,
	                                 final ExecutionContext execCxt ) {
		log.info( "Completed input processing for child 2." );
		child2InputComplete = true;

		if ( child1InputComplete ) {
			wrapUp();
		}
	}

	protected void wrapUp() {
		// clear both indexes to enable the GC to release memory early,
		// but make sure we keep the final stats of the indexes
		log.info( "Symmetric hash join execution completed. Clearing indexes." );

		statsOfIndexForChild1 = indexForChild1.getStats();
		statsOfIndexForChild2 = indexForChild2.getStats();
		log.info( "Final index statistics for child 1: {}.", statsOfIndexForChild1 );
		log.info( "Final index statistics for child 2: {}.", statsOfIndexForChild2 );

		indexForChild1.clear();
		indexForChild2.clear();
	}

	@Override
	public void resetStats() {
		super.resetStats();
		numberOfOutputMappingsProduced = 0L;
	}

	@Override
	protected ExecutableOperatorStatsImpl createStats() {
		final ExecutableOperatorStatsImpl s = super.createStats();
		s.put( "numberOfOutputMappingsProduced",  Long.valueOf(numberOfOutputMappingsProduced) );
		return s;
	}

	protected static void _processInputSolMap( final SolutionMapping inputSolMap,
	                                           final SolutionMappingsIndex indexForInput,
	                                           final SolutionMappingsIndex indexForProbing,
	                                           final List<SolutionMapping> outputBuffer ) {
		indexForInput.add(inputSolMap);

		final Iterable<SolutionMapping> matchingSolMaps = indexForProbing.getJoinPartners(inputSolMap);
		for ( final SolutionMapping matchingSolMap : matchingSolMaps ) {
			outputBuffer.add( SolutionMappingUtils.merge(inputSolMap, matchingSolMap) );
		}
	}

}
