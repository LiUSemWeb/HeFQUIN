package se.liu.ida.hefquin.queryplan.executable.impl.ops;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import se.liu.ida.hefquin.data.jenaimpl.JenaBasedSolutionMapping;
import se.liu.ida.hefquin.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.queryplan.executable.IntermediateResultElementSink;
import org.apache.jena.sparql.engine.join.JoinKey;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;
import java.util.List;

public abstract class ExecOpGenericJoin implements BinaryExecutableOp {

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
        final List<Var> keys = new ArrayList<Var>();
        while(keyIt.hasNext()){
            keys.add(keyIt.next());
        }
        return keys;
    }

    protected abstract void process(
            final IntermediateResultBlock inputL,
            final IntermediateResultBlock inputR,
            final IntermediateResultElementSink sink);
}
