package se.liu.ida.hefquin.engine.queryplan.utils;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.logical.BinaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.NaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.NullaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.BinaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.NaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.NullaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOpRegistry;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.UnaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.*;

/**
 * This class provides methods to convert logical operators into
 * physical operators by using the respective default type of
 * physical operator for each type of logical operator.
 */
public class LogicalToPhysicalOpConverter
{
	final private static PhysicalOpRegistry registry = new PhysicalOpRegistry()
		.register( PhysicalOpBinaryUnion.Factory.get() )                // Binary union
		.register( PhysicalOpMultiwayUnion.Factory.get() )              // Multiway union
		.register( PhysicalOpBind.Factory.get() )                       // Bind clauses
		.register( PhysicalOpFilter.Factory.get() )                     // Filter clauses
		.register( PhysicalOpRequest.Factory.get() )                    // Request at a federation member
		.register( PhysicalOpGlobalToLocal.Factory.get() )              // Apply vocab mappings global to local
		.register( PhysicalOpLocalToGlobal.Factory.get() )              // Apply vocab mappings local to global
		.register( PhysicalOpBindJoin.Factory.get() )                   // Bind-join for brTPF interface
		.register( PhysicalOpBindJoinWithBoundJoin.Factory.get() )      // Bind-join for SPARQL interface
		.register( PhysicalOpBindJoinWithVALUESorFILTER.Factory.get() ) // (fallback) if no non-joining var available
		// .register( PhysicalOpBindJoinWithUNION.Factory.get() )
		// .register( PhysicalOpBindJoinWithFILTER.Factory.get() )
		// .register( PhysicalOpBindJoinWithVALUES.Factory.get() )
		.register( PhysicalOpSymmetricHashJoin.Factory.get() )          // Inner join
		.register( PhysicalOpHashRJoin.Factory.get() )                  // Right outer join
		.register( PhysicalOpIndexNestedLoopsJoin.Factory.get() )       // Index NLJ algorithm, fm to request join partners
		// .register( PhysicalOpHashJoin.Factory.get() )
		// .register( PhysicalOpNaiveNestedLoopsJoin.Factory.get() )
	;

	public static PhysicalOperator convert( final LogicalOperator lop ) {
		return convert( lop, (ExpectedVariables[]) null );
	}

	public static PhysicalOperator convert( final LogicalOperator lop, final ExpectedVariables... inputVars ) {
		return registry.create(lop, inputVars);
	}

	public static NullaryPhysicalOp convert( final NullaryLogicalOp lop ) {
		return (NullaryPhysicalOp) convert( (LogicalOperator) lop );
	}

	public static UnaryPhysicalOp convert( final UnaryLogicalOp lop ) {
		return (UnaryPhysicalOp) convert( (LogicalOperator) lop );
	}

	public static BinaryPhysicalOp convert( final BinaryLogicalOp lop ) {
		return (BinaryPhysicalOp) convert( (LogicalOperator) lop );
	}

	public static NaryPhysicalOp convert( final NaryLogicalOp lop ) {
		return (NaryPhysicalOp) convert( (LogicalOperator) lop );
	}

	public static NullaryPhysicalOp convert( final NullaryLogicalOp lop, final ExpectedVariables... inputVars ) {
		return (NullaryPhysicalOp) convert( (LogicalOperator) lop, inputVars );
	}

	public static UnaryPhysicalOp convert( final UnaryLogicalOp lop, final ExpectedVariables... inputVars ) {
		return (UnaryPhysicalOp) convert( (LogicalOperator) lop, inputVars );
	}

	public static BinaryPhysicalOp convert( final BinaryLogicalOp lop, final ExpectedVariables... inputVars ) {
		return (BinaryPhysicalOp) convert( (LogicalOperator) lop, inputVars );
	}

	public static NaryPhysicalOp convert( final NaryLogicalOp lop, final ExpectedVariables... inputVars ) {
		return (NaryPhysicalOp) convert( (LogicalOperator) lop, inputVars );
	}
}
