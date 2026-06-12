package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecutableOperatorStatsImpl;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContextExt;

public class ExecOpBinaryUnion extends BinaryExecutableOpBase
{
	private static final Logger log = LoggerFactory.getLogger( ExecOpBinaryUnion.class );
	private long numberOfOutputMappingsProduced = 0L;

	public ExecOpBinaryUnion( final boolean mayReduce,
	                          final boolean collectExceptions,
	                          final QueryPlanningInfo qpInfo ) {
		super(mayReduce, collectExceptions, qpInfo);

		log.debug(
			"Initialized ExecOpBinaryUnion (mayReduce={}, collectExceptions={}).",
			mayReduce,
			collectExceptions );
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
	                                        final QueryProcContextExt ctx ) {
		numberOfOutputMappingsProduced++;
		sink.send(inputSolMap);
	}

	@Override
	protected void _processInputFromChild1( final List<SolutionMapping> inputSolMaps,
	                                        final IntermediateResultElementSink sink,
	                                        final QueryProcContextExt ctx ) {
		numberOfOutputMappingsProduced += inputSolMaps.size();
		sink.send(inputSolMaps);
	}

	@Override
	protected void _wrapUpForChild1( final IntermediateResultElementSink sink,
	                                 final QueryProcContextExt ctx ) {
		// nothing to be done here
	}

	@Override
	protected void _processInputFromChild2( final SolutionMapping inputSolMap,
	                                        final IntermediateResultElementSink sink,
	                                        final QueryProcContextExt ctx ) {
		numberOfOutputMappingsProduced++;
		sink.send(inputSolMap);
	}

	@Override
	protected void _processInputFromChild2( final List<SolutionMapping> inputSolMaps,
	                                        final IntermediateResultElementSink sink,
	                                        final QueryProcContextExt ctx ) {
		numberOfOutputMappingsProduced += inputSolMaps.size();
		sink.send(inputSolMaps);
	}

	@Override
	protected void _wrapUpForChild2( final IntermediateResultElementSink sink,
	                                 final QueryProcContextExt ctx ) {
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
