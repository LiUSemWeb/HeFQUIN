package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.List;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecutableOperatorStatsImpl;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public class ExecOpMultiwayUnion extends NaryExecutableOpBase
{
	private long numberOfOutputMappingsProduced = 0L;

	public ExecOpMultiwayUnion( final int numberOfChildren,
	                            final boolean collectExceptions,
	                            final QueryPlanningInfo qpInfo ) {
		super(numberOfChildren, collectExceptions, qpInfo);
	}

	@Override
	protected void _processInputFromXthChild( final int x,
	                                          final SolutionMapping inputSolMap,
	                                          final IntermediateResultElementSink sink,
	                                          final ExecutionContext execCxt) {
		numberOfOutputMappingsProduced++;
		sink.send(inputSolMap);
	}

	@Override
	protected void _processInputFromXthChild( final int x,
	                                          final List<SolutionMapping> inputSolMaps,
	                                          final IntermediateResultElementSink sink,
	                                          final ExecutionContext execCxt) {
		numberOfOutputMappingsProduced += inputSolMaps.size();
		sink.send(inputSolMaps);
	}

	@Override
	protected void _wrapUpForXthChild( final int x,
	                                   final IntermediateResultElementSink sink,
	                                   final ExecutionContext execCxt ) {
		// nothing to be done here
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
