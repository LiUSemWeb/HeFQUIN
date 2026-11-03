package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import org.apache.jena.query.SortCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.utils.SolutionMappingComparator;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecutableOperatorStatsImpl;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContextExt;

/**
 * Sorts solution mappings according to the ordering conditions of an
 * ORDER BY clause. This is a blocking operator that consumes all input
 * solution mappings before producing output.
 */
public class ExecOpOrderByLimit extends UnaryExecutableOpBase
{
	private static final Logger log = LoggerFactory.getLogger( ExecOpOrderBy.class );
	private long numberOfOutputMappingsProduced = 0L;

	// We use the PriorityQueue deliberately also for the duplicate removal
	// case (i.e., if 'mayReduce' is true), instead of using TreeSet for that
	// case. TreeSet has a significantly higher memory footprint.
	protected final SolutionMappingComparator comparator;
	protected final PriorityQueue<SolutionMapping> solutionMappings;
	protected final long limit;
	protected final long offset;
	protected final long maxNumberOfMappingsToKeep;

	public ExecOpOrderByLimit( final List<SortCondition> sortConditions,
	                           final long limit,
	                           final long offset,
	                           final boolean mayReduce,
	                           final boolean collectExceptions,
	                           final QueryPlanningInfo qpInfo ) {
		super(mayReduce, collectExceptions, qpInfo);

		assert sortConditions != null;
		assert limit >= 0;
		assert offset >= 0;

		this.comparator = new SolutionMappingComparator(sortConditions);
		this.solutionMappings = new PriorityQueue<>(comparator.reversed()); // Reverse the comparator to keep the worst retained mapping at the head of the priority queue, allowing it to be efficiently replaced.
		this.limit = limit;
		this.offset = offset;
		this.maxNumberOfMappingsToKeep = limit + offset;

		log.debug( "Initialized ExecOpOrderByLimit (collectExceptions={}).", collectExceptions );
	}

	@Override
	protected void _process( final SolutionMapping inputSolMap,
	                         final IntermediateResultElementSink sink,
	                         final QueryProcContextExt ctx ) {
		if ( maxNumberOfMappingsToKeep == 0 )
			return;

		// Use PriorityQueue.contains() instead of maintaining a separate HashSet for checking duplicates.
		// This trades O(k) lookup time for lower memory overhead.
		if ( mayReduce && solutionMappings.contains(inputSolMap) )
			return;

		if ( solutionMappings.size() < maxNumberOfMappingsToKeep ) {
			solutionMappings.add(inputSolMap);
			return;
		}

		final SolutionMapping worstMapping = solutionMappings.peek();

		if ( comparator.compare(inputSolMap, worstMapping) < 0 ) {
			solutionMappings.poll();
			solutionMappings.add(inputSolMap);
		}
	}

	@Override
	protected void _concludeExecution( final IntermediateResultElementSink sink,
	                                   final QueryProcContextExt ctx ) {
		long numberOfMappingsSkipped = 0L;
		long numberOfMappingsProduced = 0L;

		final List<SolutionMapping> sortedMappings = new ArrayList<>(solutionMappings);
		sortedMappings.sort(comparator);

		for ( final SolutionMapping currentSolutionMapping : sortedMappings ) {
			if ( numberOfMappingsSkipped < offset ) {
				numberOfMappingsSkipped++;
				continue;
			}

			if ( numberOfMappingsProduced >= limit )
				break;

			sink.send(currentSolutionMapping);
			numberOfOutputMappingsProduced++;
			numberOfMappingsProduced++;
		}
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

}
