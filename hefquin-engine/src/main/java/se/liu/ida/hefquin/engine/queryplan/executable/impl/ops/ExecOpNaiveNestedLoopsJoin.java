package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Attention, this is a purely local implementation of the nested loops
 * join algorithm---nothing fancy, no requests to federation members or
 * anything. Instead, this algorithm first consumes the whole first input
 * and materializes it into a list. Thereafter, it performs a local nested
 * loops join in which the outer loop iterates over the second input and
 * the inner loop (repeatedly) iterates over the list with the first input.
 *
 * It is certainly better to use the {@link ExecOpHashJoin} instead. Instead 
 * of simply putting all left-input solution mappings into a list, the hash
 * join puts them into a hash index which can than be probed into for each
 *  right-input solution mapping.
 */
public class ExecOpNaiveNestedLoopsJoin extends BinaryExecutableOpBase
{
	protected final List<SolutionMapping> inputLHS = new ArrayList<>();

	public ExecOpNaiveNestedLoopsJoin( final boolean collectExceptions ) {
		super(collectExceptions);
	}

	@Override
	public boolean requiresCompleteChild1InputFirst() {
		return true;
	}

	@Override
	protected void _processInputFromChild1( final SolutionMapping inputSolMap,
	                                        final IntermediateResultElementSink sink,
	                                        final ExecutionContext execCxt ) {
		inputLHS.add(inputSolMap);
	}

	@Override
	protected void _wrapUpForChild1( final IntermediateResultElementSink sink,
	                                 final ExecutionContext execCxt ) {
		// nothing to be done here
	}

	@Override
	protected void _processInputFromChild2( final SolutionMapping inputSolMap,
	                                        final IntermediateResultElementSink sink,
	                                        final ExecutionContext execCxt ) {
		final List<SolutionMapping> output = new ArrayList<>();
		for ( final SolutionMapping smL : inputLHS ) {
			if ( SolutionMappingUtils.compatible(smL,inputSolMap) ) {
				output.add( SolutionMappingUtils.merge(smL,inputSolMap) );
			}
		}

		sink.send(output);
	}

	@Override
	protected void _processInputFromChild2( final List<SolutionMapping> inputSolMaps,
	                                        final IntermediateResultElementSink sink,
	                                        final ExecutionContext execCxt ) {
		final List<SolutionMapping> output = new ArrayList<>();
		for ( final SolutionMapping inputSolMap : inputSolMaps ) {
			for ( final SolutionMapping smL : inputLHS ) {
				if ( SolutionMappingUtils.compatible(smL,inputSolMap) ) {
					output.add( SolutionMappingUtils.merge(smL,inputSolMap) );
				}
			}
		}

		sink.send(output);
	}

	@Override
	protected void _wrapUpForChild2( final IntermediateResultElementSink sink,
	                                 final ExecutionContext execCxt ) {
		// clear the list of collected first-input solution
		// mappings to enable the GC to release memory early
		inputLHS.clear();
	}

}
