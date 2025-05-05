package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

/**
 * A right outer join version of the hash join algorithm implemented in
 * {@link ExecOpHashJoin}. The only difference is that, when consuming
 * the second input, for every solution mapping that does not have any
 * join partners in the hash index (that was created when consuming the
 * first input), the solution mapping is not simply discarded (as required
 * by the inner join semantics implemented by the base algorithm) but it
 * is sent to the output as is (to comply with the right join semantics).
 * <br/>
 * <br/>
 * Attention: if this algorithm is used to implement the OPTIONAL operator,
 *            the OPTIONAL part must be used as the first input.
 */
public class ExecOpHashRJoin extends ExecOpHashJoin {

	public ExecOpHashRJoin( final ExpectedVariables inputVars1,
	                        final ExpectedVariables inputVars2,
	                        final boolean collectExceptions ) {
		super(inputVars1, inputVars2, collectExceptions);
	}

	@Override
	protected void _processInputFromChild2( final SolutionMapping inputSolMap,
	                                        final IntermediateResultElementSink sink,
	                                        final ExecutionContext execCxt ) {
		if ( child1InputComplete == false ) {
			throw new IllegalStateException();
		}

		boolean hasJoinPartner = false;
		final Iterable<SolutionMapping> matchSolMapL = index.getJoinPartners(inputSolMap);
		for ( final SolutionMapping smL : matchSolMapL ){
			hasJoinPartner = true;
			sink.send(SolutionMappingUtils.merge(smL, inputSolMap));
		}

		if ( ! hasJoinPartner ) {
			sink.send(inputSolMap);
		}
	}
}
