package se.liu.ida.hefquin.queryplan.executable.impl.ops;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import se.liu.ida.hefquin.data.SolutionMapping;
import se.liu.ida.hefquin.data.jenaimpl.JenaBasedSolutionMapping;
import se.liu.ida.hefquin.data.jenaimpl.JenaBasedSolutionMappingUtils;
import se.liu.ida.hefquin.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.queryproc.ExecutionContext;

import java.util.*;

public class ExecOpSymmetricHashJoin extends ExecOpGenericJoin{
    protected final Map<ListKey<Node>, JenaBasedSolutionMapping> mapL = new HashMap<>();
    protected final Map<ListKey<Node>, JenaBasedSolutionMapping> mapR = new HashMap<>();

    @Override
    public int preferredInputBlockSize() {
        return 1;
    }

    @Override
    public boolean requiresCompleteChild1InputFirst() {
        return false;
    }

    @Override
    public void processBlockFromChild1(IntermediateResultBlock input,
                                       IntermediateResultElementSink sink,
                                       ExecutionContext execCxt) {
        final List<SolutionMapping> liL = input.ArrayList();
        for(SolutionMapping SolMapL: liL){
            final JenaBasedSolutionMapping JBSolMapL = (JenaBasedSolutionMapping) SolMapL;
            final Binding LHS = JBSolMapL.asJenaBinding();

            // composite key
            final ArrayList<Node> valKeyL = new ArrayList<>();
            for (Var v : keys) {
                valKeyL.add(LHS.get(v));
            }
            // Insert into left hash map
            if (! valKeyL.isEmpty()){
                mapL.put(new ListKey(valKeyL), JBSolMapL);
            }

            // Use the listKey to probe the right hash table
            final JenaBasedSolutionMapping MatchSolMapR = mapR.get(new ListKey(valKeyL));
            if (MatchSolMapR != null){
                SolutionMapping out = JenaBasedSolutionMappingUtils.merge( MatchSolMapR, JBSolMapL);
                sink.send(out);
            }
        }
    }

    @Override
    public void wrapUpForChild1(IntermediateResultElementSink sink, ExecutionContext execCxt) {

    }

    @Override
    public void processBlockFromChild2(IntermediateResultBlock input, IntermediateResultElementSink sink, ExecutionContext execCxt) {
        final List<SolutionMapping> liR = input.ArrayList();
        for(SolutionMapping SolMapL: liR){
            final JenaBasedSolutionMapping JBSolMapR = (JenaBasedSolutionMapping) SolMapL;
            final Binding RHS = JBSolMapR.asJenaBinding();
            // composite key
            ArrayList<Node> valKeyR = new ArrayList<>();
            for(Var v : keys){
                valKeyR.add(RHS.get(v));
            }

            // Insert into right hash map
            if (! valKeyR.isEmpty()) {
                mapR.put(new ListKey(valKeyR), JBSolMapR);
            }
            // Use the listKey to probe the left hash table
            final JenaBasedSolutionMapping MatchSolMapL = mapL.get(new ListKey(valKeyR));
            if (MatchSolMapL != null){
                final SolutionMapping out = JenaBasedSolutionMappingUtils.merge( MatchSolMapL, JBSolMapR);
                sink.send(out);
            }
        }
    }

    @Override
    public void wrapUpForChild2(IntermediateResultElementSink sink, ExecutionContext execCxt) {

    }
}
