package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.SortCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.utils.SolutionMappingComparator;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
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
	private final List<SortCondition> sortConditions;
	private long numberOfOutputMappingsProduced = 0L;

	protected final List<SolutionMapping> solutionMappings = new ArrayList<>();

	public ExecOpOrderBy( final List<SortCondition> sortConditions,
	                      final boolean mayReduce,
	                      final boolean collectExceptions,
	                      final QueryPlanningInfo qpInfo ) {
		super(mayReduce, collectExceptions, qpInfo);

		this.sortConditions = sortConditions;

		log.debug( "Initialized ExecOpOrderBy (collectExceptions={}).", collectExceptions );
	}

	@Override
	protected void _process( final SolutionMapping inputSolMap,
	                         final IntermediateResultElementSink sink,
	                         final QueryProcContextExt ctx )
			throws ExecOpExecutionException {
		solutionMappings.add(inputSolMap);
	}

	@Override
	protected void _concludeExecution( final IntermediateResultElementSink sink,
	                                   final QueryProcContextExt ctx )
			throws ExecOpExecutionException {
		solutionMappings.sort(new SolutionMappingComparator(sortConditions));

		for ( final SolutionMapping solutionMapping : solutionMappings ) {
			sink.send(solutionMapping);
			numberOfOutputMappingsProduced++;
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
