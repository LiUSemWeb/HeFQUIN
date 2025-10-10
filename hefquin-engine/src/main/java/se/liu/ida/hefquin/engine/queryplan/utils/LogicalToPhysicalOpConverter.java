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
		.register( PhysicalOpBinaryUnion.Factory.getInstance() )                // Binary union
		.register( PhysicalOpMultiwayUnion.Factory.getInstance() )              // Multiway union
		.register( PhysicalOpBind.Factory.getInstance() )                       // Bind clauses
		.register( PhysicalOpFilter.Factory.getInstance() )                     // Filter clauses
		.register( PhysicalOpRequest.Factory.getInstance() )                    // Request at a federation member
		.register( PhysicalOpGlobalToLocal.Factory.getInstance() )              // Apply vocab mappings global to local
		.register( PhysicalOpLocalToGlobal.Factory.getInstance() )              // Apply vocab mappings local to global
		.register( PhysicalOpBindJoin.Factory.getInstance() )                   // Bind-join for brTPF interface
		.register( PhysicalOpBindJoinWithBoundJoin.Factory.getInstance() )      // Bind-join for SPARQL interface
		.register( PhysicalOpBindJoinWithVALUESorFILTER.Factory.getInstance() ) // (fallback) if no non-joining var available
		// .register( PhysicalOpBindJoinWithUNION.Factory.getInstance() )
		// .register( PhysicalOpBindJoinWithFILTER.Factory.getInstance() )
		// .register( PhysicalOpBindJoinWithVALUES.Factory.getInstance() )
		.register( PhysicalOpSymmetricHashJoin.Factory.getInstance() )          // Inner join
		.register( PhysicalOpHashRJoin.Factory.getInstance() )                  // Right outer join
		.register( PhysicalOpIndexNestedLoopsJoin.Factory.getInstance() )       // Index NLJ algorithm, fm to request join partners
		// .register( PhysicalOpHashJoin.Factory.getInstance() )
		.register( PhysicalOpNaiveNestedLoopsJoin.Factory.getInstance() )
	;

	protected static PhysicalOperator _convert( final LogicalOperator lop ) {
		return _convert( lop, (ExpectedVariables[]) null );
	}

	protected static PhysicalOperator _convert( final LogicalOperator lop, final ExpectedVariables... inputVars ) {
		return registry.create(lop, inputVars);
	}

	public static NullaryPhysicalOp convert( final NullaryLogicalOp lop ) {
		return (NullaryPhysicalOp) _convert(lop);
	}

	public static UnaryPhysicalOp convert( final UnaryLogicalOp lop ) {
		return (UnaryPhysicalOp) _convert(lop);
	}

	public static BinaryPhysicalOp convert( final BinaryLogicalOp lop ) {
		return (BinaryPhysicalOp) _convert(lop);
	}

	public static NaryPhysicalOp convert( final NaryLogicalOp lop ) {
		return (NaryPhysicalOp) _convert(lop);
	}

	public static UnaryPhysicalOp convert( final UnaryLogicalOp lop, final ExpectedVariables inputVars ) {
		return (UnaryPhysicalOp) _convert(lop, inputVars);
	}

	public static BinaryPhysicalOp convert( final BinaryLogicalOp lop,
	                                        final ExpectedVariables inputVars1,
	                                        final ExpectedVariables inputVars2 ) {
		return (BinaryPhysicalOp) _convert(lop, inputVars1, inputVars2);
	}

	public static NaryPhysicalOp convert( final NaryLogicalOp lop, final ExpectedVariables... inputVars ) {
		return (NaryPhysicalOp) _convert(lop, inputVars);
	}
}
