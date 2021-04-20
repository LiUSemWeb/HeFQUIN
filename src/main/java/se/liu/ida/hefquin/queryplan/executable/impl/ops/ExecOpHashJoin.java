package se.liu.ida.hefquin.queryplan.executable.impl.ops;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.join.JoinKey;
import se.liu.ida.hefquin.data.SolutionMapping;
import se.liu.ida.hefquin.data.jenaimpl.JenaBasedSolutionMapping;
import se.liu.ida.hefquin.data.jenaimpl.JenaBasedSolutionMappingUtils;
import se.liu.ida.hefquin.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.queryproc.ExecutionContext;
import org.apache.jena.sparql.engine.binding.BindingHashMap;

import java.util.*;

public class ExecOpHashJoin extends ExecOpGenericJoin {

    public ExecOpHashJoin() {
        super();
    }
    @Override
    protected Iterator<? extends SolutionMapping> joinImp(
            final IntermediateResultBlock inputL,
            final IntermediateResultBlock inputR,
            final ExecutionContext execCxt ){
        final Iterator<SolutionMapping> itL = inputL.iterator();
        final Iterator<SolutionMapping> itR = inputR.iterator();
        BindingHashMap LHS = new BindingHashMap();
        BindingHashMap RHS = new BindingHashMap();
        Collection<Var> varL = null;
        Collection<Var> varR = null;
        final JoinKey joinAttribute;

        while (itL.hasNext()) {
            final JenaBasedSolutionMapping JBSolMapL = (JenaBasedSolutionMapping) itL.next();
            LHS.addAll(JBSolMapL.asJenaBinding());
            varL = super.getVariables(JBSolMapL, execCxt);
        }
        while (itR.hasNext()) {
            final JenaBasedSolutionMapping JBSolMapR = (JenaBasedSolutionMapping) itR.next();
            RHS.addAll(JBSolMapR.asJenaBinding());
            varR = super.getVariables(JBSolMapR, execCxt);
        }
        joinAttribute = super.getJoinAttribute(varL, varR, execCxt);

        // build
        Map<Node, JenaBasedSolutionMapping> map = new HashMap<>() ;
        Iterator<Var> keys = joinAttribute.iterator();
        while (itL.hasNext()) {
            final JenaBasedSolutionMapping JBSolMapL = (JenaBasedSolutionMapping) itL.next();
            for(;keys.hasNext();){
                Var v = keys.next();
                map.put(LHS.get1(v), JBSolMapL);
            }
        }


        List<SolutionMapping> output = new ArrayList<>();
        Node[] keyValue = new Node[joinAttribute.length()];
        // Probe
        while (itR.hasNext()) {
            JenaBasedSolutionMapping JBSolMapR = (JenaBasedSolutionMapping) itR.next();
            JenaBasedSolutionMapping bind = null;

            for(int i= 0; i < joinAttribute.length();i++){
                Var v = keys.next();
                keyValue[i] = RHS.get1(v);
                if(i>0 && map.get(keyValue[i]) != map.get(keyValue[i-1])){
                    bind = null;
                }
                else{
                    bind = map.get(keyValue[i]);
                }
            }
            if (bind != null){
                SolutionMapping out = JenaBasedSolutionMappingUtils.merge( bind, JBSolMapR);
                output.add(out);
            }
        }
        return output.iterator();
    };

    // TODO: Hash map with multi-keys
}
