package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import org.apache.jena.sparql.core.Var;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.impl.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.datastructures.SolutionMappingsIndex;
import se.liu.ida.hefquin.engine.datastructures.impl.SolutionMappingsHashTable;
import se.liu.ida.hefquin.engine.datastructures.impl.SolutionMappingsHashTableBasedOnOneVar;
import se.liu.ida.hefquin.engine.datastructures.impl.SolutionMappingsHashTableBasedOnTwoVars;
import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ExecOpHashJoin implements BinaryExecutableOp {
    protected final SolutionMappingsIndex solMHashTableL;
    protected boolean child1InputComplete = false;

    public ExecOpHashJoin(final ExpectedVariables inputVars1, final ExpectedVariables inputVars2) {
        final Set<Var> joinVars = new HashSet<>( inputVars1.getCertainVariables());
        joinVars.retainAll( inputVars2.getCertainVariables() );

        if (joinVars.size() == 1 ){
            final Var joinVar = joinVars.iterator().next();
            this.solMHashTableL = new SolutionMappingsHashTableBasedOnOneVar(joinVar);
        }
        else if (joinVars.size() == 2){
            final Iterator<Var> liVar = joinVars.iterator();
            final Var joinVar1 = liVar.next();
            final Var joinVar2 = liVar.next();

            this.solMHashTableL = new SolutionMappingsHashTableBasedOnTwoVars(joinVar1, joinVar2);
        }
        else{
            this.solMHashTableL = new SolutionMappingsHashTable(joinVars);
        }
    }

    @Override
    public int preferredInputBlockSize() {
        return 1;
    }

    @Override
    public boolean requiresCompleteChild1InputFirst() {
        return true;
    }

    @Override
    public void processBlockFromChild1( final IntermediateResultBlock input, final IntermediateResultElementSink sink, final ExecutionContext execCxt ) {
        for ( final SolutionMapping smL : input.getSolutionMappings() ) {
            solMHashTableL.add(smL);
        }
    }

    @Override
    public void wrapUpForChild1( final IntermediateResultElementSink sink, final ExecutionContext execCxt ) {
        this.child1InputComplete = true;
    }

    @Override
    public void processBlockFromChild2( final IntermediateResultBlock input, final IntermediateResultElementSink sink, final ExecutionContext execCxt ) {
        if (child1InputComplete == false){
            throw new IllegalStateException();
        }
        for ( final SolutionMapping smR : input.getSolutionMappings() ) {
            final Iterable<SolutionMapping> matchSolMapL = solMHashTableL.getJoinPartners(smR);
            for ( final SolutionMapping smL : matchSolMapL ){
                sink.send(SolutionMappingUtils.merge(smL, smR));
            }
        }
    }

    @Override
    public void wrapUpForChild2( final IntermediateResultElementSink sink, final ExecutionContext execCxt ) {
        // nothing to be done here
    }
}
