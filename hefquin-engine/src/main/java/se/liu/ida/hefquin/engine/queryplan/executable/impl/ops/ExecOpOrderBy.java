package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

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
public class ExecOpOrderBy extends UnaryExecutableOpBase
{
	private static final Logger log = LoggerFactory.getLogger( ExecOpOrderBy.class );
	private long numberOfOutputMappingsProduced = 0L;

	// We use the PriorityQueue deliberately also for the duplicate removal
	// case (i.e., if 'mayReduce' is true), instead of using TreeSet for that
	// case. TreeSet has a significantly higher memory footprint.
	protected final PriorityQueue<SolutionMapping> solutionMappings;

	public ExecOpOrderBy( final List<SortCondition> sortConditions,
	                      final boolean mayReduce,
	                      final boolean collectExceptions,
	                      final QueryPlanningInfo qpInfo ) {
		super(mayReduce, collectExceptions, qpInfo);

		assert sortConditions != null;
		this.solutionMappings = new PriorityQueue<>(new SolutionMappingComparator(sortConditions));

		log.debug( "Initialized ExecOpOrderBy (collectExceptions={}).", collectExceptions );
	}

	@Override
	protected void _process( final SolutionMapping inputSolMap,
	                         final IntermediateResultElementSink sink,
	                         final QueryProcContextExt ctx ) {
		solutionMappings.add(inputSolMap);
	}

	@Override
	protected void _concludeExecution( final IntermediateResultElementSink sink,
	                                   final QueryProcContextExt ctx ) {
		SolutionMapping previousSolutionMapping = null;

		// PriorityQueue iteration does not guarantee sorted order.
		// We must use poll() to retrieve mappings in priority order.
		while ( ! solutionMappings.isEmpty() ) {
			final SolutionMapping currentSolutionMapping = solutionMappings.poll();

			if ( mayReduce && currentSolutionMapping.equals(previousSolutionMapping) ) {
				continue;
			}

			sink.send(currentSolutionMapping);
			numberOfOutputMappingsProduced++;

			previousSolutionMapping = currentSolutionMapping;
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
