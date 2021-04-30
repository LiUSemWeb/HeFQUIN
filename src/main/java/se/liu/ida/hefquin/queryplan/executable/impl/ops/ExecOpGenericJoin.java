package se.liu.ida.hefquin.queryplan.executable.impl.ops;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import se.liu.ida.hefquin.data.SolutionMapping;
import se.liu.ida.hefquin.data.jenaimpl.JenaBasedSolutionMapping;
import se.liu.ida.hefquin.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.queryplan.executable.IntermediateResultElementSink;
import org.apache.jena.sparql.engine.join.JoinKey;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;
import java.util.List;

public abstract class ExecOpGenericJoin implements BinaryExecutableOp {
    protected List<Var> keys;

    protected Collection<Var> getVariables( final JenaBasedSolutionMapping JBSolMap){
        final Binding b = JBSolMap.asJenaBinding();
        final Iterator<Var> variables = b.vars();
        Collection<Var> vars = new ArrayList<>();
        while (variables.hasNext()) {
            final Var v = variables.next();
            vars.add(v);
        }
        return vars;
    }

    public List<Var> findJoinVars(Collection<Var> varL, Collection<Var> varR){
        // get join keys
        final JoinKey joinAttribute= JoinKey.create( varL, varR);
        final Iterator<Var> keyIt = joinAttribute.iterator();
        final List<Var> keys = new ArrayList<>();
        while(keyIt.hasNext()){
            keys.add(keyIt.next());
        }
        return keys;
    }

    protected void preprocess(
            final IntermediateResultBlock inputL,
            final IntermediateResultBlock inputR,
            final IntermediateResultElementSink sink){
        final List<SolutionMapping> li1= inputL.ArrayList();
        final List<SolutionMapping> li2= inputR.ArrayList();
        Collection<Var> varL = new ArrayList<>();
        Collection<Var> varR = new ArrayList<>();

        if (inputL.size() > 0){
            varL = getVariables((JenaBasedSolutionMapping) li1.get(0));
        }
        if (inputR.size() > 0){
            varR = getVariables((JenaBasedSolutionMapping) li2.get(0));
        }
        this.keys = findJoinVars(varL, varR);
    }
}
