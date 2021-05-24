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
        //TODO
        return 10;
    }

    @Override
    public boolean requiresCompleteChild1InputFirst() {
        return true;
    }

    @Override
    public void processBlockFromChild1(IntermediateResultBlock input, IntermediateResultElementSink sink, ExecutionContext execCxt) {
        for ( final SolutionMapping smL : input.getSolutionMappings() ) {
            solMHashTableL.add(smL);
        }
    }

    @Override
    public void wrapUpForChild1(IntermediateResultElementSink sink, ExecutionContext execCxt) {
        // nothing to be done here
    }

    @Override
    public void processBlockFromChild2(IntermediateResultBlock input, IntermediateResultElementSink sink, ExecutionContext execCxt) {
        for ( final SolutionMapping smR : input.getSolutionMappings() ) {
            final Iterable<SolutionMapping> matchSolMapL = solMHashTableL.getJoinPartners(smR);
            for ( final SolutionMapping smL : matchSolMapL ){
                sink.send(SolutionMappingUtils.merge(smL, smR));
            }
        }
    }

    @Override
    public void wrapUpForChild2(IntermediateResultElementSink sink, ExecutionContext execCxt) {
        // nothing to be done here
    }
}
