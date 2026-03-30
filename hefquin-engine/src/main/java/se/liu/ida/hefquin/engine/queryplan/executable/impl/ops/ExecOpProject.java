package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.Iterator;
import java.util.List;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecutableOperatorStatsImpl;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public class ExecOpProject extends UnaryExecutableOpBaseWithoutBlocking
{
	private long numberOfOutputMappingsProduced = 0L;

	protected final List<Var> variables;

	public ExecOpProject( final List<Var> variables,
	                      final boolean collectExceptions,
	                      final QueryPlanningInfo qpInfo ) {
		super(collectExceptions, qpInfo);

		assert variables != null;
		assert ! variables.isEmpty();

		this.variables = variables;
	}

	@Override
	protected void _process( final SolutionMapping inputSolMap,
	                         final IntermediateResultElementSink sink,
	                         final ExecutionContext execCxt )
			throws ExecOpExecutionException {
		final SolutionMapping outputSolMap = SolutionMappingUtils.restrict(inputSolMap, variables);
		sink.send(outputSolMap);
		numberOfOutputMappingsProduced++;
	}

	@Override
	protected void _process( final Iterator<SolutionMapping> inputSolMap,
	                         final int maxBatchSize,
	                         final IntermediateResultElementSink sink,
	                         final ExecutionContext execCxt )
			throws ExecOpExecutionException {
		// go through the list of solution mappings, keeping wanted variables but discarding the rest
	}

	@Override
	protected void _concludeExecution( final IntermediateResultElementSink sink,
	                                   final ExecutionContext execCxt )
			throws ExecOpExecutionException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method '_concludeExecution'");
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