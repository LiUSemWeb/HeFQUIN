package se.liu.ida.hefquin.engine.queryplan.utils;

import java.util.NoSuchElementException;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.logical.BinaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.NaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.NullaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.BinaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.NaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.NullaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.UnaryPhysicalOp;

/**
 * Implementations of this interface provide methods to
 * convert logical operators into physical operators.
 */
public interface LogicalToPhysicalOpConverter
{
	/**
	 * Returns a physical operator that implements the given logical
	 * operator. If this converter knows of multiple types of physical
	 * operators that may be used for the given logical operator, then
	 * the default type is used. If this converter does not know of any
	 * type of physical operator that may be used for the given logical
	 * operator, then an {@link UnsupportedOperationException} is thrown.
	 *
	 * @param lop - the logical operator to be converted
	 * @return a physical operator for the given logical operator
	 * @throws NoSuchElementException if this converter does not know of
	 *                     type of physical operator for the given input
	 */
	NullaryPhysicalOp convert( NullaryLogicalOp lop );

	/**
	 * Returns a physical operator that implements the given logical
	 * operator under the assumption that this operator will be used
	 * in a plan in which the subplan under this operator will produce
	 * solution mappings with the given variables.
	 * <p>
	 * If this converter knows of multiple types of physical operators
	 * that may be applied in this case, then the default type is used.
	 * If this converter does not know of any type of physical operator
	 * that may be applied in this case, then an
	 * {@link UnsupportedOperationException} is thrown.
	 *
	 * @param lop - the logical operator to be converted
	 * @param inputVars - the variables that can be expected to be bound
	 *                    in solution mappings that the physical operator
	 *                    will have to process
	 * @return a physical operator for the given logical operator
	 * @throws NoSuchElementException if this converter does not know of
	 *                     type of physical operator for the given input
	 */
	UnaryPhysicalOp convert( UnaryLogicalOp lop, ExpectedVariables inputVars );

	/**
	 * Returns a physical operator that implements the given logical
	 * operator under the assumption that this operator will be used
	 * in a plan in which the two subplans under this operator will
	 * produce solution mappings with the given variables, respectively.
	 * <p>
	 * If this converter knows of multiple types of physical operators
	 * that may be applied in this case, then the default type is used.
	 * If this converter does not know of any type of physical operator
	 * that may be applied in this case, then an
	 * {@link UnsupportedOperationException} is thrown.
	 *
	 * @param lop - the logical operator to be converted
	 * @param inputVars1 - the variables that can be expected to be bound
	 *                     in solution mappings that the physical operator
	 *                     will have to process as its left input
	 * @param inputVars2 - the variables that can be expected to be bound
	 *                     in solution mappings that the physical operator
	 *                     will have to process as its right input
	 * @return a physical operator for the given logical operator
	 * @throws NoSuchElementException if this converter does not know of
	 *                     type of physical operator for the given input
	 */
	BinaryPhysicalOp convert( BinaryLogicalOp lop,
	                          ExpectedVariables inputVars1,
	                          ExpectedVariables inputVars2 );

	/**
	 * Returns a physical operator that implements the given logical
	 * operator under the assumption that this operator will be used
	 * in a plan in which the subplans under this operator will produce
	 * solution mappings with the given variables, respectively.
	 * <p>
	 * If this converter knows of multiple types of physical operators
	 * that may be applied in this case, then the default type is used.
	 * If this converter does not know of any type of physical operator
	 * that may be applied in this case, then an
	 * {@link UnsupportedOperationException} is thrown.
	 *
	 * @param lop - the logical operator to be converted
	 * @param inputVars - the variables that can be expected to be bound
	 *                    in solution mappings that the physical operator
	 *                    will have to process for each of its inputs
	 * @return a physical operator for the given logical operator
	 * @throws NoSuchElementException if this converter does not know of
	 *                     type of physical operator for the given case
	 */
	NaryPhysicalOp convert( NaryLogicalOp lop, ExpectedVariables... inputVars );
}
