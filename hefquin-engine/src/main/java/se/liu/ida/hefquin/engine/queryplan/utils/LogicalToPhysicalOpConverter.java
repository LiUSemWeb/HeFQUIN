package se.liu.ida.hefquin.engine.queryplan.utils;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOpRegistry;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
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
		return convert(lop, (ExpectedVariables[]) null);
	}

	public static PhysicalOperator convert( final LogicalOperator lop, final ExpectedVariables... inputVars ) {
		return registry.create(lop, inputVars);
	}
}
