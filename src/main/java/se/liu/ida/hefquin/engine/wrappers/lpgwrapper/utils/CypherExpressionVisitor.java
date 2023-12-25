package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.utils;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherExpression;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression.*;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.match.EdgeMatchClause;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.match.NodeMatchClause;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.match.PathMatchClause;

/**
 * This class is made to provide generic visitor functionalities to traverse expression trees.
 * The visit call is made recursive through the accept method in {@link CypherExpression}
 */
public interface CypherExpressionVisitor {
    /**
     * Recursive initial call
     */
    void visit(final CypherExpression ex);

    /**
     * Specific calls for each type of expression
     */
    void visitAliasedExpression(final AliasedExpression ex);
    void visitCountLargerThanZero(final CountLargerThanZeroExpression ex);
    void visitVar(final CypherVar var);
    void visitEquality(final EqualityExpression ex);
    void visitEXISTS(final EXISTSExpression ex);
    void visitGetItem(final GetItemExpression ex);
    void visitKeys(final KeysExpression ex);
    void visitLabels(final FirstLabelExpression ex);
    void visitLiteral(final LiteralExpression ex);
    void visitMembership(final MembershipExpression ex);
    void visitPropertyAccess(final PropertyAccessExpression ex);
    void visitPropertyAccessWithVar(final PropertyAccessWithVarExpression ex);
    void visitTripleMap(final TripleMapExpression ex);
    void visitType(final TypeExpression ex);
    void visitUnwind(final UnwindIteratorImpl iterator);
    void visitID(final VariableIDExpression ex);
    void visitVariableLabel (final VariableLabelExpression ex);
    void visitEdgeMatch(final EdgeMatchClause ex);
    void visitNodeMatch(final NodeMatchClause ex);
    void visitPathMatch(final PathMatchClause ex);
}
