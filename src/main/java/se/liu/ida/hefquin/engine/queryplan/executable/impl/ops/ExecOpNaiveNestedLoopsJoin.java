package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Attention, this is not actually a (distributed) nested loops join.
 * Instead, this algorithm first consumes the whole first input and
 * materializes it into a list. Thereafter, it performs a local nested
 * loops join in which the outer loop iterates over the second input
 * and the inner loop (repeatedly) iterates over the list with the
 * first input.
 * It is certainly better to use the {@link ExecOpHashJoin} instead.
 * Instead of simply putting all left-input solution mappings into a
 * list, the hash join puts them into a hash index which can than be
 * probed into for each right-input solution mapping.
 */
public class ExecOpNaiveNestedLoopsJoin extends BinaryExecutableOpBase
{
    protected final List<SolutionMapping> inputLHS = new ArrayList<>();

    @Override
    public int preferredInputBlockSizeFromChild1() {
        // Since this algorithm processes the input solution mappings
        // sequentially (one at a time), and input block size of 1 may
        // reduce the response time of the overall execution process.
        return 1;
    }

    @Override
    public int preferredInputBlockSizeFromChild2() {
        // same rationale here
        return 1;
    }

    @Override
    public boolean requiresCompleteChild1InputFirst() {
        return true;
    }

    @Override
    protected void _processBlockFromChild1( final IntermediateResultBlock input, final IntermediateResultElementSink sink, final ExecutionContext execCxt ) {
        for ( final SolutionMapping sm : input.getSolutionMappings() ){
            inputLHS.add(sm);
        }
    }

    @Override
    protected void _wrapUpForChild1( final IntermediateResultElementSink sink, final ExecutionContext execCxt ) {
        // nothing to be done here
    }

    @Override
    protected void _processBlockFromChild2( final IntermediateResultBlock input, final IntermediateResultElementSink sink, final ExecutionContext execCxt ) {
        for ( final SolutionMapping smR : input.getSolutionMappings() ) {
            for ( final SolutionMapping smL : inputLHS ) {
                if ( SolutionMappingUtils.compatible(smL,smR) ) {
                    sink.send( SolutionMappingUtils.merge(smL,smR) );
                }
            }
        }
    }

    @Override
    protected void _wrapUpForChild2( final IntermediateResultElementSink sink, final ExecutionContext execCxt ) {
        // nothing to be done here
    }
}
