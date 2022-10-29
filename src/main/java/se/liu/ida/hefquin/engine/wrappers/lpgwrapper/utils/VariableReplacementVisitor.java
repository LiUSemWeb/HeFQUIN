package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.utils;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherExpression;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression.*;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.match.EdgeMatchClause;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.match.NodeMatchClause;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class VariableReplacementVisitor implements CypherExpressionVisitor {
    protected final Map<CypherVar, CypherVar> equivalences;

    protected final Stack<CypherExpression> stack = new Stack<>();

    public VariableReplacementVisitor(final Map<CypherVar, CypherVar> equivalences) {
        this.equivalences = equivalences;
    }


    @Override
    public void visit(final CypherExpression ex) {
        ex.acceptVisitor(this);
    }

    @Override
    public void visitAliasedExpression(final AliasedExpression ex) {
        //we need to get the elements from the stack in reverse order
        assert stack.peek() instanceof CypherVar;
        final CypherVar alias = (CypherVar) stack.pop();
        final CypherExpression exp = stack.pop();
        stack.push(new AliasedExpression(exp, alias));
    }

    @Override
    public void visitCountLargerThanZero(final CountLargerThanZeroExpression ex) {
        stack.push(ex);
    }

    @Override
    public void visitVar(final CypherVar var) {
        stack.push(equivalences.getOrDefault(var, var));
    }

    @Override
    public void visitEquality(final EqualityExpression ex) {
        final CypherExpression rhs = stack.pop();
        final CypherExpression lhs = stack.pop();
        stack.push(new EqualityExpression(lhs, rhs));
    }

    @Override
    public void visitEXISTS(final EXISTSExpression ex) {
        stack.push(new EXISTSExpression(stack.pop()));
    }

    @Override
    public void visitGetItem(final GetItemExpression ex) {
        stack.push(new GetItemExpression(stack.pop(), ex.getIndex()));
    }

    @Override
    public void visitKeys(final KeysExpression ex) {
        assert stack.peek() instanceof CypherVar;
        stack.push(new KeysExpression((CypherVar) stack.pop()));
    }

    @Override
    public void visitLabels(final LabelsExpression ex) {
        assert stack.peek() instanceof CypherVar;
        stack.push(new LabelsExpression((CypherVar) stack.pop()));
    }

    @Override
    public void visitLiteral(final LiteralExpression ex) {
        stack.push(ex);
    }

    @Override
    public void visitMembership(final MembershipExpression ex) {
        assert stack.peek() instanceof ListCypherExpression;
        final ListCypherExpression list = (ListCypherExpression) stack.pop();
        assert stack.peek() instanceof CypherVar;
        final CypherVar var = (CypherVar) stack.pop();
        stack.push(new MembershipExpression(var, list));
    }

    @Override
    public void visitPropertyAccess(final PropertyAccessExpression ex) {
        assert stack.peek() instanceof CypherVar;
        stack.push(new PropertyAccessExpression((CypherVar) stack.pop(), ex.getProperty()));
    }

    @Override
    public void visitPropertyAccessWithVar(final PropertyAccessWithVarExpression ex) {
        assert stack.peek() instanceof CypherVar;
        final CypherVar innerVar = (CypherVar) stack.pop();
        assert stack.peek() instanceof CypherVar;
        final CypherVar var = (CypherVar) stack.pop();
        stack.push(new PropertyAccessWithVarExpression(var, innerVar));
    }

    @Override
    public void visitTripleMap(final TripleMapExpression ex) {
        assert stack.peek() instanceof CypherVar;
        final CypherVar target = (CypherVar) stack.pop();
        assert stack.peek() instanceof CypherVar;
        final CypherVar edge = (CypherVar) stack.pop();
        assert stack.peek() instanceof CypherVar;
        final CypherVar source = (CypherVar) stack.pop();
        stack.push(new TripleMapExpression(source, edge, target));
    }

    @Override
    public void visitType(final TypeExpression ex) {
        assert stack.peek() instanceof CypherVar;
        stack.push(new TypeExpression((CypherVar) stack.pop()));
    }

    @Override
    public void visitUnwind(final UnwindIteratorImpl iterator) {
        assert stack.peek() instanceof CypherVar;
        final CypherVar alias = (CypherVar) stack.pop();
        final List<CypherExpression> returnExps = new ArrayList<>();
        for (int i = iterator.getReturnExpressions().size(); i > 0; i--) {
            returnExps.add(stack.pop());
        }
        final List<BooleanCypherExpression> filters = new ArrayList<>();
        for (int i = iterator.getFilters().size(); i > 0; i--) {
            assert stack.peek() instanceof BooleanCypherExpression;
            filters.add((BooleanCypherExpression) stack.pop());
        }
        assert stack.peek() instanceof ListCypherExpression;
        final ListCypherExpression list = (ListCypherExpression) stack.pop();
        assert stack.peek() instanceof CypherVar;
        final CypherVar innerVar = (CypherVar) stack.pop();
        stack.push(new UnwindIteratorImpl(innerVar, list, filters, returnExps, alias));
    }

    @Override
    public void visitID(final VariableIDExpression ex) {
        assert stack.peek() instanceof CypherVar;
        stack.push(new VariableIDExpression((CypherVar) stack.pop()));
    }

    @Override
    public void visitVariableLabel(final VariableLabelExpression ex) {
        assert stack.peek() instanceof CypherVar;
        stack.push(new VariableLabelExpression((CypherVar) stack.pop(), ex.getLabel()));
    }

    @Override
    public void visitEdgeMatch(final EdgeMatchClause ex) {
        assert stack.peek() instanceof CypherVar;
        final CypherVar target = (CypherVar) stack.pop();
        assert stack.peek() instanceof CypherVar;
        final CypherVar edge = (CypherVar) stack.pop();
        assert stack.peek() instanceof CypherVar;
        final CypherVar source = (CypherVar) stack.pop();
        stack.push(new EdgeMatchClause(source, edge, target));
    }

    @Override
    public void visitNodeMatch(final NodeMatchClause ex) {
        assert stack.peek() instanceof CypherVar;
        stack.push(new NodeMatchClause((CypherVar) stack.pop()));
    }

    public CypherExpression getResult() {
        return stack.pop();
    }

}
