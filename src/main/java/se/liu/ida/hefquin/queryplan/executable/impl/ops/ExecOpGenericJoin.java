package se.liu.ida.hefquin.queryplan.executable.impl.ops;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import se.liu.ida.hefquin.data.SolutionMapping;
import se.liu.ida.hefquin.data.jenaimpl.JenaBasedSolutionMapping;
import se.liu.ida.hefquin.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.queryproc.ExecutionContext;
import org.apache.jena.sparql.engine.join.JoinKey;

import java.util.Iterator;
import java.util.Collection;

public abstract class ExecOpGenericJoin implements BinaryExecutableOp {
    @Override
    public int preferredInputBlockSize() {
        // TODO
        return 1;
    }

    @Override
    public void processBlockFromChild1(
            final IntermediateResultBlock input,
            final IntermediateResultElementSink sink,
            final ExecutionContext execCxt) {
        final Iterator<SolutionMapping> it = input.iterator();
        while (it.hasNext()) {
            sink.send(it.next());
        }
    }

    @Override
    public void processBlockFromChild2(
            final IntermediateResultBlock input,
            final IntermediateResultElementSink sink,
            final ExecutionContext execCxt){
        final Iterator<SolutionMapping> it = input.iterator();
        while (it.hasNext()) {
            sink.send(it.next());
        }
    };

    @Override
    public void concludeExecution(final IntermediateResultElementSink sink,
                                  final ExecutionContext execCxt){
    };

    protected Collection<Var> getVariables(
            final JenaBasedSolutionMapping JBSolMap,
            final ExecutionContext execCxt ){
        final Binding b = JBSolMap.asJenaBinding();
        final Iterator<Var> variables = b.vars();
        Collection<Var> vars = null;
        while (variables.hasNext()) {
            final Var v = variables.next();
            vars.add(v);
        }
        return vars;
    };

    protected JoinKey getJoinAttribute(
            final Collection<Var> vars1,
            final Collection<Var> vars2,
            final ExecutionContext execCxt ){
        final JoinKey joinAttribute= JoinKey.create( vars1, vars2);
        return joinAttribute;
    };

    protected void execute(
            final IntermediateResultBlock inputL,
            final IntermediateResultBlock inputR,
            final IntermediateResultElementSink sink,
            final ExecutionContext execCxt)
    {
        final Iterator<? extends SolutionMapping> it = joinImp(inputL, inputR, execCxt);
        while ( it.hasNext() ) {
            sink.send(it.next());
        }
    }

    protected abstract Iterator<? extends SolutionMapping> joinImp(
            final IntermediateResultBlock inputL,
            final IntermediateResultBlock inputR,
            final ExecutionContext execCxt );

}
