package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecutableOperatorStatsImpl;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

/**
 * To be used for DISTINCT clauses. This algorithm removes duplicates by collecting
 * input solution mappings in the same hash set. Passes only distinct solution mappings
 * to its output. Duplicates are based on SolutionMapping equality, not object identity.
 */
public class ExecOpDuplicateRemoval extends UnaryExecutableOpBase
{
	private static final Logger log = LoggerFactory.getLogger( ExecOpDuplicateRemoval.class );
	private long numberOfOutputMappingsProduced = 0L;
	private long numberOfDuplicates = 0L;

	protected final Set<SolutionMapping> distinctSolMaps = new HashSet<>();

	public ExecOpDuplicateRemoval(
	                     final boolean collectExceptions,
	                     final QueryPlanningInfo qpInfo ) {
		super(true, collectExceptions, qpInfo);

		log.info( "Initialized ExecOpDuplicateRemoval (collectExceptions={}).", collectExceptions );
	}

	@Override
	protected void _process( final SolutionMapping inputSolMap,
	                         final IntermediateResultElementSink sink,
	                         final ExecutionContext execCxt ) {
		if ( distinctSolMaps.add(inputSolMap) ) {
			sink.send(inputSolMap);
			numberOfOutputMappingsProduced++;
		}
		else numberOfDuplicates++;
	}

	@Override
	protected void _concludeExecution( final IntermediateResultElementSink sink,
	                                   final ExecutionContext execCxt ) {
		log.info(
			"Distinct operator finishing. Produced={}, duplicates={}, uniqueSize={}.",
			numberOfOutputMappingsProduced,
			numberOfDuplicates,
			distinctSolMaps.size() );

		distinctSolMaps.clear();
	}

	@Override
	public void resetStats() {
		super.resetStats();
		numberOfOutputMappingsProduced = 0L;
		numberOfDuplicates = 0L;
	}

	@Override
	protected ExecutableOperatorStatsImpl createStats() {
		final ExecutableOperatorStatsImpl s = super.createStats();
		s.put( "numberOfOutputMappingsProduced",  Long.valueOf(numberOfOutputMappingsProduced) );
		s.put( "numberOfDuplicates", Long.valueOf(numberOfDuplicates) );
		return s;
	}

}
