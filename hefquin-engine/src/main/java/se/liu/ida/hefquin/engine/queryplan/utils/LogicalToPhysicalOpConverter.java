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
		.register( new PhysicalOpBinaryUnion.Factory() )           // Binary union
		.register( new PhysicalOpMultiwayUnion.Factory() )         // Multiway union
		.register( new PhysicalOpBind.Factory() )                  // Bind clauses
		.register( new PhysicalOpFilter.Factory() )                // Filter clauses
		.register( new PhysicalOpRequest.Factory() )               // Request at a federation member
		.register( new PhysicalOpGlobalToLocal.Factory() )         // Apply vocab mappings global to local
		.register( new PhysicalOpLocalToGlobal.Factory() )         // Apply vocab mappings local to global
		.register( new PhysicalOpBindJoin.Factory() )              // Bind-join for brTPF interface
		.register( new PhysicalOpBindJoinWithBoundJoin.Factory() ) // Bind-join for SPARQL interface
		.register( new PhysicalOpBindJoinWithVALUESorFILTER.Factory() ) // (fallback) if no non-joining var available
		// .register( new PhysicalOpBindJoinWithUNION.Factory() )
		// .register( new PhysicalOpBindJoinWithFILTER.Factory() )
		// .register( new PhysicalOpBindJoinWithVALUES.Factory() )
		.register( new PhysicalOpSymmetricHashJoin.Factory() )     // Inner join
		.register( new PhysicalOpHashRJoin.Factory() )             // Right outer join
		.register( new PhysicalOpIndexNestedLoopsJoin.Factory() )  // Index NLJ algorithm, fm to request join partners
		// .register( new PhysicalOpHashJoin.Factory() )
		// .register( new PhysicalOpNaiveNestedLoopsJoin.Factory() )
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
