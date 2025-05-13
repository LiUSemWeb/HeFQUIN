package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.base.datastructures.SolutionMappingsIndex;
import se.liu.ida.hefquin.base.datastructures.impl.*;
import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.utils.ExpectedVariablesUtils;
import se.liu.ida.hefquin.base.utils.Stats;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ExecOpHashJoin extends BinaryExecutableOpBase
{
    protected final SolutionMappingsIndex index;
    protected Stats statsOfIndex = null;

    protected boolean child1InputComplete = false;
    protected boolean child2InputComplete = false;

    public ExecOpHashJoin( final ExpectedVariables inputVars1,
                           final ExpectedVariables inputVars2,
                           final boolean collectExceptions ) {
        super(collectExceptions);

        // determine the certain join variables
        final Set<Var> certainJoinVars = ExpectedVariablesUtils.intersectionOfCertainVariables(inputVars1, inputVars2);

        // set up the core part of the index first; it is built on the certain join variables
        final SolutionMappingsIndex index1;
        if ( certainJoinVars.size() == 1 ) {
            final Var joinVar = certainJoinVars.iterator().next();
            index1 = new SolutionMappingsHashTableBasedOnOneVar(joinVar);
        }
        else if ( certainJoinVars.size() == 2 ) {
            final Iterator<Var> liVar = certainJoinVars.iterator();
            final Var joinVar1 = liVar.next();
            final Var joinVar2 = liVar.next();
        	index1 = new SolutionMappingsHashTableBasedOnTwoVars(joinVar1, joinVar2);
        }
        else {
            index1 = new SolutionMappingsHashTable(certainJoinVars);
        }

        // Check whether there are other variables that may be relevant for
        // the join and, if so, set up the index to use post-matching.
        final Set<Var> potentialJoinVars = ExpectedVariablesUtils.intersectionOfAllVariables(inputVars1, inputVars2);
        if ( ! potentialJoinVars.equals(certainJoinVars) ) {
            index = new SolutionMappingsIndexWithPostMatching(index1);
        } else {
            index = index1;
        }
    }

    @Override
    public boolean requiresCompleteChild1InputFirst() {
        return true;
    }

    @Override
    protected void _processInputFromChild1( final SolutionMapping inputSolMap,
                                            final IntermediateResultElementSink sink,
                                            final ExecutionContext execCxt ) {
        index.add(inputSolMap);
    }

    @Override
    protected void _wrapUpForChild1( final IntermediateResultElementSink sink,
                                     final ExecutionContext execCxt ) {
        this.child1InputComplete = true;
    }

    @Override
    protected void _processInputFromChild2( final SolutionMapping inputSolMap,
                                            final IntermediateResultElementSink sink,
                                            final ExecutionContext execCxt ) {
        if ( child1InputComplete == false ) {
            throw new IllegalStateException();
        }

        final List<SolutionMapping> output = new ArrayList<>();
        produceOutput(inputSolMap, output);

        sink.send(output);
    }

    @Override
    protected void _processInputFromChild2( final List<SolutionMapping> inputSolMaps,
                                            final IntermediateResultElementSink sink,
                                            final ExecutionContext execCxt ) {
        if ( child1InputComplete == false ) {
            throw new IllegalStateException();
        }

        final List<SolutionMapping> output = new ArrayList<>();
        for ( final SolutionMapping inputSolMap : inputSolMaps ) {
            produceOutput(inputSolMap, output);
        }

        sink.send(output);
    }

    @Override
    protected void _wrapUpForChild2( final IntermediateResultElementSink sink,
                                     final ExecutionContext execCxt ) {
        child2InputComplete = true;

        // clear the index to enable the GC to release memory early,
        // but make sure we keep the final stats of the index
        statsOfIndex = index.getStats();
        index.clear();
    }

    protected void produceOutput( final SolutionMapping inputSolMap,
                                  final List<SolutionMapping> output ) {
        final Iterable<SolutionMapping> matchSolMapL = index.getJoinPartners(inputSolMap);
        for ( final SolutionMapping smL : matchSolMapL ){
            output.add( SolutionMappingUtils.merge(smL,inputSolMap) );
        }
    }

}
