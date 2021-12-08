package se.liu.ida.hefquin.engine.queryplan.utils;

import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.queryplan.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.BinaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.NaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.NullaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.*;
import se.liu.ida.hefquin.engine.queryplan.physical.BinaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.NaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.NullaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.UnaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.*;

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
		if ( lop instanceof LogicalOpRequest ) return convert( (LogicalOpRequest<?,?>) lop );
		else throw new UnsupportedOperationException("Unsupported type of logical operator: " + lop.getClass().getName() + ".");
	}

	public static NullaryPhysicalOp convert( final LogicalOpRequest<?,?> lop ) {
		return new PhysicalOpRequest<>(lop);
	}

	// --------- unary operators -----------

	public static UnaryPhysicalOp convert( final UnaryLogicalOp lop ) {
		if (      lop instanceof LogicalOpTPAdd )  return convert( (LogicalOpTPAdd) lop );
		else if ( lop instanceof LogicalOpBGPAdd ) return convert( (LogicalOpBGPAdd) lop );
		else throw new UnsupportedOperationException("Unsupported type of logical operator: " + lop.getClass().getName() + ".");
	}

	public static UnaryPhysicalOp convert( final LogicalOpTPAdd lop ) {
		final FederationMember fm = lop.getFederationMember();

		if (      fm instanceof SPARQLEndpoint ) return new PhysicalOpBindJoinWithFILTER(lop);

		else if ( fm instanceof TPFServer )      return new PhysicalOpIndexNestedLoopsJoin(lop);

		else if ( fm instanceof BRTPFServer )    return new PhysicalOpBindJoin(lop);

		else throw new UnsupportedOperationException("Unsupported type of federation member: " + fm.getClass().getName() + ".");
	}

	public static UnaryPhysicalOp convert( final LogicalOpBGPAdd lop ) {
		final FederationMember fm = lop.getFederationMember();

		if (      fm instanceof SPARQLEndpoint ) return new PhysicalOpBindJoinWithFILTER(lop);

		else if ( fm instanceof TPFServer )      throw new IllegalArgumentException();

		else if ( fm instanceof BRTPFServer )    throw new IllegalArgumentException();

		else throw new UnsupportedOperationException("Unsupported type of federation member: " + fm.getClass().getName() + ".");
	}

	// --------- binary operators -----------

	public static BinaryPhysicalOp convert( final BinaryLogicalOp lop ) {
		if (      lop instanceof LogicalOpJoin )  return convert( (LogicalOpJoin) lop );
		else if ( lop instanceof LogicalOpUnion ) return convertUnion( (LogicalOpUnion) lop );
		else throw new UnsupportedOperationException("Unsupported type of logical operator: " + lop.getClass().getName() + ".");
	}

	public static BinaryPhysicalOp convert( final LogicalOpJoin lop ) {
		return new PhysicalOpSymmetricHashJoin(lop);
	}

	public static BinaryPhysicalOp convertUnion( final LogicalOpUnion lop ) {
		return new PhysicalOpBinaryUnion(lop);
	}

	// --------- n-ary operators -----------

	public static NaryPhysicalOp convert( final NaryLogicalOp lop ) {
		if (      lop instanceof LogicalOpMultiwayJoin )  return convert( (LogicalOpMultiwayJoin) lop );
		else if ( lop instanceof LogicalOpMultiwayUnion ) return convert( (LogicalOpMultiwayUnion) lop );
		else throw new UnsupportedOperationException("Unsupported type of logical operator: " + lop.getClass().getName() + ".");
	}

	public static NaryPhysicalOp convert( final LogicalOpMultiwayJoin lop ) {
		throw new UnsupportedOperationException();
	}

	public static NaryPhysicalOp convert( final LogicalOpMultiwayUnion lop ) {
		throw new UnsupportedOperationException();
	}

}
