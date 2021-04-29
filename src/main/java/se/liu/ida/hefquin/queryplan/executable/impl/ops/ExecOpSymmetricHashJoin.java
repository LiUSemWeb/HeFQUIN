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

    protected List<SolutionMapping> liL;
    protected List<SolutionMapping> liR;
    protected List<Var> keys;

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
        final JenaBasedSolutionMapping JBSolMapL = (JenaBasedSolutionMapping) liL.get(execCxt.getIndex());
        final Binding LHS = JBSolMapL.asJenaBinding();

        // composite key
        final ArrayList<Node> valKeyL = new ArrayList<>();
        for (Var v : keys) {
            valKeyL.add(LHS.get(v));
        }

        // Insert into left hash map
        mapL.put(new ListKey(valKeyL), JBSolMapL);

        // Use the listKey to probe the right hash table
        final JenaBasedSolutionMapping MatchSolMapR = mapR.get(new ListKey(valKeyL));
        if (MatchSolMapR != null){
            SolutionMapping out = JenaBasedSolutionMappingUtils.merge( MatchSolMapR, JBSolMapL);
            sink.send(out);
        }
    }

    @Override
    public void wrapUpForChild1(IntermediateResultElementSink sink, ExecutionContext execCxt) {

    }

    @Override
    public void processBlockFromChild2(IntermediateResultBlock input, IntermediateResultElementSink sink, ExecutionContext execCxt) {
        final JenaBasedSolutionMapping JBSolMapR = (JenaBasedSolutionMapping) liR.get(execCxt.getIndex());
        final Binding RHS = JBSolMapR.asJenaBinding();
        // composite key
        ArrayList<Node> valKeyR = new ArrayList<>();
        for(Var v : keys){
            valKeyR.add(RHS.get(v));
        }


        // Insert into right hash map
        mapR.put(new ListKey(valKeyR), JBSolMapR);
        // Use the listKey to probe the left hash table
        final JenaBasedSolutionMapping MatchSolMapL = mapL.get(new ListKey(valKeyR));
        if (MatchSolMapL != null){
            final SolutionMapping out = JenaBasedSolutionMappingUtils.merge( MatchSolMapL, JBSolMapR);
            sink.send(out);
        }
    }

    @Override
    public void wrapUpForChild2(IntermediateResultElementSink sink, ExecutionContext execCxt) {

    }

    @Override
    public void process(IntermediateResultBlock input1,
                        IntermediateResultBlock input2,
                        IntermediateResultElementSink sink){
        this.liL = input1.ArrayList();
        this.liR = input2.ArrayList();
        Collection<Var> varL = new ArrayList<>();
        Collection<Var> varR = new ArrayList<>();

        final int size1 = liL.size();
        final int size2= liR.size();
        if (size1 > 0){
            varL = super.getVariables((JenaBasedSolutionMapping) liL.get(0));
        }
        if (size2 > 0){
            varR = super.getVariables((JenaBasedSolutionMapping) liR.get(0));
        }
        this.keys = super.findJoinVars(varL, varR);

        for (int i=0; i < Math.max(size1, size2); i++){
            final ExecutionContext execCxt = new ExecutionContext(i);
            if(i < size1){
                processBlockFromChild1(input1, sink, execCxt);
            }
            if(i < size2){
                processBlockFromChild2(input2, sink, execCxt);
            }
        }
    }
}
