package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.HashSet;

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
    private long numberOfOutputMappingsProduced = 0L;

    protected HashSet<SolutionMapping> distinctSolMaps = new HashSet<>();

	public ExecOpDuplicateRemoval(
	                     final boolean collectExceptions,
	                     final QueryPlanningInfo qpInfo ) {
		super(collectExceptions, qpInfo);
	}

	@Override
	protected void _process( final SolutionMapping inputSolMap,
	                         final IntermediateResultElementSink sink,
	                         final ExecutionContext execCxt ) {
        distinctSolMaps.add(inputSolMap);
	}

	@Override
	protected void _concludeExecution( final IntermediateResultElementSink sink,
	                                   final ExecutionContext execCxt ) {
		sink.send(distinctSolMaps);
		numberOfOutputMappingsProduced = distinctSolMaps.size();
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
