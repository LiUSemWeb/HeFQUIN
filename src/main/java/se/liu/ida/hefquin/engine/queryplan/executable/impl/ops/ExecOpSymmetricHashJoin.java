package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import org.apache.jena.sparql.core.Var;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.impl.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.datastructures.SolutionMappingsIndex;
import se.liu.ida.hefquin.engine.datastructures.impl.*;
import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

import java.util.*;

public class ExecOpSymmetricHashJoin implements BinaryExecutableOp{

    protected final SolutionMappingsIndex solMWithMatchingL;
    protected final SolutionMappingsIndex solMWithMatchingR;

    public ExecOpSymmetricHashJoin( final ExpectedVariables inputVars1, final ExpectedVariables inputVars2 ){
        final List<Var> joinVars = new ArrayList<>( inputVars1.getCertainVariables() );
        joinVars.retainAll( inputVars2.getCertainVariables() );

        SolutionMappingsIndex solMHashTableL;
        SolutionMappingsIndex solMHashTableR;
        if (joinVars.size() == 1 ){
            final Var joinVar = joinVars.iterator().next();
            solMHashTableL = new SolutionMappingsHashTableBasedOnOneVar(joinVar);
            solMHashTableR = new SolutionMappingsHashTableBasedOnOneVar(joinVar);
        }
        else if (joinVars.size() == 2){
            final Iterator<Var> liVar = joinVars.iterator();
            final Var joinVar1 = liVar.next();
            final Var joinVar2 = liVar.next();

            solMHashTableL = new SolutionMappingsHashTableBasedOnTwoVars(joinVar1, joinVar2);
            solMHashTableR = new SolutionMappingsHashTableBasedOnTwoVars(joinVar1, joinVar2);
        }
        else{
            solMHashTableL = new SolutionMappingsHashTable(joinVars);
            solMHashTableR = new SolutionMappingsHashTable(joinVars);
        }

        final List<Var> vars1 = new ArrayList<>( inputVars1.getCertainVariables() );
        vars1.addAll(inputVars1.getPossibleVariables());
        final List<Var> vars2 = new ArrayList<>( inputVars2.getCertainVariables() );
        vars2.addAll(inputVars2.getPossibleVariables());

        if(inputVars1.getPossibleVariables().removeAll(vars2))
            solMHashTableR = new SolutionMappingsIndexWithPostMatching(solMHashTableR);
        if (inputVars2.getPossibleVariables().removeAll(vars1))
            solMHashTableL = new SolutionMappingsIndexWithPostMatching(solMHashTableL);
        this.solMWithMatchingL = new SolutionMappingsIndexForMixedUsage(solMHashTableL);
        this.solMWithMatchingR = new SolutionMappingsIndexForMixedUsage(solMHashTableR);
    }

    @Override
    public int preferredInputBlockSize() {
        return 1;
    }

    @Override
    public boolean requiresCompleteChild1InputFirst() {
        return false;
    }

    @Override
    public void processBlockFromChild1( final IntermediateResultBlock input, final IntermediateResultElementSink sink, final ExecutionContext execCxt) {
        for ( final SolutionMapping smL : input.getSolutionMappings() ) {
            solMWithMatchingL.add(smL);

            final Iterable<SolutionMapping> matchSolMapR = solMWithMatchingR.getJoinPartners(smL);
            for ( final SolutionMapping smR : matchSolMapR ){
                sink.send(SolutionMappingUtils.merge(smL, smR));
            }
        }
    }

    @Override
    public void wrapUpForChild1(IntermediateResultElementSink sink, ExecutionContext execCxt) {
        // nothing to do here
    }

    @Override
    public void processBlockFromChild2(IntermediateResultBlock input, IntermediateResultElementSink sink, ExecutionContext execCxt) {
        for ( final SolutionMapping smR : input.getSolutionMappings() ) {
            solMWithMatchingR.add(smR);

            final Iterable<SolutionMapping> matchSolMapL = solMWithMatchingL.getJoinPartners(smR);
            for ( final SolutionMapping smL : matchSolMapL ){
                sink.send(SolutionMappingUtils.merge(smL, smR));
            }
        }
    }

    @Override
    public void wrapUpForChild2(IntermediateResultElementSink sink, ExecutionContext execCxt) {
        // nothing to do here
    }
}
