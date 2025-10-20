package se.liu.ida.hefquin.engine.queryplan.utils;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.logical.BinaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.NaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.NullaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.BinaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.NaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.NullaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOpRegistry;
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
		.register( PhysicalOpBinaryUnion.getFactory() )                // Binary union
		.register( PhysicalOpMultiwayUnion.getFactory() )              // Multiway union
		.register( PhysicalOpBind.getFactory() )                       // Bind clauses
		.register( PhysicalOpFilter.getFactory() )                     // Filter clauses
		.register( PhysicalOpRequest.getFactory() )                    // Request at a federation member
		.register( PhysicalOpGlobalToLocal.getFactory() )              // Apply vocab mappings global to local
		.register( PhysicalOpLocalToGlobal.getFactory() )              // Apply vocab mappings local to global
		.register( PhysicalOpBindJoinBRTPF.getFactory() )                   // Bind-join for brTPF interface
		.register( PhysicalOpBindJoinWithBoundJoin.getFactory() )      // Bind-join for SPARQL interface
		.register( PhysicalOpBindJoinWithVALUESorFILTER.getFactory() ) // (fallback) if no non-joining var available
		// .register( PhysicalOpBindJoinWithUNION.getFactory() )
		// .register( PhysicalOpBindJoinWithFILTER.getFactory() )
		// .register( PhysicalOpBindJoinWithVALUES.getFactory() )
		.register( PhysicalOpSymmetricHashJoin.getFactory() )          // Inner join
		.register( PhysicalOpHashRJoin.getFactory() )                  // Right outer join
		.register( PhysicalOpIndexNestedLoopsJoin.getFactory() )       // Index NLJ algorithm, fm to request join partners
		// .register( PhysicalOpHashJoin.getFactory() )
		.register( PhysicalOpNaiveNestedLoopsJoin.getFactory() )
	;

	public static NullaryPhysicalOp convert( final NullaryLogicalOp lop ) {
		return registry.create(lop);
	}

	public static UnaryPhysicalOp convert( final UnaryLogicalOp lop,
	                                       final ExpectedVariables inputVars ) {
		return registry.create(lop, inputVars);
	}

	public static BinaryPhysicalOp convert( final BinaryLogicalOp lop,
	                                        final ExpectedVariables inputVars1,
	                                        final ExpectedVariables inputVars2 ) {
		return registry.create(lop, inputVars1, inputVars2);
	}

	public static NaryPhysicalOp convert( final NaryLogicalOp lop,
	                                      final ExpectedVariables... inputVars ) {
		return registry.create(lop, inputVars);
	}
}
