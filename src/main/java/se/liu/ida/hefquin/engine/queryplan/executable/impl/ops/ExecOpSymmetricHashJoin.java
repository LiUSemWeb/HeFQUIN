package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import org.apache.jena.sparql.core.Var;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.datastructures.SolutionMappingsIndex;
import se.liu.ida.hefquin.engine.datastructures.impl.*;
import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.utils.ExpectedVariablesUtils;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

import java.util.*;

public class ExecOpSymmetricHashJoin extends BinaryExecutableOpBase
{

    protected final SolutionMappingsIndex indexForChild1;
    protected final SolutionMappingsIndex indexForChild2;

    public ExecOpSymmetricHashJoin( final ExpectedVariables inputVars1, final ExpectedVariables inputVars2 ) {
        // determine the certain join variables
        final Set<Var> certainJoinVars = ExpectedVariablesUtils.intersectionOfCertainVariables(inputVars1, inputVars2);

        // set up the core part of the two indexes first; it is built on the certain join variables
        SolutionMappingsIndex solMHashTableL;
        SolutionMappingsIndex solMHashTableR;
        if ( certainJoinVars.size() == 1 ) {
            final Var joinVar = certainJoinVars.iterator().next();
            solMHashTableL = new SolutionMappingsHashTableBasedOnOneVar(joinVar);
            solMHashTableR = new SolutionMappingsHashTableBasedOnOneVar(joinVar);
        }
        else if ( certainJoinVars.size() == 2 ) {
            final Iterator<Var> liVar = certainJoinVars.iterator();
            final Var joinVar1 = liVar.next();
            final Var joinVar2 = liVar.next();

            solMHashTableL = new SolutionMappingsHashTableBasedOnTwoVars(joinVar1, joinVar2);
            solMHashTableR = new SolutionMappingsHashTableBasedOnTwoVars(joinVar1, joinVar2);
        }
        else{
            solMHashTableL = new SolutionMappingsHashTable(certainJoinVars);
            solMHashTableR = new SolutionMappingsHashTable(certainJoinVars);
        }

        // Check whether there are other variables that may be relevant for
        // the join and, if so, set up the indexes to use post-matching.
        final Set<Var> potentialJoinVars = ExpectedVariablesUtils.intersectionOfAllVariables(inputVars1, inputVars2);
        if ( ! potentialJoinVars.equals(certainJoinVars) ) {
            solMHashTableL = new SolutionMappingsIndexWithPostMatching(solMHashTableL);
            solMHashTableR = new SolutionMappingsIndexWithPostMatching(solMHashTableR);
        }

        this.indexForChild1 = new SolutionMappingsIndexForMixedUsage(solMHashTableL);
        this.indexForChild2 = new SolutionMappingsIndexForMixedUsage(solMHashTableR);
    }

    @Override
    public int preferredInputBlockSizeFromChild1() {
        return 1;
    }

    @Override
    public int preferredInputBlockSizeFromChild2() {
        return 1;
    }

    @Override
    public boolean requiresCompleteChild1InputFirst() {
        return false;
    }

    @Override
    protected void _processBlockFromChild1( final IntermediateResultBlock input, final IntermediateResultElementSink sink, final ExecutionContext execCxt) {
        for ( final SolutionMapping smL : input.getSolutionMappings() ) {
            indexForChild1.add(smL);

            final Iterable<SolutionMapping> matchSolMapR = indexForChild2.getJoinPartners(smL);
            for ( final SolutionMapping smR : matchSolMapR ){
                sink.send(SolutionMappingUtils.merge(smL, smR));
            }
        }
    }

    @Override
    protected void _wrapUpForChild1(IntermediateResultElementSink sink, ExecutionContext execCxt) {
        // nothing to do here
    }

    @Override
    protected void _processBlockFromChild2(IntermediateResultBlock input, IntermediateResultElementSink sink, ExecutionContext execCxt) {
        for ( final SolutionMapping smR : input.getSolutionMappings() ) {
            indexForChild2.add(smR);

            final Iterable<SolutionMapping> matchSolMapL = indexForChild1.getJoinPartners(smR);
            for ( final SolutionMapping smL : matchSolMapL ){
                sink.send(SolutionMappingUtils.merge(smL, smR));
            }
        }
    }

    @Override
    protected void _wrapUpForChild2(IntermediateResultElementSink sink, ExecutionContext execCxt) {
        // nothing to do here
    }
}
