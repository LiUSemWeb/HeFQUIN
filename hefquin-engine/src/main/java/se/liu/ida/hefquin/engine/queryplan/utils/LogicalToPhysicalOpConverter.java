package se.liu.ida.hefquin.engine.queryplan.utils;

import se.liu.ida.hefquin.engine.queryplan.logical.BinaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.NaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.NullaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.*;
import se.liu.ida.hefquin.engine.queryplan.physical.BinaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.NaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.NullaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.UnaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.*;
import se.liu.ida.hefquin.federation.BRTPFServer;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.federation.TPFServer;

/**
 * This class provides methods to convert logical operators into
 * physical operators by using the respective default type of
 * physical operator for each type of logical operator.
 */
public class LogicalToPhysicalOpConverter
{
	public static PhysicalOperator convert( final LogicalOperator lop ) {
		if (      lop instanceof NullaryLogicalOp ) return convert( (NullaryLogicalOp) lop );
		else if ( lop instanceof UnaryLogicalOp )   return convert( (UnaryLogicalOp) lop );
		else if ( lop instanceof BinaryLogicalOp )  return convert( (BinaryLogicalOp) lop );
		else if ( lop instanceof NaryLogicalOp )    return convert( (NaryLogicalOp) lop );
		else throw new UnsupportedOperationException("Unsupported type of logical operator: " + lop.getClass().getName() + ".");
	}

	// --------- nullary operators -----------

	public static NullaryPhysicalOp convert( final NullaryLogicalOp lop ) {
		if ( lop instanceof LogicalOpRequest reqOp ) return convert(reqOp);
		else throw new UnsupportedOperationException("Unsupported type of logical operator: " + lop.getClass().getName() + ".");
	}

	public static NullaryPhysicalOp convert( final LogicalOpRequest<?,?> lop ) {
		return new PhysicalOpRequest<>(lop);
	}

	// --------- unary operators -----------

	public static UnaryPhysicalOp convert( final UnaryLogicalOp lop ) {
		if (      lop instanceof LogicalOpGPAdd x )     return convert(x);
		else if ( lop instanceof LogicalOpGPOptAdd x )  return convert(x);
		else if ( lop instanceof LogicalOpFilter x )    return convert(x);
		else if ( lop instanceof LogicalOpBind x )      return convert(x);
		else if ( lop instanceof LogicalOpLocalToGlobal x ) return convert (x);
		else if ( lop instanceof LogicalOpGlobalToLocal x ) return convert (x);
		else throw new UnsupportedOperationException("Unsupported type of logical operator: " + lop.getClass().getName() + ".");
	}

	public static UnaryPhysicalOp convert( final LogicalOpGPAdd lop ) {
		final FederationMember fm = lop.getFederationMember();

		if (      fm instanceof SPARQLEndpoint ) {
			return new PhysicalOpBindJoinWithVALUESorFILTER(lop);
		}
		else if ( fm instanceof TPFServer && lop.containsTriplePatternOnly() ) {
			return new PhysicalOpIndexNestedLoopsJoin(lop);
		}
		else if ( fm instanceof BRTPFServer && lop.containsTriplePatternOnly() ) {
			return new PhysicalOpIndexNestedLoopsJoin(lop);
		}

		else throw new UnsupportedOperationException("Unsupported type of federation member: " + fm.getClass().getName() + ".");
	}

	public static UnaryPhysicalOp convert( final LogicalOpGPOptAdd lop ) {
		final FederationMember fm = lop.getFederationMember();

		if (      fm instanceof SPARQLEndpoint ) {
			return new PhysicalOpBindJoinWithVALUESorFILTER(lop);
		}
		else if ( fm instanceof TPFServer && lop.containsTriplePatternOnly() ) {
			return new PhysicalOpIndexNestedLoopsJoin(lop);
		}
		else if ( fm instanceof BRTPFServer && lop.containsTriplePatternOnly() ) {
			return new PhysicalOpIndexNestedLoopsJoin(lop);
		}

		else throw new UnsupportedOperationException("Unsupported type of federation member: " + fm.getClass().getName() + ".");
	}

	public static UnaryPhysicalOp convert( final LogicalOpFilter lop ) {
		return new PhysicalOpFilter(lop);
	}

	public static UnaryPhysicalOp convert( final LogicalOpBind lop ) {
		return new PhysicalOpBind(lop);
	}

	public static UnaryPhysicalOp convert( final LogicalOpLocalToGlobal lop ) {
		return new PhysicalOpLocalToGlobal(lop);
	}

	public static UnaryPhysicalOp convert( final LogicalOpGlobalToLocal lop ) {
		return new PhysicalOpGlobalToLocal(lop);
	}

	// --------- binary operators -----------

	public static BinaryPhysicalOp convert( final BinaryLogicalOp lop ) {
		if (      lop instanceof LogicalOpJoin )     return convert( (LogicalOpJoin) lop );
		else if ( lop instanceof LogicalOpUnion )    return convert( (LogicalOpUnion) lop );
		else if ( lop instanceof LogicalOpRightJoin ) return convert( (LogicalOpRightJoin) lop );
		else throw new UnsupportedOperationException("Unsupported type of logical operator: " + lop.getClass().getName() + ".");
	}

	public static BinaryPhysicalOp convert( final LogicalOpJoin lop ) {
		return new PhysicalOpSymmetricHashJoin(lop);
	}

	public static BinaryPhysicalOp convert( final LogicalOpUnion lop ) {
		return new PhysicalOpBinaryUnion(lop);
	}

	public static BinaryPhysicalOp convert( final LogicalOpRightJoin lop ) {
		return new PhysicalOpHashRJoin(lop);
	}

	// --------- n-ary operators -----------

	public static NaryPhysicalOp convert( final NaryLogicalOp lop ) {
		if (      lop instanceof LogicalOpMultiwayJoin )  return convert( (LogicalOpMultiwayJoin) lop );
		else if ( lop instanceof LogicalOpMultiwayLeftJoin ) return convert( (LogicalOpMultiwayLeftJoin) lop );
		else if ( lop instanceof LogicalOpMultiwayUnion ) return convert( (LogicalOpMultiwayUnion) lop );
		else throw new UnsupportedOperationException("Unsupported type of logical operator: " + lop.getClass().getName() + ".");
	}

	public static NaryPhysicalOp convert( final LogicalOpMultiwayJoin lop ) {
		throw new UnsupportedOperationException();
	}

	public static NaryPhysicalOp convert( final LogicalOpMultiwayLeftJoin lop ) {
		throw new UnsupportedOperationException();
	}

	public static NaryPhysicalOp convert( final LogicalOpMultiwayUnion lop ) {
		return new PhysicalOpMultiwayUnion();
	}

}
