package se.liu.ida.hefquin.engine.queryplan.logical;

/**
 * An interface for any type of {@link LogicalOperator} that has
 * an arity of one; i.e., it is defined of as a function over one
 * multiset of solution mappings.
 */
public interface UnaryLogicalOp extends LogicalOperator
{

}
