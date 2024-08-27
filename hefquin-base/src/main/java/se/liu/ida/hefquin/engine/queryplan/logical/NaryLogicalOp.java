package se.liu.ida.hefquin.engine.queryplan.logical;

/**
 * An interface for any type of {@link LogicalOperator} that has an
 * arbitrary arity ; i.e., it is defined of as a function over an
 * arbitrary number of multisets of solution mappings.
 */
public interface NaryLogicalOp extends LogicalOperator
{

}
